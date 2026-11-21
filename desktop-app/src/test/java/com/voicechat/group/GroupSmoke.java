package com.voicechat.group;

import com.voicechat.network.PeerManager;
import com.voicechat.database.DatabaseManager;
import java.nio.file.*;
import static com.voicechat.group.GroupProtocol.*;

/** Executable integration test. Uses real TCP and a private temporary SQLite file. */
public final class GroupSmoke {
    private static void check(boolean ok,String label) {
        if (!ok) throw new AssertionError(label);
    }
    private static GroupClient connect(String id,int port) throws Exception {
        var wire = new PeerManager(id);
        var client = new GroupClient(id,wire);
        wire.connect("127.0.0.1",port);
        return client;
    }
    private static void rejected(GroupClient client,String action,String room,String value) throws Exception {
        try { client.request(action,room,value,0); }
        catch (java.io.IOException expected) { return; }
        throw new AssertionError(action + " should fail");
    }
    private static PeerManager start(Path path) {
        var wire = new PeerManager(HOST);
        wire.onControlMessage(new GroupHost(wire,new DatabaseManager(path.toString()))::receive);
        return wire;
    }
    public static void main(String[] args) throws Exception {
        Path db = Files.createTempFile("ltm-group-test-", ".db");
        String room;
        var server = start(db);
        try {
            int port = server.startListening(0,0);
            try (var a=connect("a",port); var b=connect("b",port); var c=connect("c",port)) {
                a.request("login",null,"Tùng",0);
                rejected(b,"login",null,"TÙNG");
                b.request("login",null,"Nga",0);
                c.request("login",null,"Sơn",0);
                room = a.request("create",null,"Nhóm LTM",0).room();
                rejected(c,"send",room,"ngoài phòng");
                rejected(b,"join","missing","");
                b.request("join",room,"",0);
                b.request("join",room,"",0);
                c.request("join",room,"",0);
                rejected(a,"send",room,"   ");
                a.request("send",room,"Chào nhóm 'LTM' 👋",0);
                b.request("send",room,"Đã nhận!",0);
                c.request("rename",room,"Minh Sơn",0);
                var view = c.request("view",room,"",0);
                check(view.members().size()==3 && view.members().stream().anyMatch(peer -> peer.username().equals("Minh Sơn")),"membership");
                check(view.history().size()==2 && view.history().get(0).text().contains("👋"),"group history");
                c.request("leave",room,"",0);
                rejected(c,"send",room,"đã rời");
                check(c.request("view",room,"",0).history().isEmpty(),"outsider history");
                check(c.request("history",null,"",0).history().isEmpty(),"global history membership");
                String other = b.request("create",null,"Khác",0).room();
                check(b.request("view",other,"",0).history().isEmpty(),"room isolation");
                b.request("send",other,"Riêng phòng khác",0);
                check(a.request("history",null,"",0).history().size()==2,"history isolates rooms");
                for (int i=0;i<30;i++) a.request("send",room,"Tin "+i,0);
                check(a.request("view",room,"",1).history().size()==2,"older page");
            }
        } finally { server.disconnect(); }
        server = start(db);
        try {
            int port=server.startListening(0,0);
            try (var c=connect("c",port); var a=connect("a",port)) {
                check(c.request("login",null,"Sơn",0).username().equals("Minh Sơn"),"persistent name");
                a.request("login",null,"Tùng",0);
                var v=a.request("view",room,"",0);
                check(v.members().size()==2 && v.history().size()==30,"persistent room and history");
            }
        } finally { server.disconnect(); Files.deleteIfExists(db); }
        System.out.println("PASS: 3 TCP clients, SQLite, usernames, membership, delivery, paging, restart.");
    }
}
