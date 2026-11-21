package com.voicechat.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voicechat.common.model.*;
import java.util.List;
import com.voicechat.database.HistoryRepository.Line;

/** Payloads are JSON inside TV1's existing framed ControlMessage. */
public final class GroupProtocol {
    static final String HOST = "ltm-room-host";
    static final ObjectMapper JSON = new ObjectMapper();
    public record Request(String id,String action,String value,int page) {}
    public record View(String username,List<RoomInfo> rooms,String room,boolean joined,List<PeerInfo> members,List<Line> history,boolean hasOlder) {}
    public record Reply(String id,String error,View view) {}
    static ControlMessage.Type type(String action) {
        return switch(action) {
            case "create" -> ControlMessage.Type.ROOM_CREATE;
            case "join" -> ControlMessage.Type.ROOM_JOIN;
            case "leave" -> ControlMessage.Type.ROOM_LEAVE;
            case "send" -> ControlMessage.Type.CHAT;
            default -> ControlMessage.Type.ROOM_MEMBERS;
        };
    }
    private GroupProtocol() {}
}
