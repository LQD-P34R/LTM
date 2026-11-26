package com.voicechat.audio;

import java.util.Objects;
import java.util.function.Consumer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public final class AudioCapture {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    // Luồng điều khiển và luồng đọc mic cùng truy cập biến này.
    private volatile TargetDataLine line;

    // ===== Khởi động thu âm =====
    // Mở microphone theo AudioConfig và tạo luồng đọc âm thanh riêng.
    public synchronized void start(Consumer<byte[]> onAudio)
            throws LineUnavailableException {

        Objects.requireNonNull(onAudio, "onAudio không được null");

        if (line != null) {
            throw new IllegalStateException("Microphone đang thu âm");
        }

        TargetDataLine input = AudioSystem.getTargetDataLine(AudioConfig.FORMAT);

        try {
            // Bộ đệm thiết bị: yêu cầu khoảng 100 ms.
            input.open(
                    AudioConfig.FORMAT,
                    AudioConfig.CHUNK_BYTES * 5);

            input.start();
            line = input;

            Thread.ofPlatform()
                    .name("audio-capture")
                    .daemon()
                    .start(() -> readLoop(input, onAudio));

        } catch (LineUnavailableException | RuntimeException e) {
            line = null;
            input.close();
            throw e;
        }
    }

    // ===== Đọc PCM từ microphone =====
    // Ghép dữ liệu thành từng khối đủ 20 ms rồi chuyển bản sao cho callback.
    private void readLoop(
            TargetDataLine input,
            Consumer<byte[]> onAudio) {

        byte[] chunk = new byte[AudioConfig.CHUNK_BYTES];
        int offset = 0;

        try {
            while (line == input) {
                int n = input.read(
                        chunk,
                        offset,
                        chunk.length - offset);

                // Dừng nếu thiết bị đã đóng hoặc phiên thu đã đổi.
                if (n <= 0 || line != input) {
                    break;
                }

                offset += n;

                if (offset == chunk.length) {
                    onAudio.accept(chunk.clone());
                    offset = 0;
                }
            }
        } catch (RuntimeException e) {
            // Không báo lỗi khi read() kết thúc do stop().
            if (line == input) {
                System.err.println("Lỗi thu âm: " + e);
            }
        } finally {
            release(input);
        }
    }

    // ===== Dừng và giải phóng microphone =====
    // Dừng phiên thu và bảo đảm luồng cũ không xóa trạng thái của phiên mới.
    public synchronized void stop() {
        TargetDataLine input = line;
        line = null;

        if (input != null) {
            input.stop();
            input.close();
        }
    }

    private synchronized void release(TargetDataLine input) {
        // Luồng cũ không được xóa trạng thái của phiên thu mới.
        if (line == input) {
            line = null;
        }

        input.close();
    }
}