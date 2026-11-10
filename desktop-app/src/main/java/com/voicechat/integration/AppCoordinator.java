package com.voicechat.integration;

import com.voicechat.common.api.HistoryStore;
import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.api.RoomGateway;
import com.voicechat.common.api.VoiceGateway;
import com.voicechat.chat.ChatController;

/**
 * SHARED - CA NHOM: Thanh phan ghep network + voice + chat + management voi UI.
 * Chua duoc noi vao giao dien demo; day la diem tich hop sau nay.
 */
public final class AppCoordinator {
    private final NetworkGateway network;
    private final VoiceGateway voice;
    private final ChatController chat;
    private final HistoryStore history;
    private final RoomGateway rooms;

    public AppCoordinator(NetworkGateway network, VoiceGateway voice,
                          ChatController chat, HistoryStore history, RoomGateway rooms) {
        this.network = network;
        this.voice = voice;
        this.chat = chat;
        this.history = history;
        this.rooms = rooms;
    }
}
