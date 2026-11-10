package com.voicechat.chat;

import com.voicechat.common.api.HistoryStore;
import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * TV4: nghiep vu chat. Truyen qua NetworkGateway, lich su qua HistoryStore.
 * Chua co xu ly nhan tin nhan tu TCP: can dang ky callback sau khi TV1 xong.
 */
public final class ChatService {
    private final NetworkGateway network;
    private final HistoryStore history;

    public ChatService(NetworkGateway network, HistoryStore history) {
        this.network = network;
        this.history = history;
    }

    public ChatMessage sendText(String targetPeerId, String roomId,
                                String senderId, String text) throws IOException {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
        ChatMessage chat = new ChatMessage(UUID.randomUUID().toString(), roomId,
                                           senderId, text.trim(), Instant.now());
        ControlMessage control = new ControlMessage(ControlMessage.Type.CHAT,
                senderId, targetPeerId, roomId, chat.text(), chat.sentAt());
        network.sendControl(targetPeerId, control);
        history.save(chat);
        return chat;
    }

    public List<ChatMessage> historyFor(String roomId) {
        return history.findByRoom(roomId);
    }
}
