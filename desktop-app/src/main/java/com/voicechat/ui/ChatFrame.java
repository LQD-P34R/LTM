package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * TV3 – Bước 8: Cửa sổ chính của ứng dụng (viết lại hoàn toàn).
 *
 * <p>Bố cục: {@link SidebarPanel} bên trái + {@link CardLayout} nội dung bên phải
 * + status bar dưới cùng.</p>
 *
 * <p><b>Chế độ demo:</b> khởi động với mock peers và rooms, tin nhắn được hiển thị
 * cục bộ. Khi TV1 (network) và TV4 (chat) hoàn thiện, chỉ cần thay phần
 * {@code // TODO} trong các callback bằng lời gọi thực.</p>
 *
 * <p><b>Tích hợp sau:</b></p>
 * <pre>
 *   // Từ AppCoordinator, sau khi connect():
 *   network.onControlMessage(msg -> {
 *       switch (msg.type()) {
 *           case CHAT         -> SwingUtilities.invokeLater(() -> frame.onMessageReceived(...));
 *           case CALL_REQUEST -> frame.showIncomingCall(msg.senderId());
 *           case CALL_END     -> SwingUtilities.invokeLater(() -> frame.onCallEnded());
 *       }
 *   });
 * </pre>
 */
public final class ChatFrame extends JFrame implements SidebarPanel.SelectionListener {

    // ── Card keys ─────────────────────────────────────────────────
    private static final String CARD_WELCOME = "WELCOME";
    private static final String CARD_HISTORY = "HISTORY";
    private static final String CARD_CALL    = "CALL";

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG        = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_STATUS_BG = new Color(0x14, 0x1F, 0x33);
    private static final Color COLOR_STATUS_FG = new Color(0x8A, 0xA3, 0xC8);
    private static final Color COLOR_STATUS_DIM= new Color(0x4A, 0x62, 0x7A);

    private static final Font FONT_STATUS = new Font("SansSerif", Font.PLAIN, 11);

    // ── Layout ────────────────────────────────────────────────────
    private final CardLayout cardLayout  = new CardLayout();
    private final JPanel     contentPanel= new JPanel(cardLayout);

    // ── Core panels ───────────────────────────────────────────────
    private final SidebarPanel sidebarPanel;
    private final HistoryPanel historyPanel = new HistoryPanel();
    private final CallPanel    callPanel;

    // ── Lazy panels (tạo khi người dùng chọn lần đầu) ────────────
    private final Map<String, DirectChatPanel> directChats = new LinkedHashMap<>();
    private final Map<String, GroupChatPanel>  groupChats  = new LinkedHashMap<>();

    // ── State ─────────────────────────────────────────────────────
    private final String       localUsername;
    private final String       localUserId;   // demo: bằng username
    private final List<ChatMessage> chatHistory = new ArrayList<>();
    private final List<RoomInfo>    demoRooms   = new ArrayList<>();
    private PeerInfo               currentCallPeer = null;

    // ─────────────────────────────────────────────────────────────

    /**
     * Constructor duy nhất — chế độ demo (không cần gateway).
     * Khi AppCoordinator xong, bổ sung constructor nhận thêm các gateway.
     */
    public ChatFrame(String localUsername) {
        super("VoiceChat P2P — " + localUsername);
        this.localUsername = localUsername;
        this.localUserId   = localUsername;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 660);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);

        // Khởi tạo theo thứ tự phụ thuộc
        callPanel    = new CallPanel(buildCallListener());
        sidebarPanel = new SidebarPanel(this);

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebarPanel,       BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
        root.add(buildStatusBar(),   BorderLayout.SOUTH);
        setContentPane(root);

        loadDemoData();
    }

    // ── Content area (CardLayout) ─────────────────────────────────
    private JPanel buildContentArea() {
        contentPanel.setBackground(COLOR_BG);
        contentPanel.add(buildWelcomePanel(), CARD_WELCOME);
        contentPanel.add(historyPanel,        CARD_HISTORY);
        contentPanel.add(callPanel,           CARD_CALL);
        cardLayout.show(contentPanel, CARD_WELCOME);
        return contentPanel;
    }

    private JPanel buildWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel iconLbl = new JLabel("\uD83D\uDCAC", SwingConstants.CENTER); // 💬
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 56));
        iconLbl.setForeground(new Color(0xCC, 0xD5, 0xE0));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLbl = new JLabel("Ch\u00E0o m\u1EEBng, " + localUsername + "!", SwingConstants.CENTER);
        welcomeLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        welcomeLbl.setForeground(new Color(0x2C, 0x3E, 0x50));
        welcomeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLbl = new JLabel(
                "Ch\u1ECDn m\u1ED9t li\u00EAn h\u1EC7 b\u00EAn tr\u00E1i \u0111\u1EC3 b\u1EAFt \u0111\u1EA7u tr\u00F2 chuy\u1EC7n",
                SwingConstants.CENTER);
        hintLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        hintLbl.setForeground(new Color(0x7F, 0x8C, 0x8D));
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(iconLbl);
        inner.add(Box.createVerticalStrut(16));
        inner.add(welcomeLbl);
        inner.add(Box.createVerticalStrut(8));
        inner.add(hintLbl);

        panel.add(inner);
        return panel;
    }

    // ── Status bar ────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(COLOR_STATUS_BG);
        bar.setBorder(new EmptyBorder(5, 16, 5, 16));

        JLabel userLbl = new JLabel(
                "●  " + localUsername + "   \u2014  Ch\u1EBF \u0111\u1ED9 demo (ch\u01B0a k\u1EBFt n\u1ED1i m\u1EA1ng)");
        userLbl.setFont(FONT_STATUS);
        userLbl.setForeground(COLOR_STATUS_FG);
        bar.add(userLbl, BorderLayout.WEST);

        JLabel versionLbl = new JLabel("VoiceChat P2P v1.0-SNAPSHOT");
        versionLbl.setFont(FONT_STATUS);
        versionLbl.setForeground(COLOR_STATUS_DIM);
        bar.add(versionLbl, BorderLayout.EAST);

        return bar;
    }

    // ── SidebarPanel.SelectionListener ───────────────────────────

    @Override
    public void onPeerSelected(PeerInfo peer) {
        String key = "dm_" + peer.id();
        if (!directChats.containsKey(peer.id())) {
            DirectChatPanel panel = new DirectChatPanel(peer, localUserId, buildDmCallback());
            directChats.put(peer.id(), panel);
            contentPanel.add(panel, key);
        }
        cardLayout.show(contentPanel, key);
        directChats.get(peer.id()).focusComposer();
    }

    @Override
    public void onRoomSelected(RoomInfo room) {
        String key = "room_" + room.id();
        if (!groupChats.containsKey(room.id())) {
            GroupChatPanel panel = new GroupChatPanel(room, localUserId, buildGroupCallback());
            groupChats.put(room.id(), panel);
            contentPanel.add(panel, key);
        }
        cardLayout.show(contentPanel, key);
        groupChats.get(room.id()).focusComposer();
    }

    @Override
    public void onHistoryRequested() {
        historyPanel.loadHistory(chatHistory);
        cardLayout.show(contentPanel, CARD_HISTORY);
    }

    @Override
    public void onCreateGroupRequested() {
        String roomName = JOptionPane.showInputDialog(
                this,
                "Nh\u1EADp t\u00EAn nh\u00F3m:",      // "Nhập tên nhóm:"
                "T\u1EA1o nh\u00F3m m\u1EDBi",          // "Tạo nhóm mới"
                JOptionPane.PLAIN_MESSAGE
        );
        if (roomName != null && !roomName.isBlank()) {
            RoomInfo newRoom = new RoomInfo(
                    "room_" + System.currentTimeMillis(),
                    roomName.trim(),
                    localUserId
            );
            demoRooms.add(newRoom);
            sidebarPanel.setRooms(new ArrayList<>(demoRooms));
            // Tự động mở nhóm vừa tạo
            onRoomSelected(newRoom);
        }
    }

    // ── Callbacks ─────────────────────────────────────────────────

    private DirectChatPanel.ActionCallback buildDmCallback() {
        return new DirectChatPanel.ActionCallback() {

            @Override
            public void onSendMessage(PeerInfo peer, String text) {
                // Demo mode: hiển thị tin nhắn cục bộ
                ChatMessage msg = new ChatMessage(
                        UUID.randomUUID().toString(),
                        "dm_" + peer.id(),
                        localUserId,
                        text,
                        Instant.now()
                );
                chatHistory.add(msg);
                DirectChatPanel panel = directChats.get(peer.id());
                if (panel != null) {
                    SwingUtilities.invokeLater(() -> panel.appendMessage(msg));
                }
                /*
                 * TODO (TV1 + TV4 integration):
                 *   chatService.sendText(peer.id(), msg.roomId(), localUserId, text);
                 */
            }

            @Override
            public void onCallRequested(PeerInfo peer) {
                currentCallPeer = peer;
                callPanel.setState(CallPanel.CallState.RINGING, peer.username());
                cardLayout.show(contentPanel, CARD_CALL);
                /*
                 * TODO (TV1 + TV2 integration):
                 *   voiceGateway.startCall(peer.id());
                 *   network.sendControl(peer.id(), new ControlMessage(CALL_REQUEST, ...));
                 */
            }
        };
    }

    private GroupChatPanel.ActionCallback buildGroupCallback() {
        return (room, text) -> {
            ChatMessage msg = new ChatMessage(
                    UUID.randomUUID().toString(),
                    room.id(),
                    localUserId,
                    text,
                    Instant.now()
            );
            chatHistory.add(msg);
            GroupChatPanel panel = groupChats.get(room.id());
            if (panel != null) {
                SwingUtilities.invokeLater(() -> panel.appendMessage(msg));
            }
            /*
             * TODO (TV1 + TV4 group broadcast):
             *   for (PeerInfo member : roomGateway.getMembers(room.id())) {
             *       chatService.sendText(member.id(), room.id(), localUserId, text);
             *   }
             */
        };
    }

    private CallPanel.CallListener buildCallListener() {
        return new CallPanel.CallListener() {
            @Override
            public void onCallCancelled() {
                cardLayout.show(contentPanel, CARD_WELCOME);
                currentCallPeer = null;
                /*
                 * TODO (TV1 integration):
                 *   if (currentCallPeer != null)
                 *       network.sendControl(currentCallPeer.id(), new ControlMessage(CALL_END, ...));
                 */
            }

            @Override
            public void onCallEnded() {
                cardLayout.show(contentPanel, CARD_WELCOME);
                currentCallPeer = null;
                /*
                 * TODO (TV2 integration):
                 *   voiceGateway.stopCall();
                 */
            }

            @Override
            public void onMuteToggled(boolean muted) {
                /*
                 * TODO (TV2 integration):
                 *   voiceGateway.setMuted(muted);
                 */
            }
        };
    }

    // ── Public API (gọi sau khi tích hợp AppCoordinator) ─────────

    /**
     * Nhận tin nhắn đến từ mạng.
     * <b>Phải gọi trên EDT</b> (wrap bằng {@code SwingUtilities.invokeLater}).
     */
    public void onMessageReceived(ChatMessage msg) {
        chatHistory.add(msg);
        String roomId = msg.roomId();
        if (roomId.startsWith("dm_")) {
            String peerId = roomId.substring(3);
            DirectChatPanel panel = directChats.get(peerId);
            if (panel != null) panel.appendMessage(msg);
        } else {
            GroupChatPanel panel = groupChats.get(roomId);
            if (panel != null) panel.appendMessage(msg);
        }
    }

    /**
     * Cập nhật danh sách peer (gọi khi nhận HELLO / ROOM_MEMBERS từ mạng).
     * Thread-safe: tự wrap invokeLater.
     */
    public void setPeers(List<PeerInfo> peers) {
        SwingUtilities.invokeLater(() -> sidebarPanel.setPeers(peers));
    }

    /**
     * Cập nhật danh sách room.
     * Thread-safe: tự wrap invokeLater.
     */
    public void setRooms(List<RoomInfo> rooms) {
        SwingUtilities.invokeLater(() -> sidebarPanel.setRooms(rooms));
    }

    /**
     * Hiển thị dialog cuộc gọi đến.
     * Gọi từ network callback khi nhận {@code ControlMessage.Type.CALL_REQUEST}.
     * Thread-safe: tự wrap invokeLater.
     */
    public void showIncomingCall(String callerName) {
        SwingUtilities.invokeLater(() ->
            new IncomingCallDialog(this, callerName, new IncomingCallDialog.ResponseListener() {
                @Override
                public void onAccepted() {
                    callPanel.setState(CallPanel.CallState.IN_CALL, callerName);
                    cardLayout.show(contentPanel, CARD_CALL);
                    /*
                     * TODO (TV2 integration):
                     *   voiceGateway.startCall(callerPeerId);
                     *   network.sendControl(callerPeerId, CALL_ACCEPT msg);
                     */
                }
                @Override
                public void onRejected() {
                    /*
                     * TODO (TV1 integration):
                     *   network.sendControl(callerPeerId, CALL_REJECT msg);
                     */
                }
            }).setVisible(true)
        );
    }

    /** Reset giao diện khi cuộc gọi bị kết thúc từ xa (nhận CALL_END). */
    public void onCallEnded() {
        callPanel.setState(CallPanel.CallState.IDLE, "");
        if (currentCallPeer != null) {
            // Quay lại màn hình chat với peer đó
            String key = "dm_" + currentCallPeer.id();
            if (directChats.containsKey(currentCallPeer.id())) {
                cardLayout.show(contentPanel, key);
            } else {
                cardLayout.show(contentPanel, CARD_WELCOME);
            }
            currentCallPeer = null;
        } else {
            cardLayout.show(contentPanel, CARD_WELCOME);
        }
    }

    // ── Demo data ─────────────────────────────────────────────────
    private void loadDemoData() {
        List<PeerInfo> peers = List.of(
            new PeerInfo("peer_B", "User_B", "192.168.1.2", 5000, 5001),
            new PeerInfo("peer_C", "User_C", "192.168.1.3", 5000, 5001),
            new PeerInfo("peer_D", "User_D", "192.168.1.4", 5000, 5001)
        );
        demoRooms.addAll(List.of(
            new RoomInfo("room_1", "Nh\u00F3m h\u1ECDc nh\u00F3m", localUserId), // "Nhóm học nhóm"
            new RoomInfo("room_2", "Project Alpha",             localUserId)
        ));
        sidebarPanel.setPeers(peers);
        sidebarPanel.setRooms(new ArrayList<>(demoRooms));
    }
}
