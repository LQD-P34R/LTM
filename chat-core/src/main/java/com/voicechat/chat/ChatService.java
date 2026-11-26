package com.voicechat.chat;

import com.voicechat.common.api.HistoryStore;
import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.ControlMessage;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Nghiệp vụ chat trực tiếp; không chứa giao diện Swing. */
public final class ChatService {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    public static final int MAX_TEXT_LENGTH = DirectChatProtocol.MAX_TEXT_LENGTH;
    private final NetworkGateway network;
    private final HistoryStore history;

    // ===== Khởi tạo nghiệp vụ chat =====
    // Nhận tầng mạng và kho lịch sử qua interface để không phụ thuộc giao diện.
    public ChatService(NetworkGateway network, HistoryStore history) {
        this.network = Objects.requireNonNull(network);
        this.history = Objects.requireNonNull(history);
    }

    // ===== Gửi tin nhắn trực tiếp =====
    // Tạo ID, kiểm tra payload, gửi qua mạng rồi lưu tin vào lịch sử.
    public ChatMessage sendDirect(
            String localPeerId,
            String targetPeerId,
            String text) throws IOException {

        String roomId = DirectChatProtocol.conversationId(
                localPeerId, targetPeerId);

        String id = UUID.randomUUID().toString();

        // Kiểm tra nội dung trước khi gửi hoặc ghi database.
        String payload = DirectChatProtocol.encode(id, text);
        Instant sentAt = Instant.now();

        ChatMessage chat = new ChatMessage(
                id, roomId, localPeerId, text.strip(), sentAt);

        ControlMessage control = new ControlMessage(
                ControlMessage.Type.CHAT,
                localPeerId,
                targetPeerId,
                roomId,
                payload,
                sentAt);

        network.sendControl(targetPeerId, control);
        history.save(chat);

        return chat;
    }

    // ===== Nhận tin nhắn trực tiếp =====
    // Kiểm tra người nhận/mã hội thoại, giải mã nội dung và lưu tin hợp lệ.
    public ChatMessage receiveDirect(
            String localPeerId,
            ControlMessage control) throws IOException {

        if (localPeerId == null || localPeerId.isBlank()
                || control == null
                || control.type() != ControlMessage.Type.CHAT
                || !localPeerId.equals(control.targetId())
                || control.senderId() == null
                || control.senderId().isBlank()
                || control.sentAt() == null) {
            throw new IOException(
                    "Thông tin tin nhắn trực tiếp không hợp lệ.");
        }

        String expectedRoom = DirectChatProtocol.conversationId(
                localPeerId, control.senderId());

        if (!expectedRoom.equals(control.roomId())) {
            throw new IOException(
                    "Tin nhắn không thuộc cuộc trò chuyện này.");
        }

        DirectChatProtocol.Payload payload = DirectChatProtocol.decode(control.payload());

        ChatMessage chat = new ChatMessage(
                payload.id(),
                expectedRoom,
                control.senderId(),
                payload.text(),
                control.sentAt());

        history.save(chat);
        return chat;
    }

    // ===== Đọc lịch sử hội thoại =====
    // Tính cùng một mã hội thoại cho hai peer rồi truy vấn kho lịch sử.
    public List<ChatMessage> directHistory(
            String localPeerId,
            String otherPeerId) {

        String roomId = DirectChatProtocol.conversationId(
                localPeerId, otherPeerId);

        return history.findByRoom(roomId);
    }
}
