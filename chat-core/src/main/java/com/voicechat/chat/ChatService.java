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
    private final NetworkGateway network;
    private final HistoryStore history;

    public ChatService(NetworkGateway network, HistoryStore history) {
        this.network = Objects.requireNonNull(network);
        this.history = Objects.requireNonNull(history);
    }

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

    public List<ChatMessage> directHistory(
            String localPeerId,
            String otherPeerId) {

        String roomId = DirectChatProtocol.conversationId(
                localPeerId, otherPeerId);

        return history.findByRoom(roomId);
    }
}