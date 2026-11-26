package com.voicechat.audio;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public final class AudioPlayer {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private static final int MAX_CHUNKS = 5;
    private volatile SourceDataLine line;
    private ArrayBlockingQueue<byte[]> queue;

    // ===== Khởi động phát âm thanh =====
    // Mở loa và tạo hàng đợi riêng cho mỗi phiên phát.
    public synchronized void start() throws LineUnavailableException {
        if (line != null) {
            throw new IllegalStateException("Loa đang hoạt động");
        }

        SourceDataLine output = AudioSystem.getSourceDataLine(AudioConfig.FORMAT);

        // Mỗi lần start() dùng một hàng đợi riêng.
        ArrayBlockingQueue<byte[]> frames = new ArrayBlockingQueue<>(MAX_CHUNKS);

        try {
            output.open(
                    AudioConfig.FORMAT,
                    AudioConfig.CHUNK_BYTES * MAX_CHUNKS);

            output.start();

            queue = frames;
            line = output;

            Thread.ofPlatform()
                    .name("audio-player")
                    .daemon()
                    .start(() -> playLoop(output, frames));

        } catch (LineUnavailableException | RuntimeException e) {
            line = null;
            queue = null;
            output.close();
            throw e;
        }
    }

    // ===== Nhận và phát các khối PCM =====
    // Xếp khối âm thanh vào hàng đợi có giới hạn rồi ghi ra loa trên luồng riêng.
    public synchronized void enqueue(byte[] pcm) {
        Objects.requireNonNull(pcm, "pcm không được null");

        if (pcm.length != AudioConfig.CHUNK_BYTES) {
            throw new IllegalArgumentException(
                    "Mỗi khối PCM phải có "
                            + AudioConfig.CHUNK_BYTES + " byte");
        }

        // Bỏ qua dữ liệu đến khi player chưa chạy hoặc đã dừng.
        if (line == null) {
            return;
        }

        byte[] copy = pcm.clone();

        if (!queue.offer(copy)) {
            // Hàng đợi đầy: bỏ khối cũ nhất để giữ dữ liệu mới.
            queue.poll();
            queue.offer(copy);
        }
    }

    private void playLoop(
            SourceDataLine output,
            ArrayBlockingQueue<byte[]> frames) {

        try {
            while (line == output) {
                byte[] pcm = frames.poll(100, TimeUnit.MILLISECONDS);

                if (pcm == null) {
                    continue;
                }

                int offset = 0;

                while (offset < pcm.length && line == output) {
                    int n = output.write(
                            pcm,
                            offset,
                            pcm.length - offset);

                    if (n <= 0) {
                        return;
                    }

                    offset += n;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            if (line == output) {
                System.err.println("Lỗi phát âm thanh: " + e);
            }
        } finally {
            frames.clear();
            release(output);
        }
    }

    // ===== Dừng và giải phóng loa =====
    // Xóa hàng đợi, đóng thiết bị và giữ an toàn khi phiên phát đã thay đổi.
    public synchronized void stop() {
        SourceDataLine output = line;
        line = null;

        if (queue != null) {
            queue.clear();
            queue = null;
        }

        if (output != null) {
            output.stop();
            output.flush();
            output.close();
        }
    }

    private synchronized void release(SourceDataLine output) {
        // Luồng cũ không được xóa trạng thái của phiên phát mới.
        if (line == output) {
            line = null;
            queue = null;
        }

        output.close();
    }
}