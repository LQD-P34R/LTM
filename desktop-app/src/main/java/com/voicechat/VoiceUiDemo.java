package com.voicechat;

import com.voicechat.audio.VoiceService;
import com.voicechat.integration.CallCoordinator;
import com.voicechat.network.PeerManager;
import com.voicechat.ui.ChatFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Diem chay UI A/B; khoi tao mang that, khong qua LoginFrame demo. */
public final class VoiceUiDemo {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2
                || !(args[0].equals("A") || args[0].equals("B"))) {
            throw new IllegalArgumentException("Tham số: B hoặc A [IP của B]");
        }
        boolean caller = args[0].equals("A");
        String localId = args[0];
        String host = args.length == 2 ? args[1] : "127.0.0.1";
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            PeerManager network = new PeerManager(localId);
            VoiceService voice = new VoiceService(network);
            ChatFrame frame = new ChatFrame(localId, false);
            CallCoordinator calls = new CallCoordinator(network, voice, localId, frame);
            frame.bindCalls(calls);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    calls.close();
                }
            });
            frame.setVisible(true);
            calls.open(() -> {
                int tcpPort = caller ? 5101 : 5100;
                int udpPort = caller ? 6101 : 6100;
                network.startListening(tcpPort, udpPort);
                System.out.printf("LISTEN %s TCP=%d UDP=%d%n", localId, tcpPort, udpPort);
                if (caller) network.connect(host, 5100);
            });
        });
    }
}
