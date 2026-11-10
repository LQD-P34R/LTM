package com.voicechat.common.model;

import java.time.Instant;

public record ChatMessage(String id, String roomId, String senderId, String text, Instant sentAt) { }
