package com.voicechat.common.model;

import java.time.Instant;

public record ControlMessage(Type type, String senderId, String targetId,
                             String roomId, String payload, Instant sentAt) {
    public enum Type {
        HELLO, CHAT, CALL_REQUEST, CALL_ACCEPT, CALL_REJECT, CALL_END,
        ROOM_CREATE, ROOM_JOIN, ROOM_LEAVE, ROOM_MEMBERS
    }
}
