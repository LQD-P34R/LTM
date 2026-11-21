package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * VoiceLink ChatFrame: Cửa sổ làm việc chính của ứng dụng.
 * <p>Bố cục 2 cột hiện đại: Sidebar bên trái rộng 260px nền trắng,
 * vùng nội dung bên phải (CardLayout) trên nền Slate dịu mắt,
 * kèm thanh trạng thái thanh lịch ở đáy.</p>
 */
public final class ChatFrame extends JFrame implements SidebarPanel.SelectionListener {

    private static final String CARD_WELCOME = "WELCOME";
    private static final String CARD_HISTORY = "HISTORY";
    private static final String CARD_CALL    = "CALL";

    private final CardLayout cardLayout   = new CardLayout();
    private final JPanel     contentPanel = new JPanel(cardLayout);

    private final SidebarPanel sidebarPanel;
    private final HistoryPanel historyPanel = new HistoryPanel();
    private final CallPanel    callPanel;

    private final Map<String, DirectChatPanel> directChats = new LinkedHashMap<>();
    private final Map<String, GroupChatPanel>  groupChats  = new LinkedHashMap<>();

    private final String            localUsername;
    private final String            localUserId;
    private final List<ChatMessage> chatHistory = new ArrayList<>();
    private final List<RoomInfo>    demoRooms   = new ArrayList<>();
    private PeerInfo                currentCallPeer = null;

    public ChatFrame(String localUsername) {
        super("VoiceLink P2P — " + localUsername);
        this.localUsername = localUsername;
        this.localUserId   = localUsername;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 700);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);

        callPanel    = new CallPanel(buildCallListener());
        sidebarPanel = new SidebarPanel(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.C_APP_BG);
        root.add(sidebarPanel,       BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
        root.add(buildStatusBar(),   BorderLayout.SOUTH);
        setContentPane(root);

        loadDemoData();
    }

    private JPanel buildContentArea() {
        contentPanel.setBackground(UiUtils.C_APP_BG);
        contentPanel.add(buildWelcomePanel(), CARD_WELCOME);
        contentPanel.add(historyPanel,        CARD_HISTORY);
        contentPanel.add(callPanel,           CARD_CALL);
        cardLayout.show(contentPanel, CARD_WELCOME);
        return contentPanel;
    }

    private JPanel buildWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiUtils.C_APP_BG);

        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 320));

        // Logo tròn
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_PRIMARY_LIGHT);
                g2.fillOval((getWidth() - 64) / 2, 0, 64, 64);
                g2.dispose();
            }
        };
        iconPanel.setLayout(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(460, 66));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLbl = new JLabel("\uD83D\uDCAC"); // 💬
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        iconLbl.setForeground(UiUtils.C_PRIMARY);
        iconPanel.add(iconLbl);
        card.add(iconPanel);
        card.add(Box.createVerticalStrut(16));

        JLabel welcomeLbl = new JLabel("Chào mừng, " + localUsername + "!", SwingConstants.CENTER);
        welcomeLbl.setFont(UiUtils.F_TITLE_LG);
        welcomeLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        welcomeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(welcomeLbl);
        card.add(Box.createVerticalStrut(8));

        JLabel hintLbl = new JLabel(
                "Chọn một liên hệ từ danh sách bên trái để bắt đầu nhắn tin hoặc gọi thoại.",
                SwingConstants.CENTER);
        hintLbl.setFont(UiUtils.F_BODY);
        hintLbl.setForeground(UiUtils.C_TEXT_MUTED);
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hintLbl);

        panel.add(card);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiUtils.C_CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, UiUtils.C_BORDER),
                new EmptyBorder(6, 16, 6, 16)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(UiUtils.C_ONLINE);

        JLabel statusText = new JLabel(localUsername + "  •  Sẵn sàng kết nối P2P");
        statusText.setFont(UiUtils.F_SMALL);
        statusText.setForeground(UiUtils.C_TEXT_MUTED);

        left.add(dot);
        left.add(statusText);
        bar.add(left, BorderLayout.WEST);

        JLabel ver = new JLabel("VoiceLink P2P v1.0");
        ver.setFont(UiUtils.F_SMALL);
        ver.setForeground(UiUtils.C_TEXT_HINT);
        bar.add(ver, BorderLayout.EAST);

        return bar;
    }

    // ── SidebarPanel.SelectionListener ───────────────────────────
    @Override
    public void onPeerSelected(PeerInfo peer) {
        String cardKey = "dm_" + peer.id();
        if (!directChats.containsKey(peer.id())) {
            DirectChatPanel panel = new DirectChatPanel(peer, localUserId, buildDmCallback());
            directChats.put(peer.id(), panel);
            contentPanel.add(panel, cardKey);
        }
        cardLayout.show(contentPanel, cardKey);
    }

    @Override
    public void onRoomSelected(RoomInfo room) {
        String cardKey = "room_" + room.id();
        if (!groupChats.containsKey(room.id())) {
            GroupChatPanel panel = new GroupChatPanel(room, localUserId, buildGroupCallback());
            groupChats.put(room.id(), panel);
            contentPanel.add(panel, cardKey);
        }
        cardLayout.show(contentPanel, cardKey);
    }

    @Override
    public void onHistoryRequested() {
        historyPanel.loadHistory(chatHistory);
        cardLayout.show(contentPanel, CARD_HISTORY);
    }

    @Override
    public void onCreateGroupRequested() {
        // Modal dialog tạo nhóm bo góc hiện đại
        JDialog dialog = new JDialog(this, "Tạo nhóm mới", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(this);

        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel titleLbl = new JLabel("Tạo nhóm trò chuyện mới");
        titleLbl.setFont(UiUtils.F_HEADING);
        titleLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(14));

        UiUtils.RoundedTextField nameField = new UiUtils.RoundedTextField("Nhập tên nhóm...");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameField);
        card.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        UiUtils.RoundedButton cancelBtn = new UiUtils.RoundedButton(
                "Huỷ", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_SM);
        cancelBtn.addActionListener(e -> dialog.dispose());

        UiUtils.RoundedButton okBtn = new UiUtils.RoundedButton(
                "Tạo nhóm", UiUtils.C_PRIMARY, Color.WHITE, UiUtils.ARC_SM);
        okBtn.addActionListener(e -> {
            String roomName = nameField.getText().trim();
            if (!roomName.isEmpty()) {
                RoomInfo newRoom = new RoomInfo(
                        "room_" + System.currentTimeMillis(),
                        roomName,
                        localUserId
                );
                demoRooms.add(newRoom);
                sidebarPanel.setRooms(new ArrayList<>(demoRooms));
                dialog.dispose();
                onRoomSelected(newRoom);
            }
        });

        nameField.addActionListener(e -> okBtn.doClick());

        btnRow.add(cancelBtn);
        btnRow.add(okBtn);
        card.add(btnRow);

        dialog.setContentPane(card);
        dialog.setVisible(true);
    }

    // ── Callbacks ─────────────────────────────────────────────────
    private DirectChatPanel.ActionCallback buildDmCallback() {
        return new DirectChatPanel.ActionCallback() {
            @Override
            public void onSendMessage(PeerInfo peer, String text) {
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
            }

            @Override
            public void onCallRequested(PeerInfo peer) {
                currentCallPeer = peer;
                callPanel.setState(CallPanel.CallState.RINGING, peer.username());
                cardLayout.show(contentPanel, CARD_CALL);
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
        };
    }

    private CallPanel.CallListener buildCallListener() {
        return new CallPanel.CallListener() {
            @Override
            public void onCallCancelled() {
                cardLayout.show(contentPanel, CARD_WELCOME);
                currentCallPeer = null;
            }

            @Override
            public void onCallEnded() {
                cardLayout.show(contentPanel, CARD_WELCOME);
                currentCallPeer = null;
            }

            @Override
            public void onMuteToggled(boolean muted) {
                // Sẽ kích hoạt voiceGateway.setMuted(muted) khi nối core
            }
        };
    }

    // ── Public API (dùng cho tích hợp mạng sau này) ──────────────
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

    public void setPeers(List<PeerInfo> peers) {
        SwingUtilities.invokeLater(() -> sidebarPanel.setPeers(peers));
    }

    public void setRooms(List<RoomInfo> rooms) {
        SwingUtilities.invokeLater(() -> sidebarPanel.setRooms(rooms));
    }

    public void showIncomingCall(String callerName) {
        SwingUtilities.invokeLater(() ->
            new IncomingCallDialog(this, callerName, new IncomingCallDialog.ResponseListener() {
                @Override
                public void onAccepted() {
                    callPanel.setState(CallPanel.CallState.IN_CALL, callerName);
                    cardLayout.show(contentPanel, CARD_CALL);
                }
                @Override
                public void onRejected() {
                    // Từ chối cuộc gọi
                }
            }).setVisible(true)
        );
    }

    public void onCallEnded() {
        callPanel.setState(CallPanel.CallState.IDLE, "");
        if (currentCallPeer != null) {
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

    private void loadDemoData() {
        List<PeerInfo> peers = List.of(
            new PeerInfo("peer_B", "User_B", "192.168.1.2", 5000, 5001),
            new PeerInfo("peer_C", "User_C", "192.168.1.3", 5000, 5001),
            new PeerInfo("peer_D", "User_D", "192.168.1.4", 5000, 5001)
        );
        demoRooms.addAll(List.of(
            new RoomInfo("room_1", "Nhóm học nhóm", localUserId),
            new RoomInfo("room_2", "Project Alpha", localUserId)
        ));
        sidebarPanel.setPeers(peers);
        sidebarPanel.setRooms(new ArrayList<>(demoRooms));
    }
}
