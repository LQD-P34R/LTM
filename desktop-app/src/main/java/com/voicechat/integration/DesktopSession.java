package com.voicechat.integration;

import com.voicechat.audio.VoiceService;
import com.voicechat.network.PeerManager;
import com.voicechat.ui.ChatFrame;
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
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private final PeerManager network;
    private final AppCoordinator app;
    private final ChatFrame frame;
    private boolean closed;

    // ===== Khởi tạo phiên desktop =====
    // Ghép lịch sử, mạng, âm thanh và cửa sổ chat; gắn xử lý đóng cửa sổ.
    public DesktopSession(String username) {
        requireEdt();
        if (Objects.requireNonNull(username).isBlank()) {
            throw new IllegalArgumentException("Tên người dùng không được rỗng");
        }
        HistoryRepository history = directHistory(username);
        network = new PeerManager(username);
        VoiceService voice = new VoiceService(network);
        frame = new ChatFrame(username);
        app = new AppCoordinator(network, voice, username, frame, history);
        frame.bindApp(app);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                close();
            }
        });
    }

    // ===== Mở phiên P2P =====
    // Mở cổng TCP/UDP, tùy chọn kết nối peer rồi hiển thị cửa sổ khi sẵn sàng.
    public void open(int localTcpPort, int localUdpPort, boolean waitForPeer,
            String peerHost, int peerTcpPort,
            Runnable onReady, Consumer<Exception> onFailure) {
        requireEdt();
        Objects.requireNonNull(onReady);
        Objects.requireNonNull(onFailure);
        if (closed)
            throw new IllegalStateException("Phiên đã đóng");

        app.calls().open(() -> {
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

    // ===== Chuẩn bị kho lịch sử theo hồ sơ =====
    // Tạo tệp SQLite riêng dưới thư mục .voicechat/direct bằng UUID từ tên người dùng.
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

    // ===== Đóng phiên và kiểm tra luồng Swing =====
    // Thu hồi cửa sổ/dịch vụ; yêu cầu các thao tác phiên chạy trên EDT.
    @Override
    public void close() {
        requireEdt();
        if (closed)
            return;
        closed = true;
        frame.dispose();
        app.close();
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("DesktopSession phải được gọi trên EDT");
        }
    }
}
