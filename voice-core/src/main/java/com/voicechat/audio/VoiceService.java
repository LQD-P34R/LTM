package com.voicechat.audio;

import com.voicechat.common.api.VoiceGateway;

/** TV2: wire AudioCapture/AudioPlayer va NetworkGateway. */
public final class VoiceService implements VoiceGateway {
    @Override public void startCall(String peerId) {
        throw new UnsupportedOperationException("TV2 TODO: start voice pipeline");
    }
    @Override public void stopCall() {
        // TV2 TODO: stop capture/playback safely
    }
    @Override public void setMuted(boolean muted) {
        throw new UnsupportedOperationException("TV2 TODO: microphone mute");
    }
    @Override public boolean isMuted() {
        throw new UnsupportedOperationException("TV2 TODO: microphone mute state");
    }
}
