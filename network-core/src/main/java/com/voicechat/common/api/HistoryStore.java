package com.voicechat.common.api;

import com.voicechat.common.model.ChatMessage;
import java.util.List;

/** TV4 trien khai luu tru tin nhan. */
public interface HistoryStore {

    // ===== Ghi tin nhắn =====
    // Lưu một tin nhắn qua lớp lưu trữ được ứng dụng cung cấp.
    void save(ChatMessage message);

    // ===== Đọc lịch sử theo phòng =====
    // Lấy các tin nhắn thuộc cùng mã phòng hoặc cuộc trò chuyện.
    List<ChatMessage> findByRoom(String roomId);
}
