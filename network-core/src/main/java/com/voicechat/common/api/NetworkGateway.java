package com.voicechat.common.api;

import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.Set;

/** Hop dong TV1 trien khai, cac thanh vien khac su dung. */
public interface NetworkGateway {
    void connect(String host, int tcpPort) throws IOException;

    void sendControl(String peerId, ControlMessage message) throws IOException;

    void sendVoice(String peerId, byte[] packet) throws IOException;

    void onControlMessage(Consumer<ControlMessage> callback);

    void onVoicePacket(BiConsumer<String, byte[]> callback);

    void disconnect();

    Set<String> connectedPeerIds();

    void onPeerConnected(Consumer<String> callback);

    void onPeerDisconnected(Consumer<String> callback);
}
