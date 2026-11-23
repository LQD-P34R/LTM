package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

/**
 * VoiceLink DirectChatPanel: Khung chat 1-1 với một peer.
 * <p>
 * Bố cục: Header phẳng hiện đại với avatar và nút gọi bo tròn,
 * vùng cuộn tin nhắn thoáng đãng và InputComposer dạng viên thuốc ở đáy.
 * </p>
 */
public final class DirectChatPanel extends JPanel {

    public interface ActionCallback {
        void onSendMessage(PeerInfo peer, String text);

        void onCallRequested(PeerInfo peer);
    }

    private final String localUserId;
    private final JPanel messageContainer = new JPanel();
    private JScrollPane scrollPane;
    private UiUtils.AvatarBadge avatar;
    private JLabel statusLbl;
    private JButton callBtn;
    private final InputComposer composer;
    private boolean connected = true;
    private boolean sending;

    public DirectChatPanel(PeerInfo peer, String localUserId, ActionCallback callback) {
        this.localUserId = localUserId;
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_APP_BG);

        add(buildHeader(peer, callback), BorderLayout.NORTH);
        add(buildMessageArea(), BorderLayout.CENTER);

        composer = new InputComposer(text -> {
            if (callback != null)
                callback.onSendMessage(peer, text);
        });
        add(composer, BorderLayout.SOUTH);
    }

    // ── Header phẳng hiện đại ─────────────────────────────────────
    private JPanel buildHeader(PeerInfo peer, ActionCallback callback) {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(UiUtils.C_CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UiUtils.C_BORDER),
                new EmptyBorder(12, 20, 12, 20)));

        // Bên trái: AvatarBadge + Tên + Trạng thái
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        boolean isOnline = true; // Danh bạ chỉ gồm peer đang kết nối.
        avatar = new UiUtils.AvatarBadge(peer.username(), 42, false, isOnline, true);
        left.add(avatar);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(peer.username());
        nameLbl.setFont(UiUtils.F_HEADING);
        nameLbl.setForeground(UiUtils.C_TEXT_PRIMARY);

        statusLbl = new JLabel(isOnline ? "● Đang hoạt động" : "○ Ngoại tuyến");
        statusLbl.setFont(UiUtils.F_SMALL);
        statusLbl.setForeground(isOnline ? UiUtils.C_ONLINE : UiUtils.C_TEXT_MUTED);

        info.add(nameLbl);
        info.add(statusLbl);
        left.add(info);
        header.add(left, BorderLayout.WEST);

        // Bên phải: Nút gọi thoại bo tròn
        callBtn = new UiUtils.RoundedButton(
                "\uD83D\uDCDE  Gọi thoại", UiUtils.C_ONLINE, Color.WHITE, UiUtils.ARC_MD);
        callBtn.setPreferredSize(new Dimension(116, 36));
        callBtn.addActionListener(e -> {
            if (callback != null)
                callback.onCallRequested(peer);
        });
        header.add(callBtn, BorderLayout.EAST);

        return header;
    }

    // ── Message area ──────────────────────────────────────────────
    public void setConnected(boolean online) {
        connected = online;
        avatar.setOnline(online);
        statusLbl.setText(online ? "● Đang hoạt động" : "○ Ngoại tuyến");
        statusLbl.setForeground(online ? UiUtils.C_ONLINE : UiUtils.C_TEXT_MUTED);
        callBtn.setEnabled(online);
        composer.setInputEnabled(connected && !sending);
    }

    public void setSending(boolean sending) {
        this.sending = sending;
        composer.setInputEnabled(connected && !sending);
    }

    public void restoreFailedMessage(String text) {
        setSending(false);
        composer.setText(text);
        if (connected) composer.requestInputFocus();
    }

    private JScrollPane buildMessageArea() {
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(UiUtils.C_APP_BG);
        messageContainer.setBorder(new EmptyBorder(12, 0, 12, 0));

        scrollPane = new JScrollPane(messageContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UiUtils.C_APP_BG);
        scrollPane.getViewport().setBackground(UiUtils.C_APP_BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public void appendMessage(ChatMessage msg) {
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId);
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageContainer.add(bubble);
        messageContainer.revalidate();
        scrollToBottom();
    }

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

    public void clearMessages() {
        messageContainer.removeAll();
        messageContainer.revalidate();
        messageContainer.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }
}
