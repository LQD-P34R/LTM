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
 * <p>
 * Chiều rộng 260px, nền trắng tinh (C_CARD_BG), viền phải mảnh 1px (C_BORDER).
 * Các mục chat bo góc 10px, có AvatarBadge với màu pastel và chấm trạng thái
 * Online/Offline.
 * </p>
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
    private final JPanel peerListPanel = new JPanel();
    private final JPanel roomListPanel = new JPanel();
    private final UiUtils.RoundedTextField searchField = new UiUtils.RoundedTextField("Tìm bạn bè hoặc nhóm...");
    private List<PeerInfo> allPeers = new ArrayList<>();
    private List<RoomInfo> allRooms = new ArrayList<>();
    private Object selectedItem = null; // PeerInfo | RoomInfo

    public SidebarPanel(SelectionListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_CARD_BG);
        setPreferredSize(new Dimension(260, 0));
        // Đường kẻ mảnh 1px ngăn cách cột bên phải
        setBorder(new MatteBorder(0, 0, 0, 1, UiUtils.C_BORDER));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildScrollableList(), BorderLayout.CENTER);
        add(buildBottomToolbar(), BorderLayout.SOUTH);
    }

    // ── Top bar: Tiêu đề + Nút Tạo nhóm + Search box ──────────────
    private JPanel buildTopBar() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(UiUtils.C_CARD_BG);

        // Header: Logo / Tiêu đề + Nút + tạo nhóm nhanh
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(UiUtils.C_CARD_BG);
        titleBar.setBorder(new EmptyBorder(14, 16, 10, 16));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setOpaque(false);

        JLabel iconLbl = new JLabel("\uD83D\uDCAC"); // 💬
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        iconLbl.setForeground(UiUtils.C_PRIMARY);

        JLabel titleLbl = new JLabel("Tin nhắn");
        titleLbl.setFont(UiUtils.F_TITLE_LG);
        titleLbl.setForeground(UiUtils.C_TEXT_PRIMARY);

        brandPanel.add(iconLbl);
        brandPanel.add(titleLbl);
        titleBar.add(brandPanel, BorderLayout.WEST);

        // Nút tròn tạo nhóm (+)
        UiUtils.RoundedButton addBtn = new UiUtils.RoundedButton(
                "+", UiUtils.C_PRIMARY, Color.WHITE, 14);
        addBtn.setPreferredSize(new Dimension(30, 30));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setToolTipText("Tạo nhóm trò chuyện mới");
        addBtn.addActionListener(e -> {
            if (listener != null)
                listener.onCreateGroupRequested();
        });
        titleBar.add(addBtn, BorderLayout.EAST);
        top.add(titleBar);

        // Ô tìm kiếm bo góc pill
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(UiUtils.C_CARD_BG);
        searchWrap.setBorder(new EmptyBorder(0, 14, 10, 14));

        searchField.setFont(UiUtils.F_SMALL);
        searchField.setBgColor(UiUtils.C_SIDEBAR_LIGHT);
        searchField.setPreferredSize(new Dimension(230, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter(searchField.getText().trim().toLowerCase());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter(searchField.getText().trim().toLowerCase());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter(searchField.getText().trim().toLowerCase());
            }
        });
        searchWrap.add(searchField, BorderLayout.CENTER);
        top.add(searchWrap);

        // Đường kẻ mờ phân tách
        JSeparator sep = new JSeparator();
        sep.setForeground(UiUtils.C_BORDER);
        sep.setBackground(UiUtils.C_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        top.add(sep);

        return top;
    }

    // ── Vùng cuộn danh sách (Peers + Rooms) ────────────────────────
    private JScrollPane buildScrollableList() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UiUtils.C_CARD_BG);
        container.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Section: Tin nhắn trực tiếp
        container.add(buildSectionHeader("TIN NHẮN TRỰC TIẾP"));
        peerListPanel.setLayout(new BoxLayout(peerListPanel, BoxLayout.Y_AXIS));
        peerListPanel.setBackground(UiUtils.C_CARD_BG);
        container.add(peerListPanel);

        container.add(Box.createVerticalStrut(12));

        // Section: Nhóm chat
        container.add(buildSectionHeader("NHÓM TRÒ CHUYỆN"));
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
        boolean isOnline = true; // Danh sach nay chi gom cac peer dang ket noi.

        UiUtils.RoundedPanel item = new UiUtils.RoundedPanel(
                UiUtils.ARC_SM,
                isSelected ? UiUtils.C_SELECTED_BG : UiUtils.C_CARD_BG,
                isSelected ? UiUtils.C_BORDER_FOCUS : null);
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(new EmptyBorder(8, 8, 8, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // AvatarBadge tròn màu pastel + chấm tròn trạng thái
        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(peer.username(), 38, false, isOnline, true);
        item.add(avatar, BorderLayout.WEST);

        // Tên & trạng thái
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(peer.username());
        nameLbl.setFont(UiUtils.F_BODY_BOLD);
        nameLbl.setForeground(isSelected ? UiUtils.C_PRIMARY : UiUtils.C_TEXT_PRIMARY);

        JLabel statusLbl = new JLabel(isOnline ? "Đang hoạt động" : "Ngoại tuyến");
        statusLbl.setFont(UiUtils.F_SMALL);
        statusLbl.setForeground(isOnline ? UiUtils.C_ONLINE : UiUtils.C_TEXT_MUTED);

        textPanel.add(nameLbl);
        textPanel.add(statusLbl);
        item.add(textPanel, BorderLayout.CENTER);

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
                if (listener != null)
                    listener.onPeerSelected(peer);
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
                isSelected ? UiUtils.C_BORDER_FOCUS : null);
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(new EmptyBorder(8, 8, 8, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(room.name(), 38, true, false, false);
        item.add(avatar, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(room.name());
        nameLbl.setFont(UiUtils.F_BODY_BOLD);
        nameLbl.setForeground(isSelected ? UiUtils.C_PRIMARY : UiUtils.C_TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Nhóm chat");
        subLbl.setFont(UiUtils.F_SMALL);
        subLbl.setForeground(UiUtils.C_TEXT_MUTED);

        textPanel.add(nameLbl);
        textPanel.add(subLbl);
        item.add(textPanel, BorderLayout.CENTER);

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
                if (listener != null)
                    listener.onRoomSelected(room);
            }
        });

        return item;
    }

    // ── Bottom Toolbar: Nút Lịch sử & Nút Tạo nhóm ───────────────
    private JPanel buildBottomToolbar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 8, 0));
        bar.setBackground(UiUtils.C_CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, UiUtils.C_BORDER),
                new EmptyBorder(10, 12, 10, 12)));

        UiUtils.RoundedButton historyBtn = new UiUtils.RoundedButton(
                "\uD83D\uDCDC Lịch sử", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_SM);
        historyBtn.addActionListener(e -> {
            selectedItem = null;
            refreshPeerList(currentFilter());
            refreshRoomList(currentFilter());
            if (listener != null)
                listener.onHistoryRequested();
        });

        UiUtils.RoundedButton groupBtn = new UiUtils.RoundedButton(
                "+ Tạo nhóm", UiUtils.C_PRIMARY_LIGHT, UiUtils.C_PRIMARY, UiUtils.ARC_SM);
        groupBtn.addActionListener(e -> {
            if (listener != null)
                listener.onCreateGroupRequested();
        });

        bar.add(historyBtn);
        bar.add(groupBtn);
        return bar;
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
        for (PeerInfo p : allPeers) {
            if (filter.isEmpty() || p.username().toLowerCase().contains(filter)) {
                peerListPanel.add(buildPeerItem(p));
                peerListPanel.add(Box.createVerticalStrut(4));
                any = true;
            }
        }
        if (!any)
            peerListPanel.add(buildEmptyHint("Chưa có liên hệ nào"));
        peerListPanel.revalidate();
        peerListPanel.repaint();
    }

    private void refreshRoomList(String filter) {
        roomListPanel.removeAll();
        boolean any = false;
        for (RoomInfo r : allRooms) {
            if (filter.isEmpty() || r.name().toLowerCase().contains(filter)) {
                roomListPanel.add(buildRoomItem(r));
                roomListPanel.add(Box.createVerticalStrut(4));
                any = true;
            }
        }
        if (!any)
            roomListPanel.add(buildEmptyHint("Chưa có nhóm nào"));
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
        private static final Color THUMB = new Color(0x94, 0xA3, 0xB8, 140);
        private static final Color TRACK = Color.WHITE;

        @Override
        protected void configureScrollBarColors() {
            thumbColor = THUMB;
            trackColor = TRACK;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return zeroButton();
        }

        private static JButton zeroButton() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty())
                return;
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
