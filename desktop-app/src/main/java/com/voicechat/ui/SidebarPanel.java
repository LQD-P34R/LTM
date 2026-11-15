package com.voicechat.ui;

import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TV3 – Bước 2: Thanh bên hiển thị danh sách peer (chat 1-1) và room (nhóm).
 * <p>Màu nền tối tương phản với vùng nội dung sáng bên phải.
 * Giao tiếp với ChatFrame qua {@link SelectionListener}.</p>
 *
 * <p>Cách dùng từ ChatFrame:</p>
 * <pre>
 *   SidebarPanel sidebar = new SidebarPanel(myListener);
 *   sidebar.setPeers(listOfPeerInfo);
 *   sidebar.setRooms(listOfRoomInfo);
 * </pre>
 */
public final class SidebarPanel extends JPanel {

    // ── Callback giao tiếp với ChatFrame ─────────────────────────
    public interface SelectionListener {
        void onPeerSelected(PeerInfo peer);
        void onRoomSelected(RoomInfo room);
        void onHistoryRequested();
        void onCreateGroupRequested();
    }

    // ── Màu sắc (dark sidebar palette) ───────────────────────────
    private static final Color COLOR_BG             = new Color(0x1E, 0x2D, 0x48);
    private static final Color COLOR_ITEM_NORMAL    = new Color(0x1E, 0x2D, 0x48);
    private static final Color COLOR_ITEM_HOVER     = new Color(0x27, 0x3D, 0x60);
    private static final Color COLOR_ITEM_SELECTED  = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_SECTION_HDR    = new Color(0x7A, 0x93, 0xB8);
    private static final Color COLOR_TEXT_PRIMARY   = new Color(0xEC, 0xF0, 0xF1);
    private static final Color COLOR_TEXT_SECONDARY = new Color(0x8A, 0xA3, 0xC8);
    private static final Color COLOR_ONLINE         = new Color(0x2E, 0xCC, 0x71);
    private static final Color COLOR_OFFLINE        = new Color(0x55, 0x6A, 0x7E);
    private static final Color COLOR_SEARCH_BG      = new Color(0x14, 0x1F, 0x33);
    private static final Color COLOR_DIVIDER        = new Color(0x2A, 0x3C, 0x58);
    private static final Color COLOR_BTN_BG         = new Color(0x2D, 0x40, 0x62);
    private static final Color COLOR_BTN_HOVER      = new Color(0x3A, 0x7B, 0xD5);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_APP_TITLE = new Font("SansSerif", Font.BOLD,  15);
    private static final Font FONT_SECTION   = new Font("SansSerif", Font.BOLD,  10);
    private static final Font FONT_USERNAME  = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_SUBTITLE  = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_SEARCH    = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN       = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_AVATAR    = new Font("SansSerif", Font.BOLD,  14);

    // ── Palette màu avatar (chọn theo hash tên) ───────────────────
    private static final Color[] AVATAR_COLORS = {
        new Color(0x3A, 0x7B, 0xD5), new Color(0xE7, 0x4C, 0x3C),
        new Color(0x2E, 0xCC, 0x71), new Color(0xF3, 0x9C, 0x12),
        new Color(0x9B, 0x59, 0xB6), new Color(0x1A, 0xBC, 0x9C),
        new Color(0xE9, 0x1E, 0x63), new Color(0x00, 0x96, 0x88),
    };

    // ── State ─────────────────────────────────────────────────────
    private final SelectionListener listener;
    private final JPanel            peerListPanel = new JPanel();
    private final JPanel            roomListPanel = new JPanel();
    private final JTextField        searchField   = new JTextField();
    private List<PeerInfo>          allPeers      = new ArrayList<>();
    private List<RoomInfo>          allRooms      = new ArrayList<>();
    private Object                  selectedItem  = null; // PeerInfo | RoomInfo

    // ─────────────────────────────────────────────────────────────

    public SidebarPanel(SelectionListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(220, 0));

        add(buildTopBar(),         BorderLayout.NORTH);
        add(buildScrollableList(), BorderLayout.CENTER);
        add(buildBottomToolbar(),  BorderLayout.SOUTH);
    }

    // ── Top bar: logo + search ────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(COLOR_BG);

        // Tiêu đề ứng dụng
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        titleBar.setBackground(COLOR_BG);
        JLabel iconLbl = new JLabel("\uD83D\uDCAC"); // 💬
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
        iconLbl.setForeground(COLOR_ITEM_SELECTED);
        JLabel titleLbl = new JLabel("VoiceChat");
        titleLbl.setFont(FONT_APP_TITLE);
        titleLbl.setForeground(COLOR_TEXT_PRIMARY);
        titleBar.add(iconLbl);
        titleBar.add(titleLbl);
        top.add(titleBar);

        top.add(buildDivider());
        top.add(buildSearchBar());
        top.add(Box.createVerticalStrut(6));

        return top;
    }

    private JPanel buildSearchBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.setBorder(new EmptyBorder(6, 10, 0, 10));

        searchField.setFont(FONT_SEARCH);
        searchField.setBackground(COLOR_SEARCH_BG);
        searchField.setCaretColor(COLOR_TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_DIVIDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        // Placeholder
        searchField.setText("Tìm kiếm...");
        searchField.setForeground(COLOR_TEXT_SECONDARY);

        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Tìm kiếm...")) {
                    searchField.setText("");
                    searchField.setForeground(COLOR_TEXT_PRIMARY);
                }
                searchField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_ITEM_SELECTED, 1, true),
                        new EmptyBorder(6, 10, 6, 10)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setText("Tìm kiếm...");
                    searchField.setForeground(COLOR_TEXT_SECONDARY);
                }
                searchField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_DIVIDER, 1, true),
                        new EmptyBorder(6, 10, 6, 10)
                ));
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void onChanged() {
                String q = searchField.getText();
                if (!q.equals("Tìm kiếm...")) applyFilter(q.toLowerCase());
            }
            @Override public void insertUpdate(DocumentEvent e)  { onChanged(); }
            @Override public void removeUpdate(DocumentEvent e)  { onChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onChanged(); }
        });

        wrapper.add(searchField, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Scrollable list (peers + rooms) ──────────────────────────
    private JScrollPane buildScrollableList() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_BG);

        // Peer section
        container.add(buildSectionHeader("TIN NHẮN TRỰC TIẾP"));
        peerListPanel.setLayout(new BoxLayout(peerListPanel, BoxLayout.Y_AXIS));
        peerListPanel.setBackground(COLOR_BG);
        container.add(peerListPanel);

        // Room section
        container.add(Box.createVerticalStrut(6));
        container.add(buildDivider());
        container.add(buildSectionHeader("NHÓM CHAT"));
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(COLOR_BG);
        container.add(roomListPanel);

        // Filler đẩy items lên trên
        container.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setBackground(COLOR_BG);
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());
        return scroll;
    }

    private JPanel buildSectionHeader(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        panel.setBackground(COLOR_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(COLOR_SECTION_HDR);
        panel.add(lbl);
        return panel;
    }

    // ── Peer item ─────────────────────────────────────────────────
    private JPanel buildPeerItem(PeerInfo peer) {
        boolean isSelected = peer.equals(selectedItem);

        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(isSelected ? COLOR_ITEM_SELECTED : COLOR_ITEM_NORMAL);
        item.setBorder(new EmptyBorder(8, 10, 8, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar
        item.add(buildAvatar(peer.username(), false), BorderLayout.WEST);

        // Text (name + status)
        JPanel textArea = new JPanel(new GridLayout(2, 1, 0, 1));
        textArea.setOpaque(false);

        JLabel nameLbl = new JLabel(peer.username());
        nameLbl.setFont(FONT_USERNAME);
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);

        // Online/offline giả lập theo hash — TV1 cập nhật thực sau
        boolean online = (Math.abs(peer.username().hashCode()) % 3 != 0);
        JLabel statusLbl = new JLabel(online ? "Đang hoạt động" : "Đang offline");
        statusLbl.setFont(FONT_SUBTITLE);
        statusLbl.setForeground(online ? COLOR_ONLINE : COLOR_OFFLINE);

        textArea.add(nameLbl);
        textArea.add(statusLbl);
        item.add(textArea, BorderLayout.CENTER);

        // Online dot
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dot.setForeground(online ? COLOR_ONLINE : COLOR_OFFLINE);
        dot.setVerticalAlignment(SwingConstants.CENTER);
        item.add(dot, BorderLayout.EAST);

        // Hover + click
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!peer.equals(selectedItem)) item.setBackground(COLOR_ITEM_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!peer.equals(selectedItem)) item.setBackground(COLOR_ITEM_NORMAL);
            }
            @Override public void mouseClicked(MouseEvent e) {
                selectedItem = peer;
                refreshPeerList(currentFilter());
                refreshRoomList(currentFilter());
                if (listener != null) listener.onPeerSelected(peer);
            }
        });

        return item;
    }

    // ── Room item ─────────────────────────────────────────────────
    private JPanel buildRoomItem(RoomInfo room) {
        boolean isSelected = room.equals(selectedItem);

        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(isSelected ? COLOR_ITEM_SELECTED : COLOR_ITEM_NORMAL);
        item.setBorder(new EmptyBorder(8, 10, 8, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar nhóm
        item.add(buildAvatar(room.name(), true), BorderLayout.WEST);

        // Text
        JPanel textArea = new JPanel(new GridLayout(2, 1, 0, 1));
        textArea.setOpaque(false);

        JLabel nameLbl = new JLabel(room.name());
        nameLbl.setFont(FONT_USERNAME);
        nameLbl.setForeground(COLOR_TEXT_PRIMARY);

        JLabel memberLbl = new JLabel("Nhóm chat");
        memberLbl.setFont(FONT_SUBTITLE);
        memberLbl.setForeground(COLOR_TEXT_SECONDARY);

        textArea.add(nameLbl);
        textArea.add(memberLbl);
        item.add(textArea, BorderLayout.CENTER);

        // Hover + click
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!room.equals(selectedItem)) item.setBackground(COLOR_ITEM_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!room.equals(selectedItem)) item.setBackground(COLOR_ITEM_NORMAL);
            }
            @Override public void mouseClicked(MouseEvent e) {
                selectedItem = room;
                refreshPeerList(currentFilter());
                refreshRoomList(currentFilter());
                if (listener != null) listener.onRoomSelected(room);
            }
        });

        return item;
    }

    // ── Avatar hình tròn ──────────────────────────────────────────
    private JPanel buildAvatar(String name, boolean isGroup) {
        Color bg = isGroup
                ? new Color(0x2E, 0xCC, 0x71)
                : AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];

        String letter = isGroup
                ? "\uD83D\uDC65"  // 👥
                : (name.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(name.charAt(0))));

        JPanel avatar = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setMinimumSize(new Dimension(36, 36));
        avatar.setMaximumSize(new Dimension(36, 36));

        JLabel lbl = new JLabel(letter, SwingConstants.CENTER);
        lbl.setFont(isGroup ? new Font("SansSerif", Font.PLAIN, 16) : FONT_AVATAR);
        lbl.setForeground(Color.WHITE);
        avatar.add(lbl);
        return avatar;
    }

    // ── Bottom toolbar ────────────────────────────────────────────
    private JPanel buildBottomToolbar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 6, 0));
        bar.setBackground(COLOR_SEARCH_BG);
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnHistory = buildToolButton("\uD83D\uDCDC L\u1ecbch s\u1eed"); // 📜 Lịch sử
        btnHistory.addActionListener(e -> {
            if (listener != null) listener.onHistoryRequested();
        });

        JButton btnCreate = buildToolButton("+ T\u1ea1o nh\u00f3m"); // + Tạo nhóm
        btnCreate.addActionListener(e -> {
            if (listener != null) listener.onCreateGroupRequested();
        });

        bar.add(btnHistory);
        bar.add(btnCreate);
        return bar;
    }

    private JButton buildToolButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(COLOR_TEXT_PRIMARY);
        btn.setBackground(COLOR_BTN_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 6, 7, 6));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_BTN_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(COLOR_BTN_BG);    }
        });
        return btn;
    }

    // ── Divider ───────────────────────────────────────────────────
    private JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_DIVIDER);
        sep.setBackground(COLOR_DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Cập nhật danh sách peer (gọi từ ChatFrame sau khi nhận data từ network).
     * Phải gọi trên EDT.
     */
    public void setPeers(List<PeerInfo> peers) {
        this.allPeers = new ArrayList<>(peers);
        refreshPeerList(currentFilter());
    }

    /**
     * Cập nhật danh sách room nhóm (gọi từ ChatFrame).
     * Phải gọi trên EDT.
     */
    public void setRooms(List<RoomInfo> rooms) {
        this.allRooms = new ArrayList<>(rooms);
        refreshRoomList(currentFilter());
    }

    /**
     * Chọn programmatically một peer (ví dụ khi nhận cuộc gọi đến).
     * Phải gọi trên EDT.
     */
    public void selectPeer(PeerInfo peer) {
        selectedItem = peer;
        refreshPeerList(currentFilter());
        refreshRoomList(currentFilter());
    }

    // ── Filtering ─────────────────────────────────────────────────

    private String currentFilter() {
        String t = searchField.getText();
        return t.equals("Tìm kiếm...") ? "" : t.toLowerCase();
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
                any = true;
            }
        }
        if (!any) peerListPanel.add(buildEmptyHint("Chưa có peer nào"));
        peerListPanel.revalidate();
        peerListPanel.repaint();
    }

    private void refreshRoomList(String filter) {
        roomListPanel.removeAll();
        boolean any = false;
        for (RoomInfo r : allRooms) {
            if (filter.isEmpty() || r.name().toLowerCase().contains(filter)) {
                roomListPanel.add(buildRoomItem(r));
                any = true;
            }
        }
        if (!any) roomListPanel.add(buildEmptyHint("Chưa có nhóm nào"));
        roomListPanel.revalidate();
        roomListPanel.repaint();
    }

    private JLabel buildEmptyHint(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(FONT_SUBTITLE);
        lbl.setForeground(COLOR_TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return lbl;
    }

    // ── Thin scroll bar (inner class, không cần thư viện ngoài) ──
    private static final class ThinScrollBarUI
            extends javax.swing.plaf.basic.BasicScrollBarUI {

        private static final Color THUMB = new Color(0x3A, 0x7B, 0xD5, 160);
        private static final Color TRACK = new Color(0x14, 0x1F, 0x33);

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
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
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
