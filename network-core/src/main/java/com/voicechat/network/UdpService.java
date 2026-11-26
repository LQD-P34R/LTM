package com.voicechat.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.BiConsumer;

public final class UdpService implements AutoCloseable {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private final DatagramSocket socket;

    // ===== Khởi tạo cổng UDP =====
    // Mở socket datagram và cung cấp cổng thực tế do hệ điều hành cấp.
    public UdpService(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    public int localPort() {
        return socket.getLocalPort();
    }

    // ===== Gửi và nhận datagram =====
    // Gửi mảng byte đến địa chỉ đích; vòng nhận chuyển bản sao dữ liệu cho callback.
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

    // ===== Đóng socket UDP =====
    // Đóng socket để giải phóng cổng và kết thúc vòng nhận đang chờ.
    @Override
    public void close() {
        socket.close();
    }
}
