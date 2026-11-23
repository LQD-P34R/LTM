package com.voicechat.management;

import com.voicechat.common.api.RoomGateway;
import com.voicechat.common.model.*;
import com.voicechat.database.DatabaseManager;
import java.util.*;
import static com.voicechat.database.DatabaseManager.*;

public final class RoomManager implements RoomGateway {
    private final DatabaseManager db;
    public RoomManager() { this(new DatabaseManager()); }
    public RoomManager(DatabaseManager db) { this.db = db; }
    public RoomInfo createRoom(String name,String owner) {
        var room = new RoomInfo(UUID.randomUUID().toString(),label(name,80),owner);
        return db.transaction(c -> {
            update(c,"INSERT INTO rooms VALUES(?,?,?)",room.id(),room.name(),owner);
            update(c,"INSERT INTO members VALUES(?,?)",room.id(),owner);
            return room;
        });
    }
    public void joinRoom(String room,PeerInfo peer) {
        db.transaction(c -> update(c,"INSERT INTO members VALUES(?,?) ON CONFLICT DO NOTHING",room,peer.id()));
    }
    public void leaveRoom(String room,String id) {
        db.transaction(c -> update(c,"DELETE FROM members WHERE room=? AND peer=?",room,id));
    }
    public List<PeerInfo> getMembers(String room) {
        return db.transaction(c -> query(c,"SELECT u.* FROM users u JOIN members m ON m.peer=u.id WHERE m.room=? ORDER BY name_key",UserManager::peer,room));
    }
    public List<RoomInfo> listRooms() {
        return db.transaction(c -> query(c,"SELECT * FROM rooms ORDER BY name,id",r -> new RoomInfo(r.getString(1),r.getString(2),r.getString(3))));
    }
}
