package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * TV3 – Bước 4: Panel chat 1-1 với một peer.
 * <p>Hiển thị header (avatar, tên, trạng thái, nút gọi),
 * vùng cuộn tin nhắn với {@link ChatBubbleRenderer},
 * và {@link InputComposer} để nhập tin.</p>
 *
 * <p>Giao tiếp với ChatFrame qua {@link ActionCallback}.</p>
 */
public final class DirectChatPanel extends JPanel {

    public interface ActionCallback {
        void onSendMessage(PeerInfo peer, String text);
        void onCallRequested(PeerInfo peer);
    }

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG        = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_BORDER    = new Color(0xD0, 0xD8, 0xE4);
    private static final Color COLOR_ONLINE    = new Color(0x2E, 0xCC, 0x71);
    private static final Color COLOR_OFFLINE   = new Color(0x95, 0xA5, 0xA6);
    private static final Color COLOR_TEXT_PRI  = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_CALL_BTN  = new Color(0x27, 0xAE, 0x60);
    private static final Color COLOR_CALL_HOV  = new Color(0x1E, 0x8A, 0x49);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_NAME   = new Font("SansSerif", Font.BOLD,  15);
    private static final Font FONT_STATUS = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD,  12);

    // ── Widgets ───────────────────────────────────────────────────
    private final String       localUserId;
    private final JPanel       messageContainer = new JPanel();
    private JScrollPane        scrollPane;
    private final InputComposer composer;

    // ─────────────────────────────────────────────────────────────

    public DirectChatPanel(PeerInfo peer, String localUserId, ActionCallback callback) {
        this.localUserId = localUserId;
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        add(buildHeader(peer, callback), BorderLayout.NORTH);
        add(buildMessageArea(),          BorderLayout.CENTER);

        composer = new InputComposer(text -> {
            if (callback != null) callback.onSendMessage(peer, text);
        });
        add(composer, BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader(PeerInfo peer, ActionCallback callback) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Left: avatar + name + status
        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(UiUtils.buildAvatarCircle(peer.username(), 40, false), BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(peer.username());
        nameLbl.setFont(FONT_NAME);
        nameLbl.setForeground(COLOR_TEXT_PRI);

        boolean online = (Math.abs(peer.username().hashCode()) % 3 != 0);
        JLabel statusLbl = new JLabel(online ? "● Đang hoạt động" : "○ Đang offline");
        statusLbl.setFont(FONT_STATUS);
        statusLbl.setForeground(online ? COLOR_ONLINE : COLOR_OFFLINE);

        info.add(nameLbl);
        info.add(statusLbl);
        left.add(info, BorderLayout.CENTER);
        header.add(left, BorderLayout.WEST);

        // Right: Call button
        JButton callBtn = new JButton("\uD83D\uDCDE G\u1ECDi"); // 📞 Gọi
        callBtn.setFont(FONT_BTN);
        callBtn.setForeground(Color.WHITE);
        callBtn.setBackground(COLOR_CALL_BTN);
        callBtn.setBorderPainted(false);
        callBtn.setFocusPainted(false);
        callBtn.setOpaque(true);
        callBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        callBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        callBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { callBtn.setBackground(COLOR_CALL_HOV); }
            @Override public void mouseExited(MouseEvent e)  { callBtn.setBackground(COLOR_CALL_BTN); }
        });
        callBtn.addActionListener(e -> {
            if (callback != null) callback.onCallRequested(peer);
        });
        header.add(callBtn, BorderLayout.EAST);

        return header;
    }

    // ── Message area ──────────────────────────────────────────────
    private JScrollPane buildMessageArea() {
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(COLOR_BG);
        messageContainer.setBorder(new EmptyBorder(8, 0, 8, 0));

        scrollPane = new JScrollPane(messageContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Thêm một tin nhắn mới vào cuối danh sách.
     * Phải gọi trên EDT.
     */
    public void appendMessage(ChatMessage msg) {
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId);
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageContainer.add(bubble);
        messageContainer.revalidate();
        scrollToBottom();
    }

    /**
     * Xoá toàn bộ và tải lại lịch sử.
     * Phải gọi trên EDT.
     */
    public void loadHistory(List<ChatMessage> history) {
        messageContainer.removeAll();
        for (ChatMessage msg : history) {
            ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId);
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
            messageContainer.add(bubble);
        }
        messageContainer.revalidate();
        messageContainer.repaint();
        scrollToBottom();
    }

    /** Đặt focus vào ô nhập tin nhắn. */
    public void focusComposer() {
        composer.focusInput();
    }

    // ── Nội bộ ───────────────────────────────────────────────────
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
