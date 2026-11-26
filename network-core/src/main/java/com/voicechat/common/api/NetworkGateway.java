package com.voicechat.common.api;

import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Hop dong TV1 trien khai, cac thanh vien khac su dung. */
public interface NetworkGateway {

    // ===== Vòng đời kết nối =====
    // Kết nối tới peer, đọc danh sách peer và ngắt toàn bộ kết nối.
    void connect(String host, int tcpPort) throws IOException;

    Set<String> connectedPeerIds();

    void disconnect();

    // ===== Gửi dữ liệu mạng =====
    // Tách bản tin điều khiển qua TCP và gói âm thanh qua UDP.
    void sendControl(String peerId, ControlMessage message) throws IOException;

    void sendVoice(String peerId, byte[] packet) throws IOException;

    // ===== Đăng ký callback nhận dữ liệu và trạng thái =====
    // Lớp triển khai gọi các hàm đã đăng ký khi có bản tin hoặc thay đổi kết nối.
    void onControlMessage(Consumer<ControlMessage> callback);

    void onVoicePacket(BiConsumer<String, byte[]> callback);

    void onPeerConnected(Consumer<String> callback);

    void onPeerDisconnected(Consumer<String> callback);
}
