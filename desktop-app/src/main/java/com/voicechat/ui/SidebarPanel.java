package com.voicechat.ui;

import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VoiceLink SidebarPanel: Thanh bên danh bạ và nhóm chat phong cách VoiceLink.
 * <p>Chiều rộng 260px, nền trắng tinh (C_CARD_BG), viền phải mảnh 1px (C_BORDER).
 * Các mục chat bo góc 10px, có AvatarBadge với màu pastel và chấm trạng thái Online/Offline.</p>
 */
public final class SidebarPanel extends JPanel {

    // ── Callback giao tiếp với ChatFrame ─────────────────────────
    public interface SelectionListener {
        void onPeerSelected(PeerInfo peer);
        void onRoomSelected(RoomInfo room);
        void onHistoryRequested();
        void onCreateGroupRequested();
    }

    // ── State ─────────────────────────────────────────────────────
    private final SelectionListener listener;
    private final JPanel            peerListPanel = new JPanel();
    private final JPanel            roomListPanel = new JPanel();
    private final UiUtils.RoundedTextField searchField = new UiUtils.RoundedTextField("Tìm bạn bè hoặc nhóm...");
    private List<PeerInfo>          allPeers      = new ArrayList<>();
    private List<RoomInfo>          allRooms      = new ArrayList<>();
    private Object                  selectedItem  = null; // PeerInfo | RoomInfo
    private enum FilterMode { ALL, FRIENDS, GROUPS }
    private FilterMode filterMode = FilterMode.ALL;
    private final JPanel filterTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));

    public SidebarPanel(SelectionListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_CARD_BG);
        setPreferredSize(new Dimension(280, 0));
        setMinimumSize(new Dimension(280, 0));
        // Đường kẻ mảnh 1px ngăn cách cột bên phải
        setBorder(new MatteBorder(0, 0, 0, 1, UiUtils.C_BORDER));

        add(buildTopBar(),         BorderLayout.NORTH);
        add(buildScrollableList(), BorderLayout.CENTER);
    }

    // ── Top bar: Tiêu đề + Nút Tạo nhóm + Search box ──────────────
    private JPanel buildTopBar() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtils.C_CARD_BG);

        // Header: tiêu đề và nút tạo nhóm nhanh
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UiUtils.C_CARD_BG);
        titleBar.setBorder(new EmptyBorder(16, 16, 10, 16));

        JLabel titleLbl = new JLabel("Tin nhắn");
        titleLbl.setFont(UiUtils.F_TITLE_XL);
        titleLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        titleBar.add(titleLbl, BorderLayout.WEST);

        // Nút tròn tạo nhóm (+)
        UiUtils.RoundedButton addBtn = new UiUtils.RoundedButton(
                "+", UiUtils.C_PRIMARY, UiUtils.C_TEXT_PRIMARY, 14);
        addBtn.setPreferredSize(new Dimension(32, 32));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setToolTipText("Tạo nhóm trò chuyện mới");
        addBtn.addActionListener(e -> {
            if (listener != null) listener.onCreateGroupRequested();
        });
        titleBar.add(addBtn, BorderLayout.EAST);
        top.add(titleBar);

        // Ô tìm kiếm bo góc pill
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(UiUtils.C_CARD_BG);
        searchWrap.setBorder(new EmptyBorder(0, 14, 10, 14));

        searchField.setFont(UiUtils.F_SMALL);
        searchField.setBgColor(UiUtils.C_BEIGE_FIELD);
        searchField.setPreferredSize(new Dimension(244, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(searchField.getText().trim().toLowerCase()); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(searchField.getText().trim().toLowerCase()); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(searchField.getText().trim().toLowerCase()); }
        });
        JLabel searchIcon = new JLabel(new AppIcons(AppIcons.Kind.SEARCH, 15, UiUtils.C_TEXT_MUTED));
        searchIcon.setBorder(new EmptyBorder(0, 10, 0, 6));
        JPanel searchRow = new JPanel(new BorderLayout()); searchRow.setOpaque(false);
        searchRow.add(searchIcon, BorderLayout.WEST); searchRow.add(searchField, BorderLayout.CENTER);
        searchWrap.add(searchRow, BorderLayout.CENTER);
        top.add(searchWrap);

        filterTabs.setOpaque(false);
        filterTabs.setBorder(new EmptyBorder(0,14,10,10));
        refreshFilterTabs();
        top.add(filterTabs);

        // Đường kẻ mờ phân tách
        JSeparator sep = new JSeparator();
        sep.setForeground(UiUtils.C_BORDER);
        sep.setBackground(UiUtils.C_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        top.add(sep);

        return top;
    }

    private void refreshFilterTabs() {
        filterTabs.removeAll();
        addFilterTab("Tất cả", FilterMode.ALL);
        addFilterTab("Bạn bè", FilterMode.FRIENDS);
        addFilterTab("Nhóm", FilterMode.GROUPS);
        filterTabs.revalidate(); filterTabs.repaint();
    }

    private void addFilterTab(String text, FilterMode mode) {
        boolean active = filterMode == mode;
        UiUtils.RoundedButton tab = new UiUtils.RoundedButton(text,
                active ? UiUtils.C_SELECTED_BG : UiUtils.C_SIDEBAR_LIGHT,
                active ? UiUtils.C_PRIMARY_TEXT : UiUtils.C_TEXT_MUTED, 999);
        tab.setFont(UiUtils.F_SMALL); tab.setBorder(new EmptyBorder(5,10,5,10));
        tab.addActionListener(e -> { filterMode=mode; refreshFilterTabs(); applyFilter(currentFilter()); });
        filterTabs.add(tab);
    }

    private JPanel buildTrailingInfo(String time, int unread) {
        JPanel trailing=new JPanel(); trailing.setOpaque(false); trailing.setLayout(new BoxLayout(trailing,BoxLayout.Y_AXIS));
        if(time!=null&&!time.isBlank()) {
            JLabel timeLabel=new JLabel(time); timeLabel.setFont(UiUtils.F_SMALL); timeLabel.setForeground(UiUtils.C_TEXT_MUTED);
            timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT); trailing.add(timeLabel);
        }
        if(unread>0) {
            JLabel badge=new JLabel(Integer.toString(unread),SwingConstants.CENTER); badge.setFont(UiUtils.F_SMALL_BOLD);
            badge.setForeground(Color.WHITE); badge.setOpaque(true); badge.setBackground(UiUtils.C_PRIMARY_TEXT);
            badge.setPreferredSize(new Dimension(18,18)); badge.setMinimumSize(new Dimension(18,18)); badge.setMaximumSize(new Dimension(18,18));
            badge.setAlignmentX(Component.RIGHT_ALIGNMENT); trailing.add(Box.createVerticalStrut(4)); trailing.add(badge);
        }
        return trailing;
    }

    // ── Vùng cuộn danh sách (Peers + Rooms) ────────────────────────
    private JScrollPane buildScrollableList() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UiUtils.C_CARD_BG);
        container.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Section: Tin nhắn trực tiếp
        peerListPanel.setLayout(new BoxLayout(peerListPanel, BoxLayout.Y_AXIS));
        peerListPanel.setBackground(UiUtils.C_CARD_BG);
        container.add(peerListPanel);

        container.add(Box.createVerticalStrut(4));
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(UiUtils.C_CARD_BG);
        container.add(roomListPanel);

        container.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setBackground(UiUtils.C_CARD_BG);
        scroll.getViewport().setBackground(UiUtils.C_CARD_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        return scroll;
    }

    private JPanel buildSectionHeader(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(UiUtils.C_CARD_BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel l = new JLabel(text);
        l.setFont(UiUtils.F_SMALL_BOLD);
        l.setForeground(UiUtils.C_TEXT_MUTED);
        p.add(l);
        return p;
    }

    // ── Item Peer (Chat 1-1) ──────────────────────────────────────
    private JPanel buildPeerItem(PeerInfo peer) {
        boolean isSelected = peer.equals(selectedItem);
        boolean isOnline   = false;

        UiUtils.RoundedPanel item = new UiUtils.RoundedPanel(
                UiUtils.ARC_SM,
                isSelected ? UiUtils.C_SELECTED_BG : UiUtils.C_CARD_BG,
                isSelected ? UiUtils.C_BORDER_FOCUS : null
        );
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(new EmptyBorder(8, 8, 8, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // AvatarBadge tròn màu pastel + chấm tròn trạng thái
        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(peer.username(), 40, false, isOnline, true);
        item.add(avatar, BorderLayout.WEST);

        // Tên & trạng thái
        JLabel nameLbl = new JLabel(peer.username());
        nameLbl.setFont(UiUtils.F_BODY_BOLD);
        nameLbl.setForeground(isSelected ? UiUtils.C_PRIMARY_TEXT : UiUtils.C_TEXT_PRIMARY);

        JLabel preview = new JLabel("Chưa có tin nhắn"); preview.setFont(UiUtils.F_SMALL); preview.setForeground(UiUtils.C_TEXT_MUTED);
        JPanel textStack = new JPanel(); textStack.setOpaque(false); textStack.setLayout(new BoxLayout(textStack,BoxLayout.Y_AXIS));
        textStack.add(nameLbl); textStack.add(Box.createVerticalStrut(3)); textStack.add(preview);
        item.add(textStack, BorderLayout.CENTER);
        item.add(buildTrailingInfo("", 0), BorderLayout.EAST);

        // Hover & Click
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!peer.equals(selectedItem)) {
                    item.setBackgroundColor(UiUtils.C_HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!peer.equals(selectedItem)) {
                    item.setBackgroundColor(UiUtils.C_CARD_BG);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectedItem = peer;
                refreshPeerList(currentFilter());
                refreshRoomList(currentFilter());
                if (listener != null) listener.onPeerSelected(peer);
            }
        });

        return item;
    }

    // ── Item Room (Nhóm chat) ─────────────────────────────────────
    private JPanel buildRoomItem(RoomInfo room) {
        boolean isSelected = room.equals(selectedItem);

        UiUtils.RoundedPanel item = new UiUtils.RoundedPanel(
                UiUtils.ARC_SM,
                isSelected ? UiUtils.C_SELECTED_BG : UiUtils.C_CARD_BG,
                isSelected ? UiUtils.C_BORDER_FOCUS : null
        );
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(new EmptyBorder(8, 8, 8, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(room.name(), 40, true, false, false);
        item.add(avatar, BorderLayout.WEST);

        JLabel nameLbl = new JLabel(room.name());
        nameLbl.setFont(UiUtils.F_BODY_BOLD);
        nameLbl.setForeground(isSelected ? UiUtils.C_PRIMARY_TEXT : UiUtils.C_TEXT_PRIMARY);

        JPanel textStack = new JPanel(); textStack.setOpaque(false); textStack.setLayout(new BoxLayout(textStack,BoxLayout.Y_AXIS));
        JLabel preview = new JLabel("Nhóm trò chuyện"); preview.setFont(UiUtils.F_SMALL); preview.setForeground(UiUtils.C_TEXT_MUTED);
        textStack.add(nameLbl); textStack.add(Box.createVerticalStrut(3)); textStack.add(preview);
        item.add(textStack, BorderLayout.CENTER);
        item.add(buildTrailingInfo("", 0), BorderLayout.EAST);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!room.equals(selectedItem)) {
                    item.setBackgroundColor(UiUtils.C_HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!room.equals(selectedItem)) {
                    item.setBackgroundColor(UiUtils.C_CARD_BG);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectedItem = room;
                refreshPeerList(currentFilter());
                refreshRoomList(currentFilter());
                if (listener != null) listener.onRoomSelected(room);
            }
        });

        return item;
    }

    // ── Public API ────────────────────────────────────────────────
    public void setPeers(List<PeerInfo> peers) {
        this.allPeers = new ArrayList<>(peers);
        refreshPeerList(currentFilter());
    }

    public void setRooms(List<RoomInfo> rooms) {
        this.allRooms = new ArrayList<>(rooms);
        refreshRoomList(currentFilter());
    }

    public void selectPeer(PeerInfo peer) {
        selectedItem = peer;
        refreshPeerList(currentFilter());
        refreshRoomList(currentFilter());
    }

    public PeerInfo findPeer(String id) {
        for (PeerInfo peer : allPeers) if (peer.id().equals(id)) return peer;
        return null;
    }

    public void showGroups() {
        filterMode=FilterMode.GROUPS; refreshFilterTabs(); applyFilter(currentFilter());
    }

    public void showFriends() {
        filterMode=FilterMode.FRIENDS; refreshFilterTabs(); applyFilter(currentFilter());
    }

    // ── Filter logic ──────────────────────────────────────────────
    private String currentFilter() {
        return searchField.getText().trim().toLowerCase();
    }

    private void applyFilter(String query) {
        refreshPeerList(query);
        refreshRoomList(query);
    }

    private void refreshPeerList(String filter) {
        peerListPanel.removeAll();
        boolean any = false;
        if (filterMode != FilterMode.GROUPS) for (PeerInfo p : allPeers) {
            if (filter.isEmpty() || p.username().toLowerCase().contains(filter)) {
                peerListPanel.add(buildPeerItem(p));
                peerListPanel.add(Box.createVerticalStrut(4));
                any = true;
            }
        }
        if (!any && filterMode != FilterMode.GROUPS) peerListPanel.add(buildEmptyHint("Chưa có liên hệ nào"));
        peerListPanel.revalidate();
        peerListPanel.repaint();
    }

    private void refreshRoomList(String filter) {
        roomListPanel.removeAll();
        boolean any = false;
        if (filterMode != FilterMode.FRIENDS) for (RoomInfo r : allRooms) {
            if (filter.isEmpty() || r.name().toLowerCase().contains(filter)) {
                roomListPanel.add(buildRoomItem(r));
                roomListPanel.add(Box.createVerticalStrut(4));
                any = true;
            }
        }
        if (!any && filterMode != FilterMode.FRIENDS) roomListPanel.add(buildEmptyHint("Chưa có nhóm nào"));
        roomListPanel.revalidate();
        roomListPanel.repaint();
    }

    private JLabel buildEmptyHint(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UiUtils.F_SMALL);
        lbl.setForeground(UiUtils.C_TEXT_HINT);
        lbl.setBorder(new EmptyBorder(8, 4, 8, 4));
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        return lbl;
    }

    // ── Thin scroll bar UI cho giao diện sáng ─────────────────────
    private static final class ThinScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        private static final Color THUMB = new Color(0x8D, 0xB4, 0xD2, 140);
        private static final Color TRACK = Color.WHITE;

        @Override protected void configureScrollBarColors() {
            thumbColor = THUMB;
            trackColor = TRACK;
        }

        @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }

        private static JButton zeroButton() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            UiUtils.enableAntiAliasing(g2);
            g2.setColor(THUMB);
            g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(TRACK);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
