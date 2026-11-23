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

/**
 * TV4: nghiep vu chat. Truyen qua NetworkGateway, lich su qua HistoryStore.
 * Direct chat uses roomId=null on the wire and a local dm_<other peer> key.
 */
public final class ChatService {
    public static final int MAX_TEXT_LENGTH = 16_000;
    private final NetworkGateway network;
    private final HistoryStore history;

    /** Session history is owned by the desktop UI; persistence is optional. */
    public ChatService(NetworkGateway network) {
        this(network, null);
    }

    public ChatService(NetworkGateway network, HistoryStore history) {
        this.network = Objects.requireNonNull(network);
        this.history = history;
    }

    public ChatMessage sendText(String targetPeerId, String roomId,
                                String senderId, String text) throws IOException {
        validateText(text);
        ChatMessage chat = new ChatMessage(UUID.randomUUID().toString(), roomId,
                                           senderId, text.trim(), Instant.now());
        ControlMessage control = new ControlMessage(ControlMessage.Type.CHAT,
                senderId, targetPeerId, roomId, chat.text(), chat.sentAt());
        network.sendControl(targetPeerId, control);
        if (history != null) history.save(chat);
        return chat;
    }

    public ChatMessage sendDirectText(String targetPeerId, String senderId, String text)
            throws IOException {
        if (targetPeerId == null || targetPeerId.isBlank() || targetPeerId.equals(senderId)) {
            throw new IllegalArgumentException("Người nhận không hợp lệ.");
        }
        validateText(text);
        ChatMessage chat = new ChatMessage(UUID.randomUUID().toString(), "dm_" + targetPeerId,
                senderId, text.trim(), Instant.now());
        network.sendControl(targetPeerId, new ControlMessage(ControlMessage.Type.CHAT,
                senderId, targetPeerId, null, chat.text(), chat.sentAt()));
        if (history != null) history.save(chat);
        return chat;
    }

    /** Caller routes messages from the established TCP peer, not arbitrary datagrams. */
    public ChatMessage receiveDirectText(ControlMessage message, String localId) {
        if (message == null || message.type() != ControlMessage.Type.CHAT
                || !Objects.equals(localId, message.targetId()) || message.roomId() != null
                || message.senderId() == null || message.senderId().isBlank()
                || message.senderId().equals(localId) || message.sentAt() == null
                || message.payload() == null || message.payload().isBlank()
                || message.payload().length() > MAX_TEXT_LENGTH) return null;
        ChatMessage chat = new ChatMessage(UUID.randomUUID().toString(),
                "dm_" + message.senderId(), message.senderId(), message.payload(), message.sentAt());
        if (history != null) history.save(chat);
        return chat;
    }

    private static void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Tin nhắn không được để trống.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Tin nhắn tối đa " + MAX_TEXT_LENGTH + " ký tự.");
        }
    }

    public List<ChatMessage> historyFor(String roomId) {
        return history == null ? List.of() : history.findByRoom(roomId);
    }
}
