package com.voicechat.audio;

import javax.sound.sampled.AudioFormat;

public final class AudioConfig {
    // ===== Cấu hình và trạng thái =====
    // PCM mono 16 kHz, 16 bit có dấu, little-endian: mỗi khối 20 ms chứa 640 byte.
    public static final AudioFormat FORMAT = new AudioFormat(16_000f, 16, 1, true, false);
    public static final int CHUNK_MS = 20;
    public static final int CHUNK_BYTES = 640;

    // ===== Lớp cấu hình dùng chung =====
    // Chỉ dùng các hằng số PCM phía trên; constructor riêng ngăn tạo đối tượng cấu hình.
    private AudioConfig() {
    }
}
