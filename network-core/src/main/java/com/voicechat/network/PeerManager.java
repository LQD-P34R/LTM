package com.voicechat.network;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ControlMessage;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** TV1: quan ly peers, TCP/UDP, ket noi va callbacks. */
public final class PeerManager implements NetworkGateway {
    @Override public void connect(String host, int tcpPort) throws IOException {
        throw new UnsupportedOperationException("TV1 TODO: TCP connect");
    }
    @Override public void sendControl(String peerId, ControlMessage message) throws IOException {
        throw new UnsupportedOperationException("TV1 TODO: send TCP framed JSON");
    }
    @Override public void sendVoice(String peerId, byte[] packet) throws IOException {
        throw new UnsupportedOperationException("TV1 TODO: send UDP audio");
    }
    @Override public void onControlMessage(Consumer<ControlMessage> callback) {
        throw new UnsupportedOperationException("TV1 TODO: subscribe to control messages");
    }
    @Override public void onVoicePacket(BiConsumer<String, byte[]> callback) {
        throw new UnsupportedOperationException("TV1 TODO: subscribe to voice packets");
    }
    @Override public void disconnect() {
        // TV1 TODO: close connections and threads safely
    }
}
