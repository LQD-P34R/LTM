package com.voicechat.common.api;

/** Hop dong TV2 trien khai; TV3 goi thong qua interface nay. */
public interface VoiceGateway {

    // ===== Vòng đời âm thanh cuộc gọi =====
    // Khởi động hoặc dừng luồng thu/phát âm thanh cho peer được chọn.
    void startCall(String peerId);

    void stopCall();

    // ===== Điều khiển microphone =====
    // Đặt và đọc trạng thái tắt microphone của phiên gọi.
    void setMuted(boolean muted);

    boolean isMuted();
}
