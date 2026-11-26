package com.voicechat.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Định dạng payload và mã cuộc trò chuyện trực tiếp. */
public final class DirectChatProtocol {
    // ===== Dữ liệu và các kiểu hỗ trợ =====
    // Các trường lưu cấu hình/trạng thái; các kiểu lồng nhau mô tả dữ liệu và hợp đồng dùng trong lớp.
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int VERSION = 1;
    public static final int MAX_TEXT_LENGTH = 1000;
    public record Payload(int version, String id, String text) {
    }

    // ===== Khởi tạo lớp tiện ích =====
    // Constructor riêng vì các thao tác giao thức đều là hàm static.
    private DirectChatProtocol() {
    }

    // ===== Định danh cuộc trò chuyện =====
    // Sắp xếp hai ID để hai phía luôn tạo ra cùng một mã hội thoại.
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

    // ===== Mã hóa và kiểm tra payload chat =====
    // Chuyển payload sang/từ JSON; kiểm tra phiên bản, UUID và giới hạn nội dung.
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
