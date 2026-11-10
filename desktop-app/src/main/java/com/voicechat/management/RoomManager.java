package com.voicechat.management;

import com.voicechat.common.api.RoomGateway;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;
import java.util.List;

/**
 * SHARED - CA NHOM: Quan ly phong, thanh vien va dong bo trang thai.
 * Hien tai chi la skeleton, chua dong bo qua mang.
 */
public final class RoomManager implements RoomGateway {
    @Override public RoomInfo createRoom(String name, String ownerPeerId) {
        throw new UnsupportedOperationException("TEAM TODO: create room + notify peers");
    }
    @Override public void joinRoom(String roomId, PeerInfo peer) {
        throw new UnsupportedOperationException("TEAM TODO: join room + sync peers");
    }
    @Override public void leaveRoom(String roomId, String peerId) {
        throw new UnsupportedOperationException("TEAM TODO: leave room + sync peers");
    }
    @Override public List<PeerInfo> getMembers(String roomId) {
        throw new UnsupportedOperationException("TEAM TODO: query room members");
    }
}
