package com.voicechat.database;

import com.voicechat.common.api.HistoryStore;
import com.voicechat.common.model.ChatMessage;
import java.util.List;

/** TV4 TODO: create tables, INSERT, SELECT qua JDBC. */
public final class HistoryRepository implements HistoryStore {
    @Override public void save(ChatMessage message) {
        throw new UnsupportedOperationException("TV4 TODO: persist chat message");
    }
    @Override public List<ChatMessage> findByRoom(String roomId) {
        throw new UnsupportedOperationException("TV4 TODO: read history");
    }
}
