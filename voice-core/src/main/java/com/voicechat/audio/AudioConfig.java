package com.voicechat.audio;

import javax.sound.sampled.AudioFormat;

public final class AudioConfig {
    private AudioConfig() {
    }

    public static final AudioFormat FORMAT = new AudioFormat(16_000f, 16, 1, true, false);

    public static final int CHUNK_MS = 20;
    public static final int CHUNK_BYTES = 640;
}