package com.voicechat.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Định dạng payload và mã cuộc trò chuyện trực tiếp. */
public final class DirectChatProtocol {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int VERSION = 1;
    public static final int MAX_TEXT_LENGTH = 1000;

    public record Payload(int version, String id, String text) {
    }

    private DirectChatProtocol() {
    }

    public static String conversationId(String first, String second) {
        if (first == null || second == null
                || first.isBlank() || second.isBlank()) {
            throw new IllegalArgumentException("ID peer không được rỗng.");
        }

        boolean firstComesBefore = first.compareTo(second) <= 0;
        String a = firstComesBefore ? first : second;
        String b = firstComesBefore ? second : first;

        String key = a.length() + ":" + a + b.length() + ":" + b;

        return "dm:" + UUID.nameUUIDFromBytes(
                key.getBytes(StandardCharsets.UTF_8));
    }

    public static String encode(String id, String text) throws IOException {
        Payload payload = new Payload(
                VERSION,
                UUID.fromString(id).toString(),
                cleanText(text));

        return JSON.writeValueAsString(payload);
    }

    public static Payload decode(String json) throws IOException {
        try {
            Payload payload = JSON.readValue(json, Payload.class);

            if (payload == null || payload.version() != VERSION) {
                throw new IllegalArgumentException(
                        "Phiên bản tin nhắn không hợp lệ.");
            }

            return new Payload(
                    VERSION,
                    UUID.fromString(payload.id()).toString(),
                    cleanText(payload.text()));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IOException(
                    "Tin nhắn trực tiếp không hợp lệ.", ex);
        }
    }

    private static String cleanText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Tin nhắn không được rỗng.");
        }

        String value = text.strip();
        if (value.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "Tin nhắn tối đa 1000 ký tự.");
        }

        return value;
    }
}
