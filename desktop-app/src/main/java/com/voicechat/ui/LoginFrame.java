package com.voicechat.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * VoiceLink LoginFrame: Màn hình đăng nhập phong cách VoiceLink hiện đại.
 * <p>Căn giữa một thẻ RoundedPanel bo góc lớn trên nền tổng thể sáng dịu mắt (C_APP_BG),
 * sử dụng RoundedTextField và RoundedButton full-width nổi bật.</p>
 */
public final class LoginFrame extends JFrame {

    static {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 999);
        UiUtils.installGlobalTypography();
    }

    // ── Widgets ───────────────────────────────────────────────────
    private final UiUtils.RoundedTextField tfUsername = new UiUtils.RoundedTextField("Nhập tên người dùng...");
    private final UiUtils.RoundedTextField tfIp       = new UiUtils.RoundedTextField("Ví dụ: 127.0.0.1");
    private final UiUtils.RoundedTextField tfPort     = new UiUtils.RoundedTextField("Mặc định: 5000");
    private final UiUtils.RoundedButton    btnConnect = new UiUtils.RoundedButton(
            "Kết nối / Đăng nhập", UiUtils.C_PRIMARY, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_MD);
    private final UiUtils.RoundedButton btnDemo = new UiUtils.RoundedButton(
            "Xem trước giao diện (Demo UI)", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_SM);
    private final JProgressBar             progress   = new JProgressBar();
    private final JLabel                   lblError   = new JLabel(" ");

    public LoginFrame() {
        super("VoiceLink P2P — Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setMinimumSize(new Dimension(460, 560));
        setLocationRelativeTo(null);

        // Nền tổng thể sáng dịu mắt (Slate-50)
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UiUtils.C_APP_BG);

        root.add(buildLoginCard());
        setContentPane(root);

        // Nhấn Enter trên bất kỳ field nào cũng kích hoạt kết nối
        ActionListener connectAction = e -> attemptConnect();
        tfUsername.addActionListener(connectAction);
        tfIp.addActionListener(connectAction);
        tfPort.addActionListener(connectAction);
        btnConnect.addActionListener(connectAction);
        btnDemo.addActionListener(e -> new ChatFrame("Demo User").setVisible(true));
    }

    // ── Thẻ Card đăng nhập trung tâm ──────────────────────────────
    private JPanel buildLoginCard() {
        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(420, 530));
        card.setMaximumSize(new Dimension(420, 530));

        // 1. Logo Badge hình tròn
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_DANGER);
                g2.fillOval((getWidth() - 56) / 2, 0, 56, 56);
                g2.dispose();
            }
        };
        logoPanel.setLayout(new GridBagLayout());
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(420, 58));
        logoPanel.setMaximumSize(new Dimension(420, 58));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoIcon = new JLabel(new AppIcons(AppIcons.Kind.LOGO, 30, Color.WHITE));
        logoPanel.add(logoIcon);

        card.add(logoPanel);
        card.add(Box.createVerticalStrut(12));

        // 2. Tiêu đề chính & Tiêu đề phụ
        JLabel titleLbl = new JLabel("VoiceLink P2P", SwingConstants.CENTER);
        titleLbl.setFont(UiUtils.F_TITLE_XL);
        titleLbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));

        JLabel subLbl = new JLabel("Gọi thoại và nhắn tin trực tiếp giữa các máy", SwingConstants.CENTER);
        subLbl.setFont(UiUtils.F_SMALL);
        subLbl.setForeground(UiUtils.C_TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subLbl);
        card.add(Box.createVerticalStrut(24));

        // 3. Các ô nhập liệu
        tfUsername.setText("User_A");
        tfIp.setText("127.0.0.1");
        tfPort.setText("5000");

        card.add(buildFieldGroup("Tên người dùng", tfUsername));
        card.add(Box.createVerticalStrut(12));
        card.add(buildFieldGroup("Địa chỉ IP peer", tfIp));
        card.add(Box.createVerticalStrut(12));
        card.add(buildFieldGroup("Cổng TCP", tfPort));
        card.add(Box.createVerticalStrut(20));

        // 4. Nút đăng nhập Full-width
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConnect.setPreferredSize(new Dimension(340, 44));
        card.add(btnConnect);
        card.add(Box.createVerticalStrut(8));
        btnDemo.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDemo.setPreferredSize(new Dimension(340, 34));
        btnDemo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        card.add(btnDemo);
        card.add(Box.createVerticalStrut(6));

        // 5. Thanh tiến trình và thông báo lỗi
        progress.setIndeterminate(true);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progress.setPreferredSize(new Dimension(340, 4));
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setVisible(false);
        progress.setBorderPainted(false);
        progress.setForeground(UiUtils.C_PRIMARY_TEXT);
        card.add(progress);
        card.add(Box.createVerticalStrut(6));

        lblError.setFont(UiUtils.F_SMALL);
        lblError.setForeground(UiUtils.C_DANGER);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblError);

        return card;
    }

    private JPanel buildFieldGroup(String labelText, JTextField field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        AppIcons.Kind fieldIcon = labelText.contains("Tên") ? AppIcons.Kind.USER
                : labelText.contains("IP") ? AppIcons.Kind.HOST : AppIcons.Kind.PORT;
        JLabel lbl = new JLabel(labelText, new AppIcons(fieldIcon, 14, UiUtils.C_TEXT_MUTED), SwingConstants.LEFT);
        lbl.setFont(UiUtils.F_SMALL_BOLD);
        lbl.setForeground(UiUtils.C_TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(lbl);
        group.add(Box.createVerticalStrut(5));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(340, 40));
        group.add(field);

        return group;
    }

    // ── Logic kết nối ─────────────────────────────────────────────
    private void attemptConnect() {
        showError("");

        String username = tfUsername.getText().trim();
        String ip       = tfIp.getText().trim();
        String portStr  = tfPort.getText().trim();

        // Validate username
        if (username.isEmpty()) {
            showError("Tên người dùng không được để trống.");
            tfUsername.requestFocusInWindow();
            return;
        }
        if (username.length() < 2) {
            showError("Tên người dùng phải có ít nhất 2 ký tự.");
            tfUsername.requestFocusInWindow();
            return;
        }

        // Validate IP
        if (!ip.matches("^[\\w.:\\-]+$")) {
            showError("Địa chỉ IP không hợp lệ.");
            tfIp.requestFocusInWindow();
            return;
        }

        // Validate port
        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Cổng phải là số nguyên từ 1 đến 65535.");
            tfPort.requestFocusInWindow();
            return;
        }

        // LoginFrame currently has no backend callback contract; do not report a
        // connection or open the chat until AppCoordinator supplies its result.
        showError("Chưa có callback kết nối từ backend. Dữ liệu đã được kiểm tra.");
    }

    private void setFormEnabled(boolean enabled) {
        tfUsername.setEnabled(enabled);
        tfIp.setEnabled(enabled);
        tfPort.setEnabled(enabled);
        btnConnect.setEnabled(enabled);
    }

    private void showError(String msg) {
        lblError.setText(msg.isEmpty() ? " " : msg);
    }
}
