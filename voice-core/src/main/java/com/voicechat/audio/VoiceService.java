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
    private final NetworkGateway network;
    private volatile CallSession current;

    private static final class CallSession {
        final String peerId;
        final AudioCapture capture = new AudioCapture();
        final AudioPlayer player = new AudioPlayer();
        final JitterBuffer jitter = new JitterBuffer();
        final ScheduledExecutorService playout = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon().name("voice-playout").factory());

        long nextSequence; // Chi luong thu mic tang bien nay.
        volatile boolean muted;

        CallSession(String peerId) {
            this.peerId = peerId;
        }
    }

    public VoiceService(NetworkGateway network) {
        this.network = Objects.requireNonNull(network);
        network.onVoicePacket(this::receivePacket);
    }

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

    private synchronized void handleAudioError(CallSession session, Exception error) {
        if (current == session) {
            System.err.println("Loi voice pipeline: " + error);
            stopCall();
        }
    }

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
}
