package com.voicechat.integration;

import com.voicechat.audio.VoiceService;
import com.voicechat.network.PeerManager;
import com.voicechat.ui.ChatFrame;
import com.voicechat.chat.ChatService;
import com.voicechat.database.DatabaseManager;
import com.voicechat.database.HistoryRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Khoi tao mot phien desktop tu LoginFrame; duoc goi tren EDT. */
public final class DesktopSession implements AutoCloseable {
    private final PeerManager network;
    private final CallCoordinator calls;
    private final DirectChatCoordinator direct;
    private final ChatFrame frame;
    private boolean closed;

    public DesktopSession(String username) {
        requireEdt();
        if (Objects.requireNonNull(username).isBlank()) {
            throw new IllegalArgumentException("Tên người dùng không được rỗng");
        }
        HistoryRepository history = directHistory(username);
        network = new PeerManager(username);
        VoiceService voice = new VoiceService(network);
        frame = new ChatFrame(username, false);
        calls = new CallCoordinator(network, voice, username, frame);
        direct = new DirectChatCoordinator(
                username,
                new ChatService(network, history),
                frame);

        calls.onChatMessage(direct::receive);
        frame.bindDirectChat(direct);
        frame.bindCalls(calls);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                close();
            }
        });
    }

    public void open(int localTcpPort, int localUdpPort, boolean waitForPeer,
            String peerHost, int peerTcpPort,
            Runnable onReady, Consumer<Exception> onFailure) {
        requireEdt();
        Objects.requireNonNull(onReady);
        Objects.requireNonNull(onFailure);
        if (closed)
            throw new IllegalStateException("Phiên đã đóng");

        calls.open(() -> {
            network.startListening(localTcpPort, localUdpPort);
            System.out.printf("LISTEN %s TCP=%d UDP=%d%n",
                    network.localPeerId(), localTcpPort, localUdpPort);
            if (!waitForPeer)
                network.connect(peerHost, peerTcpPort);
        }, () -> {
            if (closed)
                return;
            frame.setVisible(true);
            onReady.run();
        }, error -> {
            if (closed)
                return;
            close();
            onFailure.accept(error);
        });
    }

    private static HistoryRepository directHistory(String username) {
        try {
            Path directory = Path.of(
                    System.getProperty("user.home"),
                    ".voicechat",
                    "direct");

            Files.createDirectories(directory);

            String profile = UUID.nameUUIDFromBytes(
                    username.getBytes(StandardCharsets.UTF_8)).toString();

            Path file = directory.resolve(profile + ".db");

            return new HistoryRepository(
                    new DatabaseManager(file.toString()));
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Không thể tạo thư mục lịch sử chat.", ex);
        }
    }

    @Override
    public void close() {
        requireEdt();
        if (closed)
            return;
        closed = true;
        direct.close();
        frame.dispose();
        calls.close();
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("DesktopSession phải được gọi trên EDT");
        }
    }
}
