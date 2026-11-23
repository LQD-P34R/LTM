package com.voicechat.network;

public class TcpListenDemo {
    public static void main(String[] args) throws Exception {
        var tcp = new TcpService();

        try (var server = tcp.startListening(5000)) {
            System.out.println("Dang nghe cong " + server.getLocalPort());
            System.out.println("Nhan Enter de dung");
            System.in.read();
        }
    }
}