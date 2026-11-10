package com.voicechat.common.api;

import com.voicechat.common.model.ChatMessage;
import java.util.List;

/** TV4 trien khai luu tru tin nhan. */
public interface HistoryStore {
    void save(ChatMessage message);
    List<ChatMessage> findByRoom(String roomId);
}
