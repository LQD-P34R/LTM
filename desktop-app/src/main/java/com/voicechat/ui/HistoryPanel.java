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
 * TV3 – Bước 6: Panel hiển thị lịch sử tất cả tin nhắn dưới dạng bảng.
 * <p>Nhận dữ liệu qua {@link #loadHistory(List)}.
 * Không tự gọi {@code HistoryStore} — ChatFrame truyền danh sách vào.</p>
 */
public final class HistoryPanel extends JPanel {

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG        = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_HEADER_BG = Color.WHITE;
    private static final Color COLOR_BORDER    = new Color(0xD0, 0xD8, 0xE4);
    private static final Color COLOR_ROW_ODD   = Color.WHITE;
    private static final Color COLOR_ROW_EVEN  = new Color(0xF8, 0xF9, 0xFB);
    private static final Color COLOR_ROW_SEL   = new Color(0xD6, 0xE8, 0xFB);
    private static final Color COLOR_GRID      = new Color(0xEC, 0xF0, 0xF1);
    private static final Color COLOR_TEXT_PRI  = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_TEXT_SEC  = new Color(0x7F, 0x8C, 0x8D);
    private static final Color COLOR_CLEAR_FG  = new Color(0xE7, 0x4C, 0x3C);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  16);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_COL_HDR = new Font("SansSerif", Font.BOLD,  11);

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM  HH:mm").withZone(ZoneId.systemDefault());

    private static final String[] COLUMNS =
            {"Thời gian", "Người gửi", "Phòng / Peer", "Nội dung"};

    // ── Widgets ───────────────────────────────────────────────────
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table    = new JTable(tableModel);
    private final JLabel countLbl = new JLabel("0 tin nhắn");

    private List<ChatMessage> allMessages = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);

        JLabel titleLbl = new JLabel("\uD83D\uDCDC L\u1ECBch s\u1EED cu\u1ED9c tr\u00F2 chuy\u1EC7n"); // 📜 Lịch sử cuộc trò chuyện
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(COLOR_TEXT_PRI);

        countLbl.setFont(FONT_SUB);
        countLbl.setForeground(COLOR_TEXT_SEC);

        left.add(titleLbl);
        left.add(countLbl);
        header.add(left, BorderLayout.WEST);

        JButton clearBtn = new JButton("Xo\u00E1 hi\u1EC3n th\u1ECB"); // "Xoá hiển thị"
        clearBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        clearBtn.setForeground(COLOR_CLEAR_FG);
        clearBtn.setBackground(COLOR_HEADER_BG);
        clearBtn.setBorder(BorderFactory.createLineBorder(COLOR_CLEAR_FG, 1, true));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearHistory());
        header.add(clearBtn, BorderLayout.EAST);

        return header;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        table.setFont(FONT_TABLE);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(COLOR_GRID);
        table.setSelectionBackground(COLOR_ROW_SEL);
        table.setSelectionForeground(COLOR_TEXT_PRI);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel colModel = table.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(110);
        colModel.getColumn(1).setPreferredWidth(100);
        colModel.getColumn(2).setPreferredWidth(130);
        colModel.getColumn(3).setPreferredWidth(500);

        // Header style
        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_COL_HDR);
        th.setBackground(COLOR_HEADER_BG);
        th.setForeground(COLOR_TEXT_SEC);
        th.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));
        th.setReorderingAllowed(false);

        // Alternating row renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSel, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setFont(FONT_TABLE);
                if (isSel) {
                    setBackground(COLOR_ROW_SEL);
                    setForeground(COLOR_TEXT_PRI);
                } else {
                    setBackground(row % 2 == 0 ? COLOR_ROW_ODD : COLOR_ROW_EVEN);
                    setForeground(col == 3 ? COLOR_TEXT_PRI : COLOR_TEXT_SEC);
                }
                return this;
            }
        };
        for (int i = 0; i < COLUMNS.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(COLOR_BG);
        return scroll;
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Tải danh sách tin nhắn vào bảng.
     * Phải gọi trên EDT.
     */
    public void loadHistory(List<ChatMessage> messages) {
        this.allMessages = new ArrayList<>(messages);
        tableModel.setRowCount(0);
        for (ChatMessage msg : messages) {
            String timeStr  = DT_FMT.format(msg.sentAt());
            String sender   = msg.senderId();
            String room     = msg.roomId();
            String preview  = msg.text().length() > 72
                    ? msg.text().substring(0, 72) + "\u2026"
                    : msg.text();
            tableModel.addRow(new Object[]{timeStr, sender, room, preview});
        }
        countLbl.setText(messages.size() + " tin nh\u1EAFn");
    }

    /**
     * Xoá toàn bộ hiển thị (không ảnh hưởng dữ liệu trong HistoryStore).
     * Phải gọi trên EDT.
     */
    public void clearHistory() {
        allMessages.clear();
        tableModel.setRowCount(0);
        countLbl.setText("0 tin nh\u1EAFn");
    }
}
