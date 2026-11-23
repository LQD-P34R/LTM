package com.voicechat.audio;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/** Kiem tra bang dong ho gia lap; khong mo mic, loa hoac ket noi mang. */
public final class JitterBufferTest {
    @Test
    public void handlesPacketFormatAndPlaybackTiming() {
        byte[] pcm = frame(7);
        byte[] encoded = VoicePacket.encode(42, pcm);
        VoicePacket decoded = VoicePacket.decode(encoded);
        require(encoded.length == 652 && decoded != null
                && decoded.sequence() == 42 && Arrays.equals(pcm, decoded.pcm()),
                "Packet round-trip");
        encoded[0] ^= 1;
        require(VoicePacket.decode(encoded) == null
                && VoicePacket.decode(new byte[640]) == null,
                "Reject malformed packets");

        JitterBuffer reordered = new JitterBuffer();
        reordered.offer(1, frame(11), ms(0));
        reordered.offer(0, frame(10), ms(10));
        reordered.offer(2, frame(12), ms(20));
        require(reordered.poll(ms(59)) == null, "Wait 60 ms before playback");
        requireFrame(reordered.poll(ms(60)), 10, "Reorder first packet");
        requireFrame(reordered.poll(ms(80)), 11, "Reorder second packet");
        requireFrame(reordered.poll(ms(100)), 12, "Reorder third packet");

        JitterBuffer lost = new JitterBuffer();
        lost.offer(0, frame(10), ms(0));
        lost.offer(2, frame(12), ms(40)); // Sequence 1 bi mat.
        lost.offer(2, frame(99), ms(41)); // Goi trung khong duoc ghi de.
        requireFrame(lost.poll(ms(60)), 10, "Play available packet");
        requireFrame(lost.poll(ms(80)), 0, "Replace missing packet with silence");
        lost.offer(1, frame(11), ms(81)); // Da qua han sequence 1.
        requireFrame(lost.poll(ms(100)), 12, "Ignore duplicate and late packet");

        JitterBuffer stalled = new JitterBuffer();
        for (int i = 0; i < 10; i++) {
            stalled.offer(i, frame(10 + i), ms(i * 20));
        }
        requireFrame(stalled.poll(ms(60)), 10, "Start playout");
        requireFrame(stalled.poll(ms(160)), 15, "Skip expired slots after stall");
        require(stalled.poll(ms(160)) == null, "Do not burst catch-up frames");
        requireFrame(stalled.poll(ms(180)), 16, "Resume 20 ms cadence");

        JitterBuffer reset = new JitterBuffer();
        reset.offer(0, frame(10), ms(0));
        requireFrame(reset.poll(ms(60)), 10, "Start before long gap");
        reset.offer(1000, frame(20), ms(80));
        reset.offer(1, frame(99), ms(81));
        require(reset.poll(ms(139)) == null, "Rebuffer after large jump");
        requireFrame(reset.poll(ms(140)), 20, "Recover after large jump");

        JitterBuffer single = new JitterBuffer();
        single.offer(0, frame(10), ms(0));
        requireFrame(single.poll(ms(60)), 10, "Do not wait forever for 3 packets");
        requireFrame(single.poll(ms(80)), 0, "Continue silence during outage");

    }

    private static long ms(long value) {
        return TimeUnit.MILLISECONDS.toNanos(value);
    }

    private static byte[] frame(int marker) {
        byte[] pcm = new byte[AudioConfig.CHUNK_BYTES];
        Arrays.fill(pcm, (byte) marker);
        return pcm;
    }

    private static void requireFrame(byte[] actual, int marker, String message) {
        require(Arrays.equals(actual, frame(marker)), message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
