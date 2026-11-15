package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * TV3 – Bước 9: Dialog hiển thị khi nhận cuộc gọi đến (CALL_REQUEST).
 * <p>Modal dialog với tên người gọi, nút Chấp nhận và Từ chối.
 * Kết quả được trả về qua {@link ResponseListener}.</p>
 *
 * <p>Cách dùng từ ChatFrame:</p>
 * <pre>
 *   // Gọi từ onControlMessage callback (phải qua invokeLater):
 *   SwingUtilities.invokeLater(() ->
 *       new IncomingCallDialog(chatFrame, callerName, new ResponseListener() {
 *           public void onAccepted() { callPanel.setState(IN_CALL, callerName); }
 *           public void onRejected() { /* send CALL_REJECT *&#47; }
 *       }).setVisible(true)
 *   );
 * </pre>
 */
public final class IncomingCallDialog extends JDialog {

    public interface ResponseListener {
        void onAccepted();
        void onRejected();
    }

    // ── Màu sắc ───────────────────────────────────────────────────
    private static final Color COLOR_BG     = Color.WHITE;
    private static final Color COLOR_HDR    = new Color(0x1E, 0x2D, 0x48);
    private static final Color COLOR_ACCEPT = new Color(0x27, 0xAE, 0x60);
    private static final Color COLOR_REJECT = new Color(0xE7, 0x4C, 0x3C);
    private static final Color COLOR_TEXT   = new Color(0x2C, 0x3E, 0x50);
    private static final Color COLOR_SUB    = new Color(0x7F, 0x8C, 0x8D);

    // ── Font ─────────────────────────────────────────────────────
    private static final Font FONT_ICON   = new Font("SansSerif", Font.PLAIN, 44);
    private static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  17);
    private static final Font FONT_SUB    = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD,  13);

    // ─────────────────────────────────────────────────────────────

    public IncomingCallDialog(Frame owner, String callerName, ResponseListener listener) {
        super(owner, "Cu\u1ED9c g\u1ECDi \u0111\u1EBFn", true); // "Cuộc gọi đến"
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(340, 310);
        setResizable(false);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        root.add(buildHeader(),            BorderLayout.NORTH);
        root.add(buildBody(callerName, listener), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Header (nền tối, icon) ────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient header giống LoginFrame
                GradientPaint gp = new GradientPaint(
                        0, 0,           new Color(0x1A, 0x27, 0x44),
                        0, getHeight(), new Color(0x2D, 0x4A, 0x8A));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(340, 100));

        // Pulsing phone icon
        JLabel iconLbl = new JLabel("\uD83D\uDCF2", SwingConstants.CENTER); // 📲
        iconLbl.setFont(FONT_ICON);
        iconLbl.setForeground(Color.WHITE);
        header.add(iconLbl);

        return header;
    }

    // ── Body (tên + nút) ─────────────────────────────────────────
    private JPanel buildBody(String callerName, ResponseListener listener) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(COLOR_BG);
        body.setBorder(new EmptyBorder(28, 30, 24, 30));

        // Avatar + tên người gọi
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        avatarRow.setBackground(COLOR_BG);
        avatarRow.add(UiUtils.buildAvatarCircle(callerName, 44, false));

        JLabel callerLbl = new JLabel(callerName, SwingConstants.CENTER);
        callerLbl.setFont(FONT_TITLE);
        callerLbl.setForeground(COLOR_TEXT);
        callerLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("\u0110ang g\u1ECDi cho b\u1EA1n...", SwingConstants.CENTER); // "Đang gọi cho bạn..."
        subLbl.setFont(FONT_SUB);
        subLbl.setForeground(COLOR_SUB);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setBackground(COLOR_BG);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton acceptBtn = buildBtn("\u2713 Ch\u1EA5p nh\u1EADn", COLOR_ACCEPT); // "✓ Chấp nhận"
        acceptBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onAccepted();
        });

        JButton rejectBtn = buildBtn("\u2715 T\u1EEB ch\u1ED1i", COLOR_REJECT); // "✕ Từ chối"
        rejectBtn.addActionListener(e -> {
            dispose();
            if (listener != null) listener.onRejected();
        });

        btnRow.add(acceptBtn);
        btnRow.add(rejectBtn);

        body.add(avatarRow);
        body.add(Box.createVerticalStrut(14));
        body.add(callerLbl);
        body.add(Box.createVerticalStrut(6));
        body.add(subLbl);
        body.add(Box.createVerticalStrut(28));
        body.add(btnRow);

        return body;
    }

    private JButton buildBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 22, 10, 22));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }
}
