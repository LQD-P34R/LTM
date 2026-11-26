package com.voicechat.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Giao thuc rieng: magic 4 byte + sequence 8 byte + PCM 640 byte. */
public record VoicePacket(long sequence, byte[] pcm) {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private static final int MAGIC = 0x56433031; // ASCII: VC01
    public static final int HEADER_BYTES = Integer.BYTES + Long.BYTES;
    public static final int PACKET_BYTES = HEADER_BYTES + AudioConfig.CHUNK_BYTES;

    // ===== Đóng gói âm thanh gửi đi =====
    // Ghi magic, số thứ tự và PCM vào mảng byte theo thứ tự big-endian.
    public static byte[] encode(long sequence, byte[] pcm) {
        if (sequence < 0 || pcm == null || pcm.length != AudioConfig.CHUNK_BYTES) {
            throw new IllegalArgumentException("Sequence hoac khoi PCM khong hop le");
        }

        return ByteBuffer.allocate(PACKET_BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(MAGIC)
                .putLong(sequence)
                .put(pcm)
                .array();
    }

    // ===== Giải mã âm thanh nhận về =====
    // Kiểm tra định dạng và trả về sequence/PCM; gói không hợp lệ trả về null.
    /** Tra ve null neu datagram khong dung giao thuc. */
    public static VoicePacket decode(byte[] raw) {
        if (raw == null || raw.length != PACKET_BYTES) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
        if (buffer.getInt() != MAGIC) {
            return null;
        }

        long sequence = buffer.getLong();
        if (sequence < 0) {
            return null;
        }

        byte[] pcm = new byte[AudioConfig.CHUNK_BYTES];
        buffer.get(pcm);
        return new VoicePacket(sequence, pcm);
    }
}
