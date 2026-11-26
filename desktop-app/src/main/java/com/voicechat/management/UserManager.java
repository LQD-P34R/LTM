package com.voicechat.management;

import com.voicechat.common.model.PeerInfo;
import com.voicechat.database.DatabaseManager;
import java.sql.*;
import java.util.*;
import static com.voicechat.database.DatabaseManager.*;

public final class UserManager {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    final DatabaseManager db;

    // ===== Khởi tạo quản lý người dùng =====
    // Nhận database dùng chung hoặc tạo database mặc định.
    public UserManager() { this(new DatabaseManager()); }

    public UserManager(DatabaseManager db) { this.db = db; }

    // ===== Cập nhật hồ sơ và trạng thái online =====
    // Chuẩn hóa tên, ngăn trùng tên và đánh dấu người dùng hoạt động/ngừng hoạt động.
    public void addPeer(PeerInfo peer) {
        String name = label(peer.username(), 40);
        String key = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        db.transaction(c -> {
            if (!query(c,"SELECT id FROM users WHERE name_key=? AND id<>?",r -> r.getString(1),key,peer.id()).isEmpty())
                throw new IllegalArgumentException("Tên này đã có người dùng.");
            return update(c,"INSERT INTO users VALUES(?,?,?,?,?,?,1) ON CONFLICT(id) DO UPDATE SET username=excluded.username,name_key=excluded.name_key,host=excluded.host,tcp=excluded.tcp,udp=excluded.udp,active=1",
                    peer.id(),name,key,peer.host(),peer.tcpPort(),peer.udpPort());
        });
    }

    public void removePeer(String id) { db.transaction(c -> update(c,"UPDATE users SET active=0 WHERE id=?",id)); }

    // ===== Tra cứu và ánh xạ người dùng =====
    // Lấy các peer đang hoạt động và chuyển dòng ResultSet thành PeerInfo.
    public List<PeerInfo> listPeers() {
        return db.transaction(c -> query(c,"SELECT * FROM users WHERE active=1 ORDER BY name_key",UserManager::peer));
    }

    static PeerInfo peer(ResultSet r) throws SQLException {
        return new PeerInfo(r.getString("id"),r.getString("username"),r.getString("host"),r.getInt("tcp"),r.getInt("udp"));
    }
}
