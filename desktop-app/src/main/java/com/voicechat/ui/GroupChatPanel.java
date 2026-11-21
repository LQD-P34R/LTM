package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import com.voicechat.common.model.PeerInfo;
import com.voicechat.common.model.RoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * VoiceLink GroupChatPanel: Khung trò chuyện nhóm phong cách VoiceLink.
 * <p>Bố cục: Header phẳng hiện đại với avatar nhóm và nút gọi/thành viên bo tròn,
 * vùng cuộn tin nhắn ở giữa, danh sách thành viên ở bên phải và InputComposer ở đáy.</p>
 */
public final class GroupChatPanel extends JPanel {

    public interface ActionCallback {
        void onSendMessage(RoomInfo room, String text);
        default void onGroupCallRequested(RoomInfo room) { }
    }

    private final String        localUserId;
    private final JPanel        messageContainer = new JPanel();
    private final JPanel        messageStack = new MessageViewportPanel();
    private JScrollPane         scrollPane;
    private JLabel              memberCountLbl;
    private final InputComposer composer;
    private final ActionCallback callback;
    private LocalDate lastRenderedDate;

    public GroupChatPanel(RoomInfo room, String localUserId, ActionCallback callback) {
        this.localUserId = localUserId;
        this.callback = callback;
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_CARD_BG);
        setOpaque(true);

        add(buildHeader(room),    BorderLayout.NORTH);
        add(buildMessageArea(),   BorderLayout.CENTER);
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
                new EmptyBorder(11, 20, 11, 20)
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

        memberCountLbl = new JLabel("5 thành viên  •  3 đang online");
        memberCountLbl.setFont(UiUtils.F_SMALL);
        memberCountLbl.setForeground(UiUtils.C_TEXT_MUTED);

        info.add(nameLbl);
        info.add(memberCountLbl);
        left.add(info);
        header.add(left, BorderLayout.WEST);

        // Phải: tìm kiếm và nút gọi nhóm dạng viên thuốc.
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        UiUtils.RoundedButton callGroupBtn = new UiUtils.RoundedButton(
                "Bắt đầu gọi nhóm", UiUtils.C_PRIMARY, UiUtils.C_TEXT_PRIMARY, 12);
        callGroupBtn.setIcon(new AppIcons(AppIcons.Kind.HEADSET, 16, UiUtils.C_TEXT_PRIMARY));
        callGroupBtn.setPreferredSize(new Dimension(176, 36));
        callGroupBtn.setMinimumSize(new Dimension(176, 36));
        callGroupBtn.setMaximumSize(new Dimension(176, 36));
        callGroupBtn.addActionListener(e->{if(callback!=null)callback.onGroupCallRequested(room);});
        JButton search=new JButton(new AppIcons(AppIcons.Kind.SEARCH,18,UiUtils.C_TEXT_MUTED));
        search.setPreferredSize(new Dimension(36,36)); search.setToolTipText("Tìm trong cuộc trò chuyện");
        search.setBorderPainted(false); search.setContentAreaFilled(false); search.setFocusPainted(false);
        right.add(search);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ── Message area ──────────────────────────────────────────────
    private JScrollPane buildMessageArea() {
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
        messageContainer.setBackground(UiUtils.C_CARD_BG);
        messageContainer.setOpaque(true);
        messageContainer.setBorder(new EmptyBorder(12, 16, 12, 4));

        messageStack.setBackground(UiUtils.C_CARD_BG);
        messageStack.setOpaque(true);
        messageStack.add(messageContainer, BorderLayout.CENTER);
        scrollPane = new JScrollPane(messageStack);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(UiUtils.C_CARD_BG);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setBackground(UiUtils.C_CARD_BG);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public void setMembers(List<PeerInfo> members) {
        memberCountLbl.setText(members.size() + " thành viên");
    }

    private void appendRow(ChatMessage msg) {
        LocalDate date=msg.sentAt().atZone(ZoneId.systemDefault()).toLocalDate();
        if (!date.equals(lastRenderedDate)) { messageContainer.add(dateBadge(date)); lastRenderedDate=date; }
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(msg, localUserId);
        boolean sentByMe=msg.senderId().equals(localUserId);
        JPanel row = new JPanel(new FlowLayout(sentByMe?FlowLayout.RIGHT:FlowLayout.LEFT,20,4)); row.setOpaque(false);
        row.add(bubble);
        messageContainer.add(row);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,row.getPreferredSize().height));
        messageContainer.add(Box.createVerticalStrut(8));
    }
    private JComponent dateBadge(LocalDate date) {
        String[] days={"CN","T2","T3","T4","T5","T6","T7"};
        String text=days[date.getDayOfWeek().getValue()%7]+" "+date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JPanel outer=new JPanel(new FlowLayout(FlowLayout.CENTER,0,10)); outer.setOpaque(false);
        UiUtils.RoundedButton pill=new UiUtils.RoundedButton(text,UiUtils.C_PRIMARY_LIGHT,UiUtils.C_TEXT_PRIMARY,12);
        pill.setFont(UiTheme.SEMIBOLD_FONT.deriveFont(11.5f)); pill.setBorder(new EmptyBorder(4,12,4,12)); pill.setFocusable(false);
        outer.add(pill); outer.setMaximumSize(new Dimension(Integer.MAX_VALUE,pill.getPreferredSize().height+20)); return outer;
    }

    public void appendMessage(ChatMessage msg) {
        appendRow(msg);
        messageContainer.revalidate();
        scrollToBottom();
    }

    public void loadHistory(List<ChatMessage> history) {
        messageContainer.removeAll();
        lastRenderedDate=null;
        for (ChatMessage msg : history) {
            appendRow(msg);
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

    private static final class MessageViewportPanel extends JPanel implements Scrollable {
        MessageViewportPanel() { super(new BorderLayout()); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction) { return Math.max(16,visibleRect.height-16); }
    }
}
