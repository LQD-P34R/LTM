package com.voicechat.network;

import com.voicechat.common.model.ControlMessage;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Run B first, then A, in separate application configurations. */
public final class PeerDemo {
    private static final String FIRST = "Chào bạn, tiếng Việt!";
    private static final String SECOND = "Tin nhắn thứ hai: kết nối ổn định.";
    private static final String REPLY = "Đã nhận đủ hai tin nhắn.";
    private static final byte[] VOICE = { 1, 2, 3 };

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || !(args[0].equals("A") || args[0].equals("B"))
                || (args[0].equals("A") ? args.length != 5 : args.length != 3)) {
            throw new IllegalArgumentException("Usage: B tcpPort udpPort | A tcpPort udpPort host remoteTcpPort");
        }
        String id = args[0];
        boolean caller = id.equals("A");
        int tcp = Integer.parseInt(args[1]);
        int udp = Integer.parseInt(args[2]);
        var manager = new PeerManager(id);
        manager.onPeerConnected(peerId -> System.out.println("CONNECTED: " + peerId));

        manager.onPeerDisconnected(peerId -> System.out.println("DISCONNECTED: " + peerId));
        var finished = new CountDownLatch(caller ? 2 : 3);
        var received = new java.util.concurrent.LinkedBlockingQueue<String>();
        manager.onControlMessage(message -> {
            if (message.type() == ControlMessage.Type.CHAT && id.equals(message.targetId())
                    && (caller ? "B" : "A").equals(message.senderId()) && message.sentAt() != null
                    && message.payload() != null) {
                received.add(message.payload());
                System.out.println("CHAT " + message.senderId() + ": " + message.payload());
                finished.countDown();
            }
        });
        manager.onVoicePacket((sender, bytes) -> {
            if ((caller ? "B" : "A").equals(sender) && Arrays.equals(VOICE, bytes)) {
                System.out.println("UDP " + sender + ": " + Arrays.toString(bytes));
                finished.countDown();
            }
        });
        try {
            tcp = manager.startListening(tcp, udp);
            System.out.println("LISTEN " + id + " TCP=" + tcp + " UDP=" + udp);
            if (caller) {
                manager.connect(args[3], Integer.parseInt(args[4]));
                System.out.println("HELLO OK");
                send(manager, "B", FIRST);
                send(manager, "B", SECOND);
                manager.sendVoice("B", VOICE);
            }
            if (!finished.await(caller ? 10 : 60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for CHAT/UDP");
            }
            if (caller) {
                if (!REPLY.equals(received.poll())) {
                    throw new IllegalStateException("Unexpected reply");
                }
                send(manager, "B", "ACK");
            } else {
                if (!FIRST.equals(received.poll()) || !SECOND.equals(received.poll())) {
                    throw new IllegalStateException("Unexpected CHAT content or order");
                }
                send(manager, "A", REPLY);
                manager.sendVoice("A", VOICE);
                if (!"ACK".equals(received.poll(10, TimeUnit.SECONDS))) {
                    throw new IllegalStateException("Missing ACK");
                }
            }
            System.out.println("PASS CHAT/UDP " + id);
        } finally {
            manager.disconnect();
        }
        try {
            manager.startListening(tcp, udp);
            System.out.println("PASS REBIND " + id);
        } finally {
            manager.disconnect();
        }
    }

    private static void send(PeerManager manager, String target, String text) throws Exception {
        manager.sendControl(target, new ControlMessage(ControlMessage.Type.CHAT,
                manager.localPeerId(), target, null, text, Instant.now()));
    }
}
