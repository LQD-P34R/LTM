package com.voicechat.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.BiConsumer;

public final class UdpService implements AutoCloseable {
    private final DatagramSocket socket;

    public UdpService(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    public int localPort() {
        return socket.getLocalPort();
    }

    public void send(InetSocketAddress target, byte[] bytes) throws IOException {
        if (bytes.length > 65_507) {
            throw new IOException("UDP payload exceeds 65507 bytes");
        }
        socket.send(new DatagramPacket(bytes, bytes.length, target));
    }

    void receive(BiConsumer<InetSocketAddress, byte[]> callback) throws IOException {
        byte[] buffer = new byte[65_507];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (!socket.isClosed()) {
            packet.setLength(buffer.length);
            socket.receive(packet);
            callback.accept((InetSocketAddress) packet.getSocketAddress(),
                    Arrays.copyOf(buffer, packet.getLength()));
        }
    }

    @Override
    public void close() {
        socket.close();
    }
}
