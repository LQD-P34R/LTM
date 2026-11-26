package com.voicechat.database;

import com.voicechat.common.api.HistoryStore;
import com.voicechat.common.model.ChatMessage;
import java.time.Instant;
import java.sql.*;
import java.util.List;
import static com.voicechat.database.DatabaseManager.*;

public final class HistoryRepository implements HistoryStore {
    // ===== Dữ liệu và các kiểu hỗ trợ =====
    // Các trường lưu cấu hình/trạng thái; các kiểu lồng nhau mô tả dữ liệu và hợp đồng dùng trong lớp.
    private final DatabaseManager db;
    public record Line(String id,String senderId,String sender,String roomName,String text,String at) {}
    public record Page(List<Line> lines, boolean hasOlder) {}

    // ===== Khởi tạo kho lịch sử =====
    // Dùng database mặc định hoặc database được truyền vào.
    public HistoryRepository() { this(new DatabaseManager()); }

    public HistoryRepository(DatabaseManager db) { this.db = db; }

    // ===== Lưu và đọc tin nhắn theo phòng =====
    // Bỏ qua ID tin trùng, đọc theo thứ tự seq và ánh xạ dòng SQL thành ChatMessage.
    @Override public void save(ChatMessage m) {
        db.transaction(c -> update(c, "INSERT INTO messages(id,room,sender,body,sent_at) VALUES(?,?,?,?,?) ON CONFLICT(id) DO NOTHING",
                m.id(), m.roomId(), m.senderId(), label(m.text(), 1000), m.sentAt().toString()));
    }

    @Override public List<ChatMessage> findByRoom(String room) {
        return db.transaction(c -> query(c,"SELECT * FROM messages WHERE room=? ORDER BY seq",this::message,room));
    }

    private ChatMessage message(ResultSet r) throws SQLException {
        return new ChatMessage(r.getString("id"),r.getString("room"),r.getString("sender"),
                r.getString("body"), Instant.parse(r.getString("sent_at")));
    }

    // ===== Đọc lịch sử nhóm có phân trang =====
    // Chỉ đọc các phòng người xem đang tham gia; mỗi trang có tối đa 30 tin.
    /** Null room selects history across all rooms that the viewer currently belongs to. */
    public Page readPage(String viewer,String room,int page) {
        if (page < 0) throw new IllegalArgumentException("Trang không hợp lệ.");
        List<Line> rows = db.transaction(c -> query(c,
                """
                SELECT m.id, m.sender, u.username, r.name, m.body, m.sent_at
                FROM messages m JOIN users u ON u.id=m.sender JOIN rooms r ON r.id=m.room
                JOIN members me ON me.room=m.room AND me.peer=?
                WHERE (? IS NULL OR m.room=?) ORDER BY m.seq DESC LIMIT 31 OFFSET ?
                """, rs -> new Line(rs.getString(1),rs.getString(2),rs.getString(3),
                        rs.getString(4),rs.getString(5),rs.getString(6)),viewer,room,room,30L*page));
        boolean hasOlder = rows.size()>30;
        return new Page(List.copyOf(rows.subList(0,Math.min(30,rows.size())).reversed()),hasOlder);
    }
}
