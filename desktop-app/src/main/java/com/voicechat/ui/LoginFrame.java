package com.voicechat.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * TV3 – Bước 1: Màn hình đăng nhập / kết nối.
 * <p>Sau khi người dùng nhấn "Kết nối", frame này giả lập kết nối
 * (600 ms delay bằng SwingWorker) rồi mở ChatFrame và tự đóng.
 * Khi AppCoordinator.connect() được TV1 hoàn thiện, chỉ cần thay
 * phần SwingWorker.doInBackground() bằng lệnh gọi thực.</p>
 */
public final class LoginFrame extends JFrame {

    // ── Màu sắc & font ───────────────────────────────────────────
    private static final Color COLOR_HEADER_TOP    = new Color(0x1A, 0x27, 0x44);
    private static final Color COLOR_HEADER_BOTTOM = new Color(0x2D, 0x4A, 0x8A);
    private static final Color COLOR_ACCENT        = new Color(0x3A, 0x7B, 0xD5);
    private static final Color COLOR_ACCENT_HOVER  = new Color(0x2D, 0x64, 0xB8);
    private static final Color COLOR_BG            = new Color(0xF5, 0xF7, 0xFA);
    private static final Color COLOR_FIELD_BG      = Color.WHITE;
    private static final Color COLOR_FIELD_BORDER  = new Color(0xCC, 0xD5, 0xE0);
    private static final Color COLOR_ERROR         = new Color(0xC0, 0x39, 0x2B);
    private static final Color COLOR_LABEL         = new Color(0x2C, 0x3E, 0x50);

    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    private static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  12);
    private static final Font FONT_FIELD   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_BUTTON  = new Font("SansSerif", Font.BOLD,  13);
    private static final Font FONT_ERROR   = new Font("SansSerif", Font.PLAIN, 11);

    // ── Widgets ───────────────────────────────────────────────────
    private final JTextField   tfUsername = new JTextField("User_A");
    private final JTextField   tfIp       = new JTextField("127.0.0.1");
    private final JTextField   tfPort     = new JTextField("5000");
    private final JButton      btnConnect = new JButton("  Kết nối / Đăng nhập");
    private final JProgressBar progress   = new JProgressBar();
    private final JLabel       lblError   = new JLabel(" ");

    // ─────────────────────────────────────────────────────────────

    public LoginFrame() {
        super("VoiceChat P2P — Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 420);
        setMinimumSize(new Dimension(400, 380));
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        setContentPane(root);

        // Nhấn Enter trên bất kỳ field nào cũng kích hoạt kết nối
        ActionListener connectAction = e -> attemptConnect();
        tfUsername.addActionListener(connectAction);
        tfIp.addActionListener(connectAction);
        tfPort.addActionListener(connectAction);
        btnConnect.addActionListener(connectAction);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0,           COLOR_HEADER_TOP,
                        0, getHeight(), COLOR_HEADER_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(450, 115));
        header.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);

        JLabel icon = new JLabel("\uD83D\uDCAC"); // 💬
        icon.setFont(new Font("SansSerif", Font.PLAIN, 32));
        icon.setForeground(Color.WHITE);
        header.add(icon, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 3, 0);
        JLabel title = new JLabel("VoiceChat P2P");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel sub = new JLabel("Kết nối Peer-to-Peer");
        sub.setFont(FONT_SUB);
        sub.setForeground(new Color(0xAA, 0xC4, 0xE8));
        header.add(sub, gbc);

        return header;
    }

    // ── Form ──────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(COLOR_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 36, 20, 36));

        form.add(buildFieldGroup("Tên người dùng",   tfUsername, "Ví dụ: User_A"));
        form.add(Box.createVerticalStrut(14));
        form.add(buildFieldGroup("Địa chỉ IP peer",  tfIp,       "Ví dụ: 192.168.1.10"));
        form.add(Box.createVerticalStrut(14));
        form.add(buildFieldGroup("Cổng TCP",          tfPort,     "Mặc định: 5000"));
        form.add(Box.createVerticalStrut(20));
        form.add(buildConnectButton());
        form.add(Box.createVerticalStrut(10));
        form.add(buildProgressArea());

        return form;
    }

    /** Nhóm label + text field */
    private JPanel buildFieldGroup(String labelText, JTextField field, String hint) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(COLOR_BG);
        group.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COLOR_LABEL);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(Box.createVerticalStrut(5));

        styleField(field, hint);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        group.add(field);

        return group;
    }

    /** Áp dụng style đồng nhất cho JTextField */
    private void styleField(JTextField field, String placeholder) {
        field.setFont(FONT_FIELD);
        field.setForeground(COLOR_LABEL);
        field.setBackground(COLOR_FIELD_BG);
        field.setCaretColor(COLOR_ACCENT);
        field.setToolTipText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        // Focus ring
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_ACCENT, 2, true),
                        new EmptyBorder(5, 9, 5, 9)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1, true),
                        new EmptyBorder(6, 10, 6, 10)
                ));
            }
        });
    }

    /** Nút kết nối */
    private JPanel buildConnectButton() {
        styleButton(btnConnect);
        btnConnect.setAlignmentX(LEFT_ALIGNMENT);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.add(btnConnect, BorderLayout.CENTER);
        return wrapper;
    }

    private void styleButton(JButton btn) {
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_ACCENT);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(COLOR_ACCENT_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(COLOR_ACCENT);
            }
            @Override public void mousePressed(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(COLOR_ACCENT_HOVER.darker());
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(COLOR_ACCENT_HOVER);
            }
        });
    }

    /** Progress bar + error label */
    private JPanel buildProgressArea() {
        JPanel area = new JPanel();
        area.setLayout(new BoxLayout(area, BoxLayout.Y_AXIS));
        area.setBackground(COLOR_BG);
        area.setAlignmentX(LEFT_ALIGNMENT);

        progress.setIndeterminate(true);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progress.setAlignmentX(LEFT_ALIGNMENT);
        progress.setVisible(false);
        progress.setBorderPainted(false);
        progress.setForeground(COLOR_ACCENT);
        area.add(progress);
        area.add(Box.createVerticalStrut(8));

        lblError.setFont(FONT_ERROR);
        lblError.setForeground(COLOR_ERROR);
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        area.add(lblError);

        return area;
    }

    // ── Logic kết nối ─────────────────────────────────────────────

    private void attemptConnect() {
        showError("");

        String username = tfUsername.getText().trim();
        String ip       = tfIp.getText().trim();
        String portStr  = tfPort.getText().trim();

        // Validate username
        if (username.isEmpty()) {
            showError("⚠  Tên người dùng không được để trống.");
            tfUsername.requestFocusInWindow();
            return;
        }
        if (username.length() < 2) {
            showError("⚠  Tên người dùng phải có ít nhất 2 ký tự.");
            tfUsername.requestFocusInWindow();
            return;
        }

        // Validate IP
        if (!ip.matches("^[\\w.:\\-]+$")) {
            showError("⚠  Địa chỉ IP không hợp lệ.");
            tfIp.requestFocusInWindow();
            return;
        }

        // Validate port
        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("⚠  Cổng phải là số nguyên từ 1 đến 65535.");
            tfPort.requestFocusInWindow();
            return;
        }

        // Giả lập kết nối bằng SwingWorker
        setFormEnabled(false);
        progress.setVisible(true);

        final String finalUsername = username;
        final String finalIp       = ip;
        final int    finalPort     = port;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                /*
                 * TODO (TV1 integration): thay dòng Thread.sleep bằng:
                 *   AppCoordinator.getInstance().connect(finalIp, finalPort);
                 */
                Thread.sleep(600);
                return null;
            }

            @Override
            protected void done() {
                progress.setVisible(false);
                try {
                    get();
                    new ChatFrame(finalUsername).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    setFormEnabled(true);
                    showError("⚠  Không thể kết nối: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void setFormEnabled(boolean enabled) {
        tfUsername.setEnabled(enabled);
        tfIp.setEnabled(enabled);
        tfPort.setEnabled(enabled);
        btnConnect.setEnabled(enabled);
        btnConnect.setBackground(enabled ? COLOR_ACCENT : COLOR_ACCENT.darker());
    }

    private void showError(String msg) {
        lblError.setText(msg.isEmpty() ? " " : msg);
    }
}
