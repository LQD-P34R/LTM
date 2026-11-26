package com.voicechat.common.model;

import java.time.Instant;

/** Dữ liệu tin nhắn: ID duy nhất, phòng/hội thoại, người gửi, nội dung và thời điểm gửi. */
public record ChatMessage(String id, String roomId, String senderId, String text, Instant sentAt) { }
