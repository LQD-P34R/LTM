package com.voicechat.chat;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;

/** TV4: logic gui chat; KHONG chua Swing / UI. */
public final class ChatController {
    private final NetworkGateway network;

    public ChatController(NetworkGateway network) {
        this.network = network;
    }

    public void sendMessage(String targetPeerId, ControlMessage message) throws IOException {
        if (message.type() != ControlMessage.Type.CHAT) {
            throw new IllegalArgumentException("Only CHAT message is supported here");
        }
        network.sendControl(targetPeerId, message);
    }
}
