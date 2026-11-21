package com.voicechat.group;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import static com.voicechat.group.GroupProtocol.*;

public final class GroupClient implements AutoCloseable {
    private final String id;
    private final NetworkGateway wire;
    private final ConcurrentMap<String,CompletableFuture<Reply>> pending = new ConcurrentHashMap<>();
    public GroupClient(String id,NetworkGateway wire) {
        this.id = id;
        this.wire = wire;
        wire.onControlMessage(this::receive);
    }
    public String localId() { return id; }

    public static GroupClient connect(String profile,String host,int port) throws Exception {
        var prefs = java.util.prefs.Preferences.userNodeForPackage(GroupClient.class);
        String key = UUID.nameUUIDFromBytes(profile.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        String id = prefs.get(key,null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.put(key,id);
            prefs.flush();
        }
        var manager = new com.voicechat.network.PeerManager(id);
        var client = new GroupClient(id,manager);
        try {
            manager.connect(host,port);
            return client;
        } catch (Exception e) { client.close(); throw e; }
    }

    private void receive(ControlMessage m) {
        if (!HOST.equals(m.senderId()) || !id.equals(m.targetId()) || m.type()!=ControlMessage.Type.ROOM_MEMBERS) return;
        try {
            Reply reply = JSON.readValue(m.payload(),Reply.class);
            var future = pending.get(reply.id());
            if (future != null) future.complete(reply);
        } catch (IOException e) { System.err.println("Phản hồi sai định dạng: " + e.getMessage()); }
    }
    public View request(String action,String room,String value,int page) throws Exception {
        String key = UUID.randomUUID().toString();
        var future = new CompletableFuture<Reply>();
        pending.put(key,future);
        try {
            wire.sendControl(HOST,new ControlMessage(type(action),id,HOST,room,
                    JSON.writeValueAsString(new Request(key,action,value,page)),Instant.now()));
            Reply reply;
            try { reply = future.get(6,TimeUnit.SECONDS); }
            catch (TimeoutException e) { throw new IOException("Máy chủ chưa xác nhận. Hãy tải lại phòng để kiểm tra.",e); }
            if (reply.error()!=null) throw new IOException(reply.error());
            return reply.view();
        } finally { pending.remove(key); }
    }
    public void close() { wire.disconnect(); }
}
