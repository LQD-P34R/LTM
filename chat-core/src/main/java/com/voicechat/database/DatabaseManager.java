package com.voicechat.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** TV4: them SQLite JDBC dependency vao pom.xml truoc khi ket noi. */
public final class DatabaseManager {
    private DatabaseManager() { }
    public static Connection open(String filePath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }
}
