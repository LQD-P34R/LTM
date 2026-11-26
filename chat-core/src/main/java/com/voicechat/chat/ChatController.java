package com.voicechat.chat;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;

/** TV4: logic gui chat; KHONG chua Swing / UI. */
public final class ChatController {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private final NetworkGateway network;

    // ===== Khởi tạo tầng gửi chat =====
    // Nhận cổng giao tiếp mạng từ bên ngoài.
    public ChatController(NetworkGateway network) {
        this.network = network;
    }

    // ===== Gửi bản tin chat =====
    // Chỉ chấp nhận loại CHAT rồi chuyển bản tin cho tầng mạng.
    public void sendMessage(String targetPeerId, ControlMessage message) throws IOException {
        if (message.type() != ControlMessage.Type.CHAT) {
            throw new IllegalArgumentException("Only CHAT message is supported here");
        }
        network.sendControl(targetPeerId, message);
    }
}
