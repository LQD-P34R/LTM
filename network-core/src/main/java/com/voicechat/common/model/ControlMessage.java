package com.voicechat.common.model;

import java.time.Instant;

/** Bản tin điều khiển qua TCP: loại thao tác, các bên liên quan, payload và thời điểm gửi. */
public record ControlMessage(Type type, String senderId, String targetId,
                             String roomId, String payload, Instant sentAt) {
    // ===== Phân loại bản tin điều khiển =====
    // Bên nhận dùng type để chuyển payload đến xử lý bắt tay, chat, cuộc gọi hoặc phòng nhóm.
    public enum Type {
        HELLO, CHAT, CALL_REQUEST, CALL_ACCEPT, CALL_REJECT, CALL_END,
        ROOM_CREATE, ROOM_JOIN, ROOM_LEAVE, ROOM_MEMBERS
    }
}
