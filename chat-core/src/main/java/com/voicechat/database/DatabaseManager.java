package com.voicechat.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** One transaction per operation. Foreign keys are enabled on every connection. */
public final class DatabaseManager {
    private final String file;
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
    public static Connection open(String file) throws SQLException {
        var c = DriverManager.getConnection("jdbc:sqlite:" + file);
        try (var s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA busy_timeout=5000");
        } catch (SQLException e) { c.close(); throw e; }
        return c;
    }
    public interface Work<T> { T run(Connection c) throws SQLException; }
    public interface Row<T> { T read(ResultSet r) throws SQLException; }
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
    public static String label(String value, int max) {
        if (value == null || value.isBlank() || value.strip().length() > max)
            throw new IllegalArgumentException("Nội dung phải dài từ 1 đến " + max + " ký tự.");
        return value.strip();
    }
}
