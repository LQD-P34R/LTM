package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * VoiceLink ChatFrame: Cửa sổ làm việc chính của ứng dụng.
 */
public final class ChatFrame extends JFrame implements SidebarPanel.SelectionListener {

    static {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 999);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 18);
        UiUtils.installGlobalTypography();
    }

    private static final String CARD_WELCOME = "WELCOME";
    private static final String CARD_HISTORY = "HISTORY";
    private static final String CARD_CALL    = "CALL";
    private static final String CARD_CHAT    = "CHAT";

    private final CardLayout cardLayout      = new CardLayout();
    private final JPanel     contentPanel    = new JPanel(cardLayout);
    private final CardLayout workspaceLayout = new CardLayout();
    private final JPanel     workspacePanel  = new JPanel(workspaceLayout);

    private final SidebarPanel   sidebarPanel;
    private final HistoryPanel   historyPanel = new HistoryPanel();
    private final CallPanel      callPanel;
    private final ChatInfoPanel  infoPanel;
    private final NavigationRail navigationRail;

    private final Map<String, DirectChatPanel> directChats = new LinkedHashMap<>();
    private final Map<String, GroupChatPanel>  groupChats  = new LinkedHashMap<>();

    private final String            localUsername;
    private final String            localUserId;
    private final List<ChatMessage> chatHistory     = new ArrayList<>();
    private final List<RoomInfo>    demoRooms       = new ArrayList<>();
    private PeerInfo                currentCallPeer = null;
    private RoomInfo                currentCallRoom = null;

    public ChatFrame(String localUsername) {
        super("VoiceLink P2P — " + localUsername);
        this.localUsername = localUsername;
        this.localUserId = localUsername;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1080, 660));
        setLocationRelativeTo(null);

        callPanel = new CallPanel(buildCallListener());
        sidebarPanel = new SidebarPanel(this);
        infoPanel = new ChatInfoPanel(buildInfoActions());
        navigationRail = new NavigationRail(localUsername, new NavigationRail.Listener() {
            @Override public void onNavigate(NavigationRail.Destination destination) { navigate(destination); }
            @Override public void onProfileRequested(String username) { showProfileDialog(username); }
        });
        historyPanel.setConversationListener(this::openConversationFromHistory);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.C_CARD_BG);
        root.add(navigationRail, BorderLayout.WEST);
        root.add(buildWorkspaces(), BorderLayout.CENTER);
        setContentPane(root);

        loadDemoData();
    }

    private JPanel buildWorkspaces() {
        contentPanel.setBackground(UiUtils.C_CARD_BG);
        contentPanel.setOpaque(true);
        contentPanel.add(buildWelcomePanel(), CARD_WELCOME);
        cardLayout.show(contentPanel, CARD_WELCOME);

        JPanel chatWorkspace = new JPanel(new BorderLayout());
        chatWorkspace.setBackground(UiUtils.C_CARD_BG);
        chatWorkspace.setOpaque(true);
        chatWorkspace.add(sidebarPanel, BorderLayout.WEST);
        chatWorkspace.add(contentPanel, BorderLayout.CENTER);
        chatWorkspace.add(infoPanel, BorderLayout.EAST);

        workspacePanel.add(chatWorkspace, CARD_CHAT);
        workspacePanel.add(historyPanel, CARD_HISTORY);
        workspacePanel.add(callPanel, CARD_CALL);
        workspaceLayout.show(workspacePanel, CARD_CHAT);
        return workspacePanel;
    }

    private void navigate(NavigationRail.Destination destination) {
        switch (destination) {
            case CHAT -> {
                navigationRail.setVisible(true);
                workspaceLayout.show(workspacePanel, CARD_CHAT);
            }
            case CALLS -> {
                callPanel.setState(CallPanel.CallState.IDLE, "");
                showCallWorkspace();
            }
            case GROUPS -> {
                navigationRail.setVisible(true);
                workspaceLayout.show(workspacePanel, CARD_CHAT);
                sidebarPanel.showGroups();
            }
            case HISTORY -> onHistoryRequested();
        }
    }

    private void showCallWorkspace() {
        navigationRail.setVisible(false);
        workspaceLayout.show(workspacePanel, CARD_CALL);
    }

    private JPanel buildWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiUtils.C_APP_BG);

        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 320));

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

        JLabel iconLbl = new JLabel(new AppIcons(AppIcons.Kind.LOGO, 36, UiUtils.C_PRIMARY_TEXT));
        iconLbl.setForeground(UiUtils.C_PRIMARY_TEXT);
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
                "<html><div style='text-align:center;width:310px'>Chọn một liên hệ từ danh sách bên trái<br>để bắt đầu nhắn tin hoặc gọi thoại.</div></html>",
                SwingConstants.CENTER);
        hintLbl.setFont(UiUtils.F_BODY);
        hintLbl.setForeground(UiUtils.C_TEXT_MUTED);
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hintLbl);

        panel.add(card);
        return panel;
    }

    // ── SidebarPanel.SelectionListener ───────────────────────────
    @Override
    public void onPeerSelected(PeerInfo peer) {
        navigationRail.select(NavigationRail.Destination.CHAT);
        workspaceLayout.show(workspacePanel, CARD_CHAT);
        infoPanel.showPeer(peer);
        String cardKey = "dm_" + peer.id();
        if (!directChats.containsKey(peer.id())) {
            DirectChatPanel panel = new DirectChatPanel(peer, localUserId, buildDmCallback());
            panel.loadHistory(chatHistory.stream()
                    .filter(msg -> cardKey.equals(msg.roomId()))
                    .toList());
            directChats.put(peer.id(), panel);
            contentPanel.add(panel, cardKey);
        }
        cardLayout.show(contentPanel, cardKey);
    }

    @Override
    public void onRoomSelected(RoomInfo room) {
        navigationRail.select(NavigationRail.Destination.CHAT);
        workspaceLayout.show(workspacePanel, CARD_CHAT);
        infoPanel.showRoom(room);
        String cardKey = "room_" + room.id();
        if (!groupChats.containsKey(room.id())) {
            GroupChatPanel panel = new GroupChatPanel(room, localUserId, buildGroupCallback());
            panel.loadHistory(chatHistory.stream()
                    .filter(msg -> room.id().equals(msg.roomId()))
                    .toList());
            groupChats.put(room.id(), panel);
            contentPanel.add(panel, cardKey);
        }
        cardLayout.show(contentPanel, cardKey);
    }

    @Override
    public void onHistoryRequested() {
        historyPanel.loadHistory(chatHistory);
        navigationRail.select(NavigationRail.Destination.HISTORY);
        workspaceLayout.show(workspacePanel, CARD_HISTORY);
    }

    private void openConversationFromHistory(String roomId) {
        if (roomId.startsWith("dm_")) {
            String peerId = roomId.substring(3);
            PeerInfo peer = sidebarPanel.findPeer(peerId);
            if (peer != null) onPeerSelected(peer);
        } else {
            for (RoomInfo room : demoRooms) {
                if (room.id().equals(roomId)) {
                    onRoomSelected(room);
                    return;
                }
            }
        }
    }

    private ChatInfoPanel.Actions buildInfoActions() {
        return new ChatInfoPanel.Actions() {
            @Override public void onCallPeer(PeerInfo peer) { startPeerCall(peer); }
            @Override public void onCallGroup(RoomInfo room) {
                currentCallPeer = null;
                currentCallRoom = room;
                callPanel.setState(CallPanel.CallState.OUTGOING, room.name());
                showCallWorkspace();
            }
            @Override public void onShowHistory(PeerInfo peer) { ChatFrame.this.onHistoryRequested(); }
            @Override public void onAddMember(RoomInfo room) { showAddMemberDialog(room); }
            @Override public void onLeaveGroup(RoomInfo room) { showLeaveGroupDialog(room); }
        };
    }

    private void showAddMemberDialog(RoomInfo room) {
        JDialog dialog = createUiDialog("Thêm thành viên", 360, 420);
        JPanel card = dialogCard(16);
        card.setLayout(new BorderLayout(0, 12));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Thêm thành viên", SwingConstants.CENTER);
        title.setFont(UiTheme.SEMIBOLD_FONT.deriveFont(15f));
        title.setForeground(UiUtils.C_TEXT_PRIMARY);
        titleRow.add(title);
        top.add(titleRow);
        top.add(Box.createVerticalStrut(12));
        UiUtils.RoundedTextField search = new UiUtils.RoundedTextField("Tìm kiếm thành viên...");
        search.setFont(UiUtils.F_BODY);
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        top.add(search);
        card.add(top, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setBackground(UiUtils.C_CARD_BG);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        String[][] samples = {
                {"Quốc Bảo", "Online"},
                {"Thu Hà", "Online"},
                {"Lan Phương", "Ngoại tuyến"},
                {"Đức Anh", "Ngoại tuyến"}
        };
        for (String[] sample : samples) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
            row.setBackground(UiUtils.C_CARD_BG);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
            JCheckBox check = new JCheckBox();
            check.setOpaque(false);
            row.add(check);
            UiUtils.AvatarBadge memberAvatar = new UiUtils.AvatarBadge(sample[0], 34, false, sample[1].equals("Online"), true);
            row.add(memberAvatar);
            JPanel details = new JPanel();
            details.setOpaque(false);
            details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
            details.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel name = new JLabel(sample[0]);
            name.setFont(UiTheme.SEMIBOLD_FONT.deriveFont(13.5f));
            name.setForeground(UiUtils.C_TEXT_PRIMARY);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel state = new JLabel(sample[1]);
            state.setFont(UiUtils.F_SMALL);
            state.setForeground(sample[1].equals("Online") ? UiUtils.C_PRIMARY_TEXT : UiUtils.C_TEXT_MUTED);
            state.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.add(name);
            details.add(state);
            row.add(details);
            list.add(row);
        }
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        UiUtils.RoundedButton cancel = new UiUtils.RoundedButton("Hủy", UiUtils.C_CARD_BG, UiUtils.C_TEXT_PRIMARY, 12);
        cancel.setBorderColor(UiUtils.C_BORDER);
        cancel.addActionListener(e -> dialog.dispose());
        UiUtils.RoundedButton add = new UiUtils.RoundedButton("Thêm", UiUtils.C_PRIMARY, UiUtils.C_TEXT_PRIMARY, 12);
        add.addActionListener(e -> dialog.dispose());
        buttons.add(cancel);
        buttons.add(add);
        card.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(card);
        dialog.setVisible(true);
    }

    private void showLeaveGroupDialog(RoomInfo room) {
        JDialog dialog = createUiDialog("Rời khỏi nhóm", 400, 185);
        JPanel card = dialogCard(16);
        card.setLayout(new BorderLayout(0, 12));
        JLabel title = new JLabel("Rời khỏi nhóm");
        title.setFont(UiTheme.SEMIBOLD_FONT.deriveFont(15f));
        title.setForeground(UiUtils.C_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JTextArea message = new JTextArea("Bạn có chắc chắn muốn rời khỏi nhóm này không?");
        message.setFont(UiTheme.REGULAR_FONT.deriveFont(13f));
        message.setForeground(UiUtils.C_TEXT_MUTED);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setEditable(false);
        message.setOpaque(false);
        message.setFocusable(false);
        message.setBorder(BorderFactory.createEmptyBorder());
        card.add(message, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        UiUtils.RoundedButton no = new UiUtils.RoundedButton("No", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_TEXT_PRIMARY, 12);
        no.addActionListener(e -> dialog.dispose());
        UiUtils.RoundedButton yes = new UiUtils.RoundedButton("Yes", UiUtils.C_DANGER, Color.WHITE, 12);
        yes.addActionListener(e -> dialog.dispose());
        buttons.add(no);
        buttons.add(yes);
        card.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(card);
        dialog.setVisible(true);
    }

    private void showProfileDialog(String username) {
        JDialog dialog = createUiDialog("Thông tin tài khoản", 320, 340);
        JPanel card = dialogCard(24);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(username, 72, false, true, true);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel(username);
        name.setFont(UiTheme.SEMIBOLD_FONT.deriveFont(16f));
        name.setForeground(UiUtils.C_TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel handle = new JLabel("@" + username);
        handle.setFont(UiTheme.REGULAR_FONT.deriveFont(12.5f));
        handle.setForeground(UiUtils.C_TEXT_MUTED);
        handle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel online = new JLabel("Đang trực tuyến");
        online.setFont(UiUtils.F_SMALL);
        online.setForeground(UiUtils.C_PRIMARY_TEXT);
        online.setAlignmentX(Component.CENTER_ALIGNMENT);
        UiUtils.RoundedButton close = new UiUtils.RoundedButton("Đóng", UiUtils.C_PRIMARY, UiUtils.C_TEXT_PRIMARY, 12);
        close.setAlignmentX(Component.CENTER_ALIGNMENT);
        close.setPreferredSize(new Dimension(240, 38));
        close.addActionListener(e -> dialog.dispose());

        card.add(Box.createVerticalGlue());
        card.add(avatar);
        card.add(Box.createVerticalStrut(12));
        card.add(name);
        card.add(Box.createVerticalStrut(3));
        card.add(handle);
        card.add(Box.createVerticalStrut(10));
        card.add(online);
        card.add(Box.createVerticalGlue());
        card.add(close);
        card.add(Box.createVerticalStrut(4));

        dialog.setContentPane(card);
        dialog.setVisible(true);
    }

    private JDialog createUiDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private UiUtils.RoundedPanel dialogCard(int padding) {
        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(16, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return card;
    }

    private void startPeerCall(PeerInfo peer) {
        currentCallPeer = peer;
        currentCallRoom = null;
        callPanel.setState(CallPanel.CallState.OUTGOING, peer.username());
        showCallWorkspace();
    }

    @Override
    public void onCreateGroupRequested() {
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
                "Tạo nhóm", UiUtils.C_PRIMARY, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_SM);
        okBtn.addActionListener(e -> {
            String roomName = nameField.getText().trim();
            if (!roomName.isEmpty()) {
                RoomInfo newRoom = new RoomInfo(
                        "room_" + System.currentTimeMillis(),
                        roomName,
                        localUserId);
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
                        Instant.now());
                chatHistory.add(msg);
                DirectChatPanel panel = directChats.get(peer.id());
                if (panel != null) {
                    SwingUtilities.invokeLater(() -> panel.appendMessage(msg));
                }
            }

            @Override
            public void onCallRequested(PeerInfo peer) {
                startPeerCall(peer);
            }
        };
    }

    private GroupChatPanel.ActionCallback buildGroupCallback() {
        return new GroupChatPanel.ActionCallback() {
            @Override
            public void onSendMessage(RoomInfo room, String text) {
                ChatMessage msg = new ChatMessage(
                        UUID.randomUUID().toString(),
                        room.id(),
                        localUserId,
                        text,
                        Instant.now());
                chatHistory.add(msg);
                GroupChatPanel panel = groupChats.get(room.id());
                if (panel != null) {
                    SwingUtilities.invokeLater(() -> panel.appendMessage(msg));
                }
            }

            @Override
            public void onGroupCallRequested(RoomInfo room) {
                currentCallPeer = null;
                currentCallRoom = room;
                callPanel.setState(CallPanel.CallState.OUTGOING, room.name());
                showCallWorkspace();
            }
        };
    }

    private CallPanel.CallListener buildCallListener() {
        return new CallPanel.CallListener() {
            @Override
            public void onCallCancelled() {
                navigationRail.setVisible(true);
                workspaceLayout.show(workspacePanel, CARD_CHAT);
                cardLayout.show(contentPanel, CARD_WELCOME);
                currentCallPeer = null;
                currentCallRoom = null;
            }

            @Override
            public void onCallEnded() {
                ChatFrame.this.onCallEnded();
            }

            @Override
            public void onMuteToggled(boolean muted) {
                // Sẽ kích hoạt voiceGateway.setMuted(muted) khi nối core
            }

            @Override
            public void onReturnToChat() {
                if (currentCallPeer != null && directChats.containsKey(currentCallPeer.id()))
                    cardLayout.show(contentPanel, "dm_" + currentCallPeer.id());
                else if (currentCallRoom != null && groupChats.containsKey(currentCallRoom.id())) {
                    cardLayout.show(contentPanel, "room_" + currentCallRoom.id());
                    infoPanel.showRoom(currentCallRoom);
                } else cardLayout.show(contentPanel, CARD_WELCOME);
                navigationRail.select(NavigationRail.Destination.CHAT);
                navigationRail.setVisible(true);
                workspaceLayout.show(workspacePanel, CARD_CHAT);
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
            if (panel != null)
                panel.appendMessage(msg);
        } else {
            GroupChatPanel panel = groupChats.get(roomId);
            if (panel != null)
                panel.appendMessage(msg);
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
                        showCallWorkspace();
                    }

                    @Override
                    public void onRejected() {
                        // Từ chối cuộc gọi
                    }
                }).setVisible(true)
        );
    }

    public void onCallEnded() {
        callPanel.setState(CallPanel.CallState.ENDED, callPanel.getCurrentPeerName());
        showCallWorkspace();
    }

    private void loadDemoData() {
        List<PeerInfo> peers = List.of(
                new PeerInfo("peer_B", "User_B", "192.168.1.2", 5000, 5001),
                new PeerInfo("peer_C", "User_C", "192.168.1.3", 5000, 5001),
                new PeerInfo("peer_D", "User_D", "192.168.1.4", 5000, 5001));
        demoRooms.addAll(List.of(
                new RoomInfo("room_1", "Nhóm học nhóm", localUserId),
                new RoomInfo("room_2", "Project Alpha", localUserId)));
        sidebarPanel.setPeers(peers);
        sidebarPanel.setRooms(new ArrayList<>(demoRooms));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatFrame frame = new ChatFrame("User_A");
            frame.setVisible(true);
        });
    }
}