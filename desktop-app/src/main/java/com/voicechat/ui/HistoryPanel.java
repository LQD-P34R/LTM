package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Visual call and message history cards, without Swing's default table. */
public final class HistoryPanel extends JPanel {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM  HH:mm").withZone(ZoneId.systemDefault());
    private final JLabel countLbl = new JLabel("0 tin nhắn");
    private final JPanel messagesList = new JPanel();
    private final JPanel callsList = new JPanel();
    private final JPanel callFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    private final JTextField search = new UiUtils.RoundedTextField("Tìm trong lịch sử...");
    private final List<ChatMessage> allMessages = new ArrayList<>();
    private String callFilter = "Tất cả";
    private Consumer<String> conversationListener = id -> {};

    public HistoryPanel() {
        setLayout(new BorderLayout(0, 16)); setBackground(UiUtils.C_APP_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(), BorderLayout.NORTH);
        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0)); columns.setOpaque(false);
        columns.add(buildCallsCard()); columns.add(buildMessagesCard());
        add(columns, BorderLayout.CENTER);
        fillSampleCalls();
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){ refreshMessages(); }
            public void removeUpdate(DocumentEvent e){ refreshMessages(); }
            public void changedUpdate(DocumentEvent e){ refreshMessages(); }
        });
    }

    public void setConversationListener(Consumer<String> listener) {
        conversationListener = listener == null ? id -> {} : listener;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel title = new JPanel(); title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Lịch sử", new AppIcons(AppIcons.Kind.HISTORY, 20, UiUtils.C_PRIMARY_TEXT), SwingConstants.LEFT);
        heading.setFont(UiUtils.F_TITLE_XL); heading.setForeground(UiUtils.C_TEXT_PRIMARY);
        countLbl.setFont(UiUtils.F_SMALL); countLbl.setForeground(UiUtils.C_TEXT_MUTED);
        JLabel subtitle=sectionTitle("Cuộc gọi và tin nhắn gần đây của bạn");
        subtitle.setFont(UiUtils.F_SMALL); subtitle.setForeground(UiUtils.C_TEXT_MUTED);
        title.add(heading); title.add(Box.createVerticalStrut(3)); title.add(subtitle); title.add(countLbl);
        header.add(title, BorderLayout.WEST);
        search.setPreferredSize(new Dimension(260,36));
        JPanel searchRow=new JPanel(new BorderLayout()); searchRow.setOpaque(false);
        JLabel icon=new JLabel(new AppIcons(AppIcons.Kind.SEARCH,16,UiUtils.C_TEXT_MUTED));
        icon.setBorder(new EmptyBorder(0,10,0,6)); searchRow.add(icon,BorderLayout.WEST); searchRow.add(search,BorderLayout.CENTER);
        header.add(searchRow,BorderLayout.EAST);
        return header;
    }

    private JPanel buildCallsCard() {
        JPanel card = card(); card.setLayout(new BorderLayout(0, 10)); card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel title = sectionTitle("Cuộc gọi gần đây"); card.add(title, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 10)); body.setOpaque(false);
        callFilters.setOpaque(false);
        refreshCallFilters();
        body.add(callFilters, BorderLayout.NORTH);
        callsList.setOpaque(false); callsList.setLayout(new BoxLayout(callsList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(callsList); styleScroll(scroll); body.add(scroll, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER); return card;
    }

    private void refreshCallFilters() {
        callFilters.removeAll();
        for (String filter : List.of("Tất cả", "Cuộc gọi đi", "Cuộc gọi đến", "Cuộc gọi nhỡ")) {
            UiUtils.RoundedButton pill = new UiUtils.RoundedButton(filter,
                    filter.equals(callFilter) ? UiUtils.C_PRIMARY_LIGHT : UiUtils.C_SIDEBAR_LIGHT,
                    filter.equals(callFilter) ? UiUtils.C_PRIMARY_TEXT : UiUtils.C_TEXT_MUTED, UiUtils.ARC_XL);
            pill.setFont(UiUtils.F_SMALL); pill.setMargin(new Insets(4, 8, 4, 8));
            pill.setBorder(new EmptyBorder(4, 6, 4, 6));
            pill.addActionListener(e -> { callFilter = filter; refreshCallFilters(); refreshCalls(); }); callFilters.add(pill);
        }
        callFilters.revalidate(); callFilters.repaint();
    }

    private JPanel buildMessagesCard() {
        JPanel card = card(); card.setLayout(new BorderLayout(0, 12)); card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
        JLabel title=sectionTitle("Lịch sử tin nhắn"); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle=sectionTitle("Mở một cuộc trò chuyện để xem lại toàn bộ");
        subtitle.setFont(UiUtils.F_SMALL); subtitle.setForeground(UiUtils.C_TEXT_MUTED); subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(title); top.add(Box.createVerticalStrut(3)); top.add(subtitle); card.add(top, BorderLayout.NORTH);
        messagesList.setOpaque(false); messagesList.setLayout(new BoxLayout(messagesList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(messagesList); styleScroll(scroll); card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel card() {
        UiUtils.RoundedPanel panel = new UiUtils.RoundedPanel(16, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        panel.setOpaque(false); return panel;
    }
    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text); label.setFont(UiUtils.F_HEADING); label.setForeground(UiUtils.C_TEXT_PRIMARY); return label;
    }
    private void styleScroll(JScrollPane scroll) {
        scroll.setBorder(null); scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setOpaque(false); scroll.setOpaque(false);
    }

    private void fillSampleCalls() {
        addCall("Quốc Bảo", "Cuộc gọi đi", "Hôm nay, 10:42", "12:34", AppIcons.Kind.ARROW_OUT, UiUtils.C_PRIMARY_TEXT);
        addCall("Thu Hà", "Cuộc gọi đến", "Hôm qua, 20:16", "08:21", AppIcons.Kind.ARROW_IN, UiUtils.C_PRIMARY_TEXT);
        addCall("Lan Phương", "Cuộc gọi nhỡ", "Hôm qua, 18:03", "", AppIcons.Kind.ARROW_MISSED, UiUtils.C_DANGER);
        addCall("Đức Anh", "Cuộc gọi đi", "20/11, 14:50", "03:18", AppIcons.Kind.ARROW_OUT, UiUtils.C_PRIMARY_TEXT);
    }
    private void addCall(String name, String type, String time, String duration, AppIcons.Kind icon, Color color) {
        UiUtils.RoundedPanel item = new UiUtils.RoundedPanel(12, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        item.setLayout(new BorderLayout(10, 0)); item.setBorder(new EmptyBorder(14, 14, 14, 14));
        UiUtils.AvatarBadge avatar = new UiUtils.AvatarBadge(name, 40, false, false, true); item.add(avatar, BorderLayout.WEST);
        JPanel info = new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel who = new JLabel(name); who.setFont(UiUtils.F_BODY_BOLD); who.setForeground(UiUtils.C_TEXT_PRIMARY);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel kindLine = new JPanel(); kindLine.setOpaque(false); kindLine.setLayout(new BoxLayout(kindLine, BoxLayout.X_AXIS));
        kindLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        kindLine.add(new JLabel(new AppIcons(icon, 14, color))); kindLine.add(Box.createHorizontalStrut(5));
        JLabel kind = new JLabel(type); kind.setFont(UiUtils.F_SMALL); kind.setForeground(UiUtils.C_TEXT_MUTED);
        kind.setAlignmentX(Component.LEFT_ALIGNMENT);
        kindLine.add(kind); info.add(who); info.add(Box.createVerticalStrut(3)); info.add(kindLine);
        item.add(info, BorderLayout.CENTER);
        JPanel when = new JPanel(); when.setOpaque(false); when.setLayout(new BoxLayout(when, BoxLayout.Y_AXIS));
        when.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel at = new JLabel(time); at.setFont(UiUtils.F_SMALL); at.setForeground(UiUtils.C_TEXT_MUTED);
        JLabel length = new JLabel(duration); length.setFont(UiUtils.F_SMALL_BOLD); length.setForeground(UiUtils.C_TEXT_PRIMARY);
        at.setHorizontalAlignment(SwingConstants.RIGHT); at.setAlignmentX(Component.RIGHT_ALIGNMENT);
        length.setHorizontalAlignment(SwingConstants.RIGHT); length.setAlignmentX(Component.RIGHT_ALIGNMENT);
        when.add(at); when.add(Box.createVerticalStrut(5)); when.add(length); item.add(when, BorderLayout.EAST);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        callsList.add(item); callsList.add(Box.createVerticalStrut(8));
    }
    private void refreshCalls() {
        callsList.removeAll(); fillSampleCallsFiltered(); callsList.revalidate(); callsList.repaint();
    }
    private void fillSampleCallsFiltered() {
        if (callFilter.equals("Tất cả") || callFilter.equals("Cuộc gọi đi")) addCall("Quốc Bảo", "Cuộc gọi đi", "Hôm nay, 10:42", "12:34", AppIcons.Kind.ARROW_OUT, UiUtils.C_PRIMARY_TEXT);
        if (callFilter.equals("Tất cả") || callFilter.equals("Cuộc gọi đến")) addCall("Thu Hà", "Cuộc gọi đến", "Hôm qua, 20:16", "08:21", AppIcons.Kind.ARROW_IN, UiUtils.C_PRIMARY_TEXT);
        if (callFilter.equals("Tất cả") || callFilter.equals("Cuộc gọi nhỡ")) addCall("Lan Phương", "Cuộc gọi nhỡ", "Hôm qua, 18:03", "", AppIcons.Kind.ARROW_MISSED, UiUtils.C_DANGER);
        if (callFilter.equals("Tất cả") || callFilter.equals("Cuộc gọi đi")) addCall("Đức Anh", "Cuộc gọi đi", "20/11, 14:50", "03:18", AppIcons.Kind.ARROW_OUT, UiUtils.C_PRIMARY_TEXT);
    }

    public void loadHistory(List<ChatMessage> messages) {
        allMessages.clear(); allMessages.addAll(messages); countLbl.setText(messages.size() + " tin nhắn"); refreshMessages();
    }
    private void refreshMessages() {
        messagesList.removeAll(); String query = search.getText().trim().toLowerCase();
        for (ChatMessage msg : allMessages) {
            if (!query.isEmpty() && !msg.senderId().toLowerCase().contains(query) && !msg.text().toLowerCase().contains(query)) continue;
            UiUtils.RoundedPanel item = new UiUtils.RoundedPanel(12, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
            item.setLayout(new BorderLayout(10, 0)); item.setBorder(new EmptyBorder(14, 14, 14, 14));
            item.add(new UiUtils.AvatarBadge(msg.senderId(), 38, false, false, true), BorderLayout.WEST);
            JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            JLabel who = new JLabel(msg.senderId()); who.setFont(UiUtils.F_BODY_BOLD); who.setForeground(UiUtils.C_TEXT_PRIMARY);
            JLabel preview = new JLabel(msg.text()); preview.setFont(UiUtils.F_SMALL); preview.setForeground(UiUtils.C_TEXT_MUTED);
            JLabel date = new JLabel(DT_FMT.format(msg.sentAt())); date.setFont(UiUtils.F_SMALL); date.setForeground(UiUtils.C_TEXT_HINT);
            text.add(who); text.add(Box.createVerticalStrut(3)); text.add(preview); text.add(Box.createVerticalStrut(3)); text.add(date);
            item.add(text, BorderLayout.CENTER); item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { conversationListener.accept(msg.roomId()); } });
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82)); messagesList.add(item); messagesList.add(Box.createVerticalStrut(8));
        }
        if (messagesList.getComponentCount() == 0) {
            JLabel empty = new JLabel(allMessages.isEmpty() ? "Chưa có lịch sử tin nhắn" : "Không tìm thấy kết quả");
            empty.setForeground(UiUtils.C_TEXT_MUTED); empty.setBorder(new EmptyBorder(18, 6, 18, 6)); messagesList.add(empty);
        }
        messagesList.revalidate(); messagesList.repaint();
    }
    public void clearHistory() { loadHistory(List.of()); }
    public int getRowCount() { return allMessages.size(); }
}
