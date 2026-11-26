package com.voicechat.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** One transaction per operation. Foreign keys are enabled on every connection. */
public final class DatabaseManager {
    // ===== Dữ liệu và các kiểu hỗ trợ =====
    // Các trường lưu cấu hình/trạng thái; các kiểu lồng nhau mô tả dữ liệu và hợp đồng dùng trong lớp.
    private final String file;
    public interface Work<T> {

        // ===== Công việc trong giao dịch =====
        // Nhận Connection của giao dịch; lỗi SQLException làm giao dịch bị rollback.
        T run(Connection c) throws SQLException;
    }
    public interface Row<T> {

        // ===== Ánh xạ một dòng SQL =====
        // Chuyển dòng hiện tại trong ResultSet thành một đối tượng dữ liệu.
        T read(ResultSet r) throws SQLException;
    }

    // ===== Khởi tạo SQLite và cấu trúc bảng =====
    // Chọn tệp database rồi tạo các bảng/chỉ mục nếu chưa tồn tại.
    public DatabaseManager() { this(System.getProperty("voicechat.db", "voicechat.db")); }

    public DatabaseManager(String file) {
        this.file = file;
        transaction(c -> {
            try (var s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY, username TEXT NOT NULL, name_key TEXT NOT NULL UNIQUE, host TEXT, tcp INTEGER, udp INTEGER, active INTEGER NOT NULL)");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS rooms(id TEXT PRIMARY KEY, name TEXT NOT NULL, owner TEXT NOT NULL REFERENCES users(id))");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS members(room TEXT REFERENCES rooms(id), peer TEXT REFERENCES users(id), PRIMARY KEY(room,peer))");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS messages(seq INTEGER PRIMARY KEY, id TEXT NOT NULL UNIQUE, room TEXT NOT NULL, sender TEXT NOT NULL, body TEXT NOT NULL, sent_at TEXT NOT NULL)");
                s.executeUpdate("CREATE INDEX IF NOT EXISTS messages_room_seq ON messages(room,seq)");
            }
            return null;
        });
    }

    // ===== Kết nối và giao dịch =====
    // Bật khóa ngoại, đặt thời gian chờ; commit khi thành công và rollback khi có lỗi.
    public static Connection open(String file) throws SQLException {
        var c = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (var s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA busy_timeout=5000");
        } catch (SQLException e) { c.close(); throw e; }
        return c;
    }

    public <T> T transaction(Work<T> work) {
        try (var c = open(file)) {
            c.setAutoCommit(false);
            try {
                T value = work.run(c);
                c.commit();
                return value;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new IllegalStateException("SQLite: " + e.getMessage(), e); }
    }

    // ===== Thực thi SQL có tham số =====
    // Dùng PreparedStatement để gắn tham số, cập nhật dữ liệu hoặc ánh xạ các dòng kết quả.
    private static PreparedStatement prepare(Connection c, String sql, Object[] args) throws SQLException {
        var p = c.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) p.setObject(i+1, args[i]);
        return p;
    }

    public static int update(Connection c, String sql, Object... args) throws SQLException {
        try (var p = prepare(c, sql, args)) { return p.executeUpdate(); }
    }

    public static <T> List<T> query(Connection c, String sql, Row<T> row, Object... args) throws SQLException {
        try (var p = prepare(c, sql, args); var r = p.executeQuery()) {
            var result = new ArrayList<T>();
            while (r.next()) result.add(row.read(r));
            return result;
        }
    }

    // ===== Chuẩn hóa dữ liệu nhập =====
    // Loại khoảng trắng ở hai đầu và kiểm tra nội dung không rỗng, không quá dài.
    public static String label(String value, int max) {
        if (value == null || value.isBlank() || value.strip().length() > max)
            throw new IllegalArgumentException("Nội dung phải dài từ 1 đến " + max + " ký tự.");
        return value.strip();
    }
}
