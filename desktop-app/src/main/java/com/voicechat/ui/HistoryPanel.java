package com.voicechat.ui;

import com.voicechat.common.model.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * VoiceLink HistoryPanel: Giao diện xem lịch sử cuộc trò chuyện phẳng hiện đại,
 * loại bỏ hoàn toàn các đường kẻ lưới thô cứng mặc định của JTable.
 */
public final class HistoryPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM  HH:mm").withZone(ZoneId.systemDefault());

    private static final String[] COLUMNS =
            {"Thời gian", "Người gửi", "Phòng / Peer", "Nội dung"};

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table    = new JTable(tableModel);
    private final JLabel countLbl = new JLabel("0 tin nhắn");
    private List<ChatMessage> allMessages = new ArrayList<>();

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.C_APP_BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
    }

    // ── Header phẳng hiện đại ─────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(UiUtils.C_CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UiUtils.C_BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel titleLbl = new JLabel("Lịch sử cuộc trò chuyện",UiIcon.HISTORY,SwingConstants.LEADING);
        titleLbl.setFont(UiUtils.F_TITLE_LG);
        titleLbl.setForeground(UiUtils.C_TEXT_PRIMARY);

        countLbl.setFont(UiUtils.F_SMALL);
        countLbl.setForeground(UiUtils.C_TEXT_MUTED);

        left.add(titleLbl);
        left.add(countLbl);
        header.add(left, BorderLayout.WEST);

        UiUtils.RoundedButton clearBtn = new UiUtils.RoundedButton(
                "Xoá hiển thị", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_DANGER, UiUtils.ARC_SM);
        clearBtn.setPreferredSize(new Dimension(110, 34));
        clearBtn.addActionListener(e -> clearHistory());
        header.add(clearBtn, BorderLayout.EAST);

        return header;
    }

    // ── Table phẳng (không viền lưới thô) ─────────────────────────
    private JScrollPane buildTable() {
        table.setFont(UiUtils.F_BODY);
        table.setRowHeight(42);
        table.setShowGrid(false); // Loại bỏ đường kẻ lưới thô của Swing mặc định
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(UiUtils.C_SELECTED_BG);
        table.setSelectionForeground(UiUtils.C_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setBackground(UiUtils.C_CARD_BG);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel colModel = table.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(120);
        colModel.getColumn(1).setPreferredWidth(120);
        colModel.getColumn(2).setPreferredWidth(140);
        colModel.getColumn(3).setPreferredWidth(520);

        // Header bảng phẳng hiện đại
        JTableHeader th = table.getTableHeader();
        th.setFont(UiUtils.F_SMALL_BOLD);
        th.setBackground(UiUtils.C_SIDEBAR_LIGHT);
        th.setForeground(UiUtils.C_TEXT_MUTED);
        th.setBorder(new MatteBorder(0, 0, 1, 0, UiUtils.C_BORDER));
        th.setPreferredSize(new Dimension(0, 36));
        th.setReorderingAllowed(false);

        // Renderer từng dòng với padding rộng rãi và màu nền xen kẽ nhẹ
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 16, 0, 16));
                setFont(UiUtils.F_BODY);
                if (isSel) {
                    setBackground(UiUtils.C_SELECTED_BG);
                    setForeground(UiUtils.C_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : UiUtils.C_APP_BG);
                    setForeground(col == 3 ? UiUtils.C_TEXT_PRIMARY : UiUtils.C_TEXT_MUTED);
                }
                return this;
            }
        };
        for (int i = 0; i < COLUMNS.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(UiUtils.C_APP_BG);
        scroll.getViewport().setBackground(UiUtils.C_APP_BG);
        return scroll;
    }

    public void loadHistory(List<ChatMessage> messages) {
        this.allMessages = new ArrayList<>(messages);
        tableModel.setRowCount(0);
        for (ChatMessage msg : messages) {
            tableModel.addRow(new Object[]{
                DT_FMT.format(msg.sentAt()),
                msg.senderId(),
                msg.roomId().isEmpty() ? "Chat 1-1" : msg.roomId(),
                msg.text()
            });
        }
        countLbl.setText(messages.size() + " tin nhắn");
    }

    public void clearHistory() {
        tableModel.setRowCount(0);
        countLbl.setText("0 tin nhắn");
    }

    public int getRowCount() {
        return tableModel.getRowCount();
    }
}
