package com.voicechat.audio;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/** Bo dem co dinh cho PCM 20 ms; nhan va lay du lieu o hai luong khac nhau. */
public final class JitterBuffer {
    private static final long FRAME_NS =
            TimeUnit.MILLISECONDS.toNanos(AudioConfig.CHUNK_MS);
    private static final long PREFILL_NS = 3 * FRAME_NS;
    private static final int MAX_PACKETS = 50;

    private final TreeMap<Long, byte[]> packets = new TreeMap<>();

    private long nextSequence = -1;
    private long minimumSequence;
    private long nextPlayNanos;
    private boolean playing;

    public synchronized void offer(long sequence, byte[] pcm, long nowNanos) {
        if (sequence < minimumSequence || pcm == null
                || pcm.length != AudioConfig.CHUNK_BYTES) {
            return;
        }

        if (nextSequence == -1) {
            nextSequence = sequence;
            nextPlayNanos = nowNanos + PREFILL_NS;
        }

        // Trong 60 ms dau, van nhan goi co sequence nho hon goi den dau tien.
        if (!playing && sequence < nextSequence) {
            if (nextSequence - sequence >= MAX_PACKETS) {
                return;
            }
            nextSequence = sequence;
        }

        if (sequence < nextSequence) {
            return; // Goi da qua han phat.
        }

        // Nhay qua xa: bo backlog va dem lai, tranh mac ket o sequence cu.
        if (sequence - nextSequence >= MAX_PACKETS) {
            packets.clear();
            nextSequence = sequence;
            minimumSequence = sequence;
            nextPlayNanos = nowNanos + PREFILL_NS;
            playing = false;
        }

        packets.putIfAbsent(sequence, pcm.clone());

        // Giu toi da 50 vi tri sequence, ke ca khi moc dau duoc lui lai.
        while (!packets.isEmpty()
                && packets.lastKey() - nextSequence >= MAX_PACKETS) {
            packets.pollLastEntry();
        }
    }

    /** null = chua den han; khoi 0 = da den han nhung thieu goi. */
    public synchronized byte[] poll(long nowNanos) {
        if (nextSequence == -1 || nowNanos - nextPlayNanos < 0) {
            return null;
        }

        playing = true;

        // Neu scheduler bi cham, bo slot cu thay vi phat bu hang loat.
        long skipped = (nowNanos - nextPlayNanos) / FRAME_NS;
        nextSequence += skipped;
        nextPlayNanos += (skipped + 1) * FRAME_NS;

        packets.headMap(nextSequence, false).clear();
        byte[] pcm = packets.remove(nextSequence);

        nextSequence++;
        minimumSequence = nextSequence;

        return pcm != null ? pcm : new byte[AudioConfig.CHUNK_BYTES];
    }
}
