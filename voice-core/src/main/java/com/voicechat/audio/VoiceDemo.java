package com.voicechat.audio;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.voicechat.network.PeerManager;

public final class VoiceDemo {

    public static void main(String[] args) throws Exception {
        if (args.length < 1
                || args.length > 2
                || !(args[0].equals("A") || args[0].equals("B"))) {
            throw new IllegalArgumentException(
                    "Cách chạy: B hoặc A [IP của B]");
        }

        boolean caller = args[0].equals("A");

        String localId = caller ? "A" : "B";
        String remoteId = caller ? "B" : "A";
        String remoteHost = args.length == 2 ? args[1] : "127.0.0.1";

        int tcpPort = caller ? 5101 : 5100;
        int udpPort = caller ? 6101 : 6100;

        PeerManager network = new PeerManager(localId);

        // VoiceService tự đăng ký callback nhận PCM.
        VoiceService voice = new VoiceService(network);

        CountDownLatch connected = new CountDownLatch(1);

        // Đăng ký sự kiện trước khi mở kết nối.
        network.onPeerConnected(peerId -> {
            if (remoteId.equals(peerId)) {
                System.out.println("CONNECTED: " + peerId);
                connected.countDown();
            }
        });

        network.onPeerDisconnected(peerId -> {
            if (remoteId.equals(peerId)) {
                voice.stopCall();

                System.out.println("DISCONNECTED: " + peerId);
                System.out.println("Nhập stop để thoát.");
            }
        });

        try (Scanner console = new Scanner(System.in)) {
            network.startListening(tcpPort, udpPort);

            System.out.printf(
                    "LISTEN %s TCP=%d UDP=%d%n",
                    localId, tcpPort, udpPort);

            if (caller) {
                System.out.println(
                        "Đang kết nối tới " + remoteHost + ":5100");

                network.connect(remoteHost, 5100);
            } else {
                System.out.println("Đang chờ A kết nối...");
            }

            // Chờ hoàn tất bắt tay, không dùng sleep để đoán.
            if (!connected.await(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Không kết nối được peer trong 60 giây");
            }

            voice.startCall(remoteId);

            System.out.println("Đã khởi động âm thanh với " + remoteId);
            System.out.println("Lệnh: mute / unmute / status / stop");

            while (console.hasNextLine()) {
                String command = console.nextLine().trim();

                switch (command) {
                    case "mute" -> {
                        voice.setMuted(true);
                        System.out.println("Đã yêu cầu tắt mic.");
                    }

                    case "unmute" -> {
                        voice.setMuted(false);
                        System.out.println("Đã yêu cầu bật mic.");
                    }

                    case "status" -> {
                        System.out.println(
                                "Peers: " + network.connectedPeerIds());
                        System.out.println(
                                "Muted: " + voice.isMuted());
                    }

                    case "stop" -> {
                        return;
                    }

                    default -> System.out.println(
                            "Lệnh: mute / unmute / status / stop");
                }
            }
        } finally {
            try {
                voice.stopCall();
            } finally {
                network.disconnect();
            }

            System.out.println("Đã đóng audio và mạng.");
        }
    }
}