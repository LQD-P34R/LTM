package com.voicechat.common.api;

/** Hop dong TV2 trien khai; TV3 goi thong qua interface nay. */
public interface VoiceGateway {
    void startCall(String peerId);
    void stopCall();
    void setMuted(boolean muted);
    boolean isMuted();
}
