package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * TV3 – Bước 5: Panel chat nhóm cho một RoomInfo.
 * <p>Bố cục: header trên — vùng chat giữa — danh sách thành viên phải — InputComposer dưới.</p>
 */
public final class GroupChatPanel extends JPanel {

    public interface ActionCallback {
        void onSendMessage(RoomInfo room, String text);
    }

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG         = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_HEADER_BG  = Color.WHITE;
    private static final Color COLOR_BORDER     = new Color(0xD0, 0xD8, 0xE4);
    private static final Color COLOR_TEXT_PRI   = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_TEXT_SEC   = new Color(0x7F, 0x8C, 0x8D);
    private static final Color COLOR_MEMBERS_BG = new Color(0xEC, 0xF0, 0xF1);
    private static final Color COLOR_ACCENT     = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_SECTION    = new Color(0x7F, 0x8C, 0x8D);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_NAME    = new Font("SansSerif", Font.BOLD,  15);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD,  10);
    private static final Font FONT_MEMBER  = new Font("SansSerif", Font.PLAIN, 12);

    // ── Widgets ───────────────────────────────────────────────────
    private final String       localUserId;
    private final JPanel       messageContainer = new JPanel();
    private final JPanel       memberListPanel  = new JPanel();
    private JScrollPane        scrollPane;
    private JLabel             memberCountLbl;
    private final InputComposer composer;

    // ─────────────────────────────────────────────────────────────

    public GroupChatPanel(RoomInfo room, String localUserId, ActionCallback callback) {
        this.localUserId = localUserId;
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        add(buildHeader(room),    BorderLayout.NORTH);
        add(buildMessageArea(),   BorderLayout.CENTER);
        add(buildMemberPanel(),   BorderLayout.EAST);

        composer = new InputComposer(text -> {
            if (callback != null) callback.onSendMessage(room, text);
        });
        add(composer, BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader(RoomInfo room) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(UiUtils.buildAvatarCircle(room.name(), 40, true), BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(room.name());
        nameLbl.setFont(FONT_NAME);
        nameLbl.setForeground(COLOR_TEXT_PRI);

        memberCountLbl = new JLabel("0 thành viên");
        memberCountLbl.setFont(FONT_SUB);
        memberCountLbl.setForeground(COLOR_TEXT_SEC);

        info.add(nameLbl);
        info.add(memberCountLbl);
        left.add(info, BorderLayout.CENTER);
        header.add(left, BorderLayout.WEST);

        // Members button (toggle visibility)
        JButton membersBtn = new JButton("\uD83D\uDC65 Thành viên"); // 👥
        membersBtn.setFont(FONT_BTN);
        membersBtn.setForeground(Color.WHITE);
        membersBtn.setBackground(COLOR_ACCENT);
        membersBtn.setBorderPainted(false);
        membersBtn.setFocusPainted(false);
        membersBtn.setOpaque(true);
        membersBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        membersBtn.setBorder(new EmptyBorder(7, 12, 7, 12));
        membersBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                membersBtn.setBackground(new Color(0x2D, 0x64, 0xB8));
            }
            @Override public void mouseExited(MouseEvent e) {
                membersBtn.setBackground(COLOR_ACCENT);
            }
        });
        // Toggle member panel
        membersBtn.addActionListener(e -> {
            Component east = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.EAST);
            if (east != null) {
                east.setVisible(!east.isVisible());
                revalidate();
            }
        });
        header.add(membersBtn, BorderLayout.EAST);

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

    // ── Member panel ──────────────────────────────────────────────
    private JPanel buildMemberPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_MEMBERS_BG);
        panel.setPreferredSize(new Dimension(170, 0));

        // Section header
        JPanel sectionHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        sectionHdr.setBackground(COLOR_MEMBERS_BG);
        sectionHdr.setBorder(new MatteBorder(0, 1, 1, 0, COLOR_BORDER));
        JLabel hdrLbl = new JLabel("THÀNH VIÊN");
        hdrLbl.setFont(FONT_SECTION);
        hdrLbl.setForeground(COLOR_SECTION);
        sectionHdr.add(hdrLbl);
        panel.add(sectionHdr, BorderLayout.NORTH);

        // List
        memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.Y_AXIS));
        memberListPanel.setBackground(COLOR_MEMBERS_BG);

        JScrollPane scroll = new JScrollPane(memberListPanel);
        scroll.setBorder(new MatteBorder(0, 1, 0, 0, COLOR_BORDER));
        scroll.setBackground(COLOR_MEMBERS_BG);
        scroll.getViewport().setBackground(COLOR_MEMBERS_BG);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildMemberRow(PeerInfo member) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(COLOR_MEMBERS_BG);
        row.setBorder(new EmptyBorder(6, 10, 6, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        row.add(UiUtils.buildAvatarCircle(member.username(), 28, false), BorderLayout.WEST);

        JLabel nameLbl = new JLabel(member.username());
        nameLbl.setFont(FONT_MEMBER);
        nameLbl.setForeground(COLOR_TEXT_PRI);
        row.add(nameLbl, BorderLayout.CENTER);

        return row;
    }

    // ── Public API ────────────────────────────────────────────────

    /** Thêm tin nhắn mới. Phải gọi trên EDT. */
    public void appendMessage(ChatMessage msg) {
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId);
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageContainer.add(bubble);
        messageContainer.revalidate();
        scrollToBottom();
    }

    /** Tải lịch sử. Phải gọi trên EDT. */
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

    /** Cập nhật danh sách thành viên. Phải gọi trên EDT. */
    public void setMembers(List<PeerInfo> members) {
        memberListPanel.removeAll();
        for (PeerInfo m : members) {
            memberListPanel.add(buildMemberRow(m));
        }
        memberListPanel.revalidate();
        memberListPanel.repaint();
        memberCountLbl.setText(members.size() + " thành viên");
    }

    /** Đặt focus vào ô nhập tin nhắn. */
    public void focusComposer() {
        composer.focusInput();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
