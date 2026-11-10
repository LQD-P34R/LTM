package com.voicechat.common.api;

import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;
import java.util.List;

/** SHARED: Quan ly phong do ca nhom trien khai va dong bo qua TV1. */
public interface RoomGateway {
    RoomInfo createRoom(String name, String ownerPeerId);
    void joinRoom(String roomId, PeerInfo peer);
    void leaveRoom(String roomId, String peerId);
    List<PeerInfo> getMembers(String roomId);
}
