package com.voicechat.group;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.*;
import com.voicechat.database.*;
import com.voicechat.management.*;
import com.voicechat.network.PeerManager;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import static com.voicechat.group.GroupProtocol.*;

public final class GroupHost {
    private final UserManager users;
    private final RoomManager rooms;
    private final HistoryRepository history;
    private final DatabaseManager db;
    private final NetworkGateway wire;

    public GroupHost(NetworkGateway wire, DatabaseManager db) {
        this.wire = wire;
        this.db = db;
        users = new UserManager(db);
        rooms = new RoomManager(db);
        history = new HistoryRepository(db);
    }

    public synchronized void receive(ControlMessage m) {
        if (!HOST.equals(m.targetId()) || m.senderId() == null) return;
        String requestId = "";
        Reply reply;
        try {
            Request q = JSON.readValue(m.payload(),Request.class);
            requestId = UUID.fromString(q.id()).toString();
            if (type(q.action()) != m.type()) throw new IllegalArgumentException("Sai loại yêu cầu.");
            reply = new Reply(requestId,null,apply(m.senderId(),m.roomId(),q));
        } catch (Exception e) { reply = new Reply(requestId,Objects.toString(e.getMessage(),"Yêu cầu không hợp lệ."),null); }
        try {
            wire.sendControl(m.senderId(),new ControlMessage(ControlMessage.Type.ROOM_MEMBERS,HOST,
                    m.senderId(),m.roomId(),JSON.writeValueAsString(reply),Instant.now()));
        } catch (IOException e) { System.err.println("Không gửi được phản hồi: " + e.getMessage()); }
    }

    private View apply(String id,String room,Request q) {
        String username = db.transaction(c -> DatabaseManager.query(c,
                "SELECT username FROM users WHERE id=?",r -> r.getString(1),id).stream().findFirst().orElse(null));
        if (username == null && !q.action().equals("login"))
            throw new IllegalArgumentException("Hãy đăng nhập trước.");
        if (username == null || q.action().equals("rename")) {
            username = DatabaseManager.label(q.value(),40);
            users.addPeer(new PeerInfo(id,username,"",0,0));
        }
        if (room == null && Set.of("join","leave","send").contains(q.action()))
            throw new IllegalArgumentException("Hãy chọn một phòng trước.");
        switch (q.action()) {
            case "login","rename","view","history" -> { }
            case "create" -> room = rooms.createRoom(q.value(),id).id();
            case "join" -> rooms.joinRoom(room,new PeerInfo(id,"","",0,0));
            case "leave" -> rooms.leaveRoom(room,id);
            case "send" -> {
                if (!isMember(room,id)) throw new IllegalArgumentException("Bạn cần tham gia phòng trước khi gửi.");
                history.save(new ChatMessage(id + "/" + q.id(),room,id,q.value(),Instant.now()));
            }
            default -> throw new IllegalArgumentException("Thao tác không hợp lệ.");
        }
        List<PeerInfo> peers = room == null ? List.of() : rooms.getMembers(room);
        boolean joined = peers.stream().anyMatch(p -> p.id().equals(id));
        boolean allRooms = q.action().equals("history");
        var page = (joined || allRooms) ? history.readPage(id,allRooms ? null : room,q.page())
                : new HistoryRepository.Page(List.of(),false);
        return new View(username,rooms.listRooms(),room,joined,
                joined ? peers : List.of(),page.lines(),page.hasOlder());
    }

    private boolean isMember(String room,String id) {
        return rooms.getMembers(room).stream().anyMatch(p -> p.id().equals(id));
    }
    public static void main(String[] args) throws Exception {
        int port = args.length == 0 ? 5000 : Integer.parseInt(args[0]);
        var wire = new PeerManager(HOST);
        var host = new GroupHost(wire,new DatabaseManager());
        wire.onControlMessage(host::receive);
        Runtime.getRuntime().addShutdownHook(new Thread(wire::disconnect));
        System.out.println("Máy chủ phòng chat đang nghe TCP " + wire.startListening(port,0));
        new java.util.concurrent.CountDownLatch(1).await();
    }
}
