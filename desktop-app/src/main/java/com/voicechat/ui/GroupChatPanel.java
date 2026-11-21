package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

/**
 * VoiceLink GroupChatPanel: Khung trò chuyện nhóm phong cách VoiceLink.
 * <p>Bố cục: Header phẳng hiện đại với avatar nhóm và nút gọi/thành viên bo tròn,
 * vùng cuộn tin nhắn ở giữa, danh sách thành viên ở bên phải và InputComposer ở đáy.</p>
 */
public final class GroupChatPanel extends JPanel {

    public interface ActionCallback {
        void onSendMessage(RoomInfo room, String text);
    }

    private final String        localUserId;
    private final JPanel        messageContainer = new MessageList();
    private final JPanel        memberListPanel  = new JPanel();
    private JScrollPane         scrollPane;
    private JLabel              memberCountLbl;
    private final InputComposer composer;
    private final UiUtils.RoundedButton membership = new UiUtils.RoundedButton(
            "Tham gia", UiUtils.C_PRIMARY_LIGHT, UiUtils.C_PRIMARY, UiUtils.ARC_SM);
    private java.util.Map<String,String> names = java.util.Map.of();
    private boolean joined;
    public void bindMembership(java.util.function.Consumer<Boolean> change) {
        membership.setVisible(true);
        membership.addActionListener(e -> change.accept(!joined));
        showMembership(false);
    }
    public void showMembership(boolean value) {
        joined = value;
        membership.setText(value ? "Rời phòng" : "Tham gia");
        composer.setVisible(value);
    }
    public void setDisplayNames(java.util.Map<String,String> names) { this.names = names; }
    public void restoreDraft(String text) {
        if (composer.getText().isEmpty()) composer.setText(text);
    }


    public GroupChatPanel(RoomInfo room, String localUserId, ActionCallback callback) {
        this.localUserId = localUserId;
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_APP_BG);

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
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(UiUtils.C_CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UiUtils.C_BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Trái: Avatar nhóm + Tên + Số thành viên
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(room.name(), 42, true, false, false);
        left.add(avatar);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(room.name());
        nameLbl.setFont(UiUtils.F_HEADING);
        nameLbl.setForeground(UiUtils.C_TEXT_PRIMARY);

        memberCountLbl = new JLabel("0 thành viên");
        memberCountLbl.setFont(UiUtils.F_SMALL);
        memberCountLbl.setForeground(UiUtils.C_TEXT_MUTED);

        info.add(nameLbl);
        info.add(memberCountLbl);
        left.add(info);
        header.add(left, BorderLayout.WEST);

        // Phải: Nút Gọi cả nhóm & Nút Thành viên
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        UiUtils.RoundedButton callGroupBtn = new UiUtils.RoundedButton(
                "Gọi cả nhóm", UiUtils.C_PRIMARY, Color.WHITE, UiUtils.ARC_MD);
        callGroupBtn.setIcon(UiIcon.PHONE);
        callGroupBtn.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        callGroupBtn.setPreferredSize(new Dimension(130, 36));

        UiUtils.RoundedButton membersBtn = new UiUtils.RoundedButton(
                "Thành viên", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_MD);
        membersBtn.setIcon(UiIcon.GROUP);
        membersBtn.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        membersBtn.setPreferredSize(new Dimension(116, 36));

        membersBtn.addActionListener(e -> {
            Component east = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.EAST);
            if (east != null) {
                east.setVisible(!east.isVisible());
                revalidate();
            }
        });

        right.add(callGroupBtn);
        right.add(membersBtn);
        membership.setVisible(false);
        right.add(membership);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ── Message area ──────────────────────────────────────────────
    private JScrollPane buildMessageArea() {
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

    // ── Member panel ──────────────────────────────────────────────
    private JPanel buildMemberPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiUtils.C_CARD_BG);
        panel.setPreferredSize(new Dimension(190, 0));
        panel.setBorder(new MatteBorder(0, 1, 0, 0, UiUtils.C_BORDER));

        JPanel sectionHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        sectionHdr.setBackground(UiUtils.C_CARD_BG);
        sectionHdr.setBorder(new MatteBorder(0, 0, 1, 0, UiUtils.C_BORDER));

        JLabel hdrLbl = new JLabel("THÀNH VIÊN");
        hdrLbl.setFont(UiUtils.F_SMALL_BOLD);
        hdrLbl.setForeground(UiUtils.C_TEXT_MUTED);
        sectionHdr.add(hdrLbl);
        panel.add(sectionHdr, BorderLayout.NORTH);

        memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.Y_AXIS));
        memberListPanel.setBackground(UiUtils.C_CARD_BG);
        memberListPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(memberListPanel);
        scroll.setBorder(null);
        scroll.setBackground(UiUtils.C_CARD_BG);
        scroll.getViewport().setBackground(UiUtils.C_CARD_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void setMembers(List<PeerInfo> members) {
        memberListPanel.removeAll();
        for (PeerInfo member : members) {

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            UiUtils.AvatarBadge av = new UiUtils.AvatarBadge(member.username(), 28, false, false, false);
            row.add(av);

            JLabel nameLbl = new JLabel(member.username());
            nameLbl.setFont(UiUtils.F_BODY);
            nameLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
            row.add(nameLbl);

            memberListPanel.add(row);
        }
        memberCountLbl.setText(members.size() + " thành viên");
        memberListPanel.revalidate();
        memberListPanel.repaint();
    }

    public void appendMessage(ChatMessage msg) {
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId, names.getOrDefault(msg.senderId(),msg.senderId()));
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageContainer.add(bubble);
        messageContainer.revalidate();
        scrollToBottom();
    }

    public void loadHistory(List<ChatMessage> history) {
        messageContainer.removeAll();
        for (ChatMessage msg : history) {
            ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId, names.getOrDefault(msg.senderId(),msg.senderId()));
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
