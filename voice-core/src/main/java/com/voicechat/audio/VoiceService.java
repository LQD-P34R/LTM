package com.voicechat.audio;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.LineUnavailableException;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.api.VoiceGateway;

public final class VoiceService implements VoiceGateway {
    // ===== Dữ liệu và các kiểu hỗ trợ =====
    // Các trường lưu cấu hình/trạng thái; các kiểu lồng nhau mô tả dữ liệu và hợp đồng dùng trong lớp.
    private final NetworkGateway network;
    private volatile CallSession current;
    private static final class CallSession {
        // ===== Cấu hình và trạng thái =====
        // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
        final String peerId;
        final AudioCapture capture = new AudioCapture();
        final AudioPlayer player = new AudioPlayer();
        final JitterBuffer jitter = new JitterBuffer();
        final ScheduledExecutorService playout = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon().name("voice-playout").factory());
        long nextSequence; // Chi luong thu mic tang bien nay.
        volatile boolean muted;

        // ===== Khởi tạo dữ liệu phiên gọi =====
        // Ghi nhớ peer đích; mỗi phiên có microphone, loa, bộ đệm và lịch phát riêng.
        CallSession(String peerId) {
            this.peerId = peerId;
        }
    }

    // ===== Khởi tạo dịch vụ thoại =====
    // Gắn tầng mạng và đăng ký callback nhận gói UDP.
    public VoiceService(NetworkGateway network) {
        this.network = Objects.requireNonNull(network);
        network.onVoicePacket(this::receivePacket);
    }

    // ===== Khởi động phiên âm thanh =====
    // Kiểm tra peer, mở loa/microphone và lập lịch phát PCM mỗi 20 ms.
    @Override
    public synchronized void startCall(String peerId) {
        Objects.requireNonNull(peerId, "peerId khong duoc null");
        if (peerId.isBlank()) {
            throw new IllegalArgumentException("peerId khong duoc rong");
        }
        if (current != null) {
            throw new IllegalStateException("Dang co cuoc goi khac");
        }
        if (!network.connectedPeerIds().contains(peerId)) {
            throw new IllegalStateException("Peer chua ket noi: " + peerId);
        }

        CallSession session = new CallSession(peerId);
        current = session;
        try {
            session.player.start();
            session.playout.scheduleAtFixedRate(
                    () -> playTick(session), 0, AudioConfig.CHUNK_MS,
                    TimeUnit.MILLISECONDS);
            session.capture.start(pcm -> sendPcm(session, pcm));
        } catch (LineUnavailableException | RuntimeException e) {
            stopCall();
            throw new IllegalStateException("Khong the khoi dong am thanh", e);
        }
    }

    // ===== Thu và gửi âm thanh =====
    // Đóng gói PCM kèm sequence; khi mute vẫn gửi khối im lặng để giữ nhịp.
    private void sendPcm(CallSession session, byte[] pcm) {
        if (current != session) {
            return;
        }
        try {
            // Mute van gui im lang de sequence va nhip 20 ms tiep tuc.
            byte[] audio = session.muted
                    ? new byte[AudioConfig.CHUNK_BYTES]
                    : pcm;
            byte[] packet = VoicePacket.encode(session.nextSequence++, audio);
            network.sendVoice(session.peerId, packet);
        } catch (IOException | RuntimeException e) {
            handleAudioError(session, e);
        }
    }

    // ===== Nhận, đệm và phát âm thanh =====
    // Giải mã UDP vào jitter buffer rồi lấy PCM theo lịch để đưa ra loa.
    private void receivePacket(String senderId, byte[] raw) {
        CallSession session = current;
        if (session == null || !session.peerId.equals(senderId)) {
            return;
        }
        VoicePacket packet = VoicePacket.decode(raw);
        if (packet != null) {
            session.jitter.offer(packet.sequence(), packet.pcm(), System.nanoTime());
        }
    }

    private void playTick(CallSession session) {
        if (current != session) {
            return;
        }
        try {
            byte[] pcm = session.jitter.poll(System.nanoTime());
            if (pcm != null && current == session) {
                session.player.enqueue(pcm);
            }
        } catch (RuntimeException e) {
            handleAudioError(session, e);
        }
    }

    // ===== Điều khiển microphone =====
    // Đặt hoặc đọc cờ mute của phiên gọi hiện tại.
    @Override
    public void setMuted(boolean muted) {
        CallSession session = current;
        if (session != null) {
            session.muted = muted;
        }
    }

    @Override
    public boolean isMuted() {
        CallSession session = current;
        return session != null && session.muted;
    }

    // ===== Kết thúc phiên và xử lý lỗi =====
    // Dừng bộ lập lịch, microphone và loa; bỏ qua lỗi từ phiên đã hết hiệu lực.
    @Override
    public synchronized void stopCall() {
        CallSession session = current;
        current = null;
        if (session == null) {
            return;
        }
        session.playout.shutdownNow();
        try {
            session.capture.stop();
        } finally {
            session.player.stop();
        }
    }

    private synchronized void handleAudioError(CallSession session, Exception error) {
        if (current == session) {
            System.err.println("Loi voice pipeline: " + error);
            stopCall();
        }
    }
}
