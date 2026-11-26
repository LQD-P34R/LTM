package com.voicechat.ui;

import com.voicechat.group.GroupClient;
import com.voicechat.group.GroupProtocol.View;

import com.voicechat.integration.DesktopSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * VoiceLink LoginFrame: Màn hình đăng nhập phong cách VoiceLink hiện đại.
 * <p>
 * Căn giữa một thẻ RoundedPanel bo góc lớn trên nền tổng thể sáng dịu mắt
 * (C_APP_BG),
 * sử dụng RoundedTextField và RoundedButton full-width nổi bật.
 * </p>
 */
public final class LoginFrame extends JFrame {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private DesktopSession pendingSession;
    private boolean groupConnecting;
    private final JCheckBox waitForPeer = new JCheckBox("Chờ peer kết nối đến");
    private final JCheckBox groupMode = new JCheckBox("Chat nhóm qua máy chủ");
    private final UiUtils.RoundedTextField tfLocalTcp = new UiUtils.RoundedTextField("Cổng TCP của máy này");
    private final UiUtils.RoundedTextField tfLocalUdp = new UiUtils.RoundedTextField("Cổng UDP của máy này");
    private final UiUtils.RoundedTextField tfUsername = new UiUtils.RoundedTextField("Nhập tên người dùng...");
    private final UiUtils.RoundedTextField tfIp = new UiUtils.RoundedTextField("Ví dụ: 127.0.0.1");
    private final UiUtils.RoundedTextField tfPort = new UiUtils.RoundedTextField("Mặc định: 5000");
    private final UiUtils.RoundedButton btnConnect = new UiUtils.RoundedButton(
            "Kết nối / Đăng nhập", UiUtils.C_PRIMARY, Color.WHITE, UiUtils.ARC_MD);
    private final JProgressBar progress = new JProgressBar();
    private final JLabel lblError = new JLabel(" ");

    // ===== Khởi tạo và dựng biểu mẫu đăng nhập =====
    // Tạo thẻ đăng nhập, các ô cấu hình kết nối và gắn sự kiện cho biểu mẫu.
    public LoginFrame() {
        super("VoiceLink P2P — Đăng nhập");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (pendingSession != null) {
                    pendingSession.close();
                    pendingSession = null;
                }
            }
        });
        setSize(560, 820);
        setMinimumSize(new Dimension(540, 780));
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
        tfLocalTcp.addActionListener(connectAction);
        tfLocalUdp.addActionListener(connectAction);
        waitForPeer.addActionListener(e -> updatePeerFields());
        groupMode.addActionListener(e -> {
            tfPort.setText(groupMode.isSelected() ? "5000" : "5100");
            updatePeerFields();
        });
        btnConnect.addActionListener(connectAction);
    }

    private JPanel buildLoginCard() {
        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));
        card.setPreferredSize(new Dimension(460, 720));
        card.setMaximumSize(new Dimension(460, 720));

        // 1. Logo Badge hình tròn
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtils.enableAntiAliasing(g2);
                g2.setColor(UiUtils.C_PRIMARY);
                g2.fillOval((getWidth() - 56) / 2, 0, 56, 56);
                g2.dispose();
            }
        };
        logoPanel.setLayout(new GridBagLayout());
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(420, 58));
        logoPanel.setMaximumSize(new Dimension(420, 58));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoIcon = new JLabel("\uD83D\uDCAC"); // 💬
        logoIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        logoIcon.setForeground(Color.WHITE);
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
        tfPort.setText("5100");
        tfLocalTcp.setText("5101");
        tfLocalUdp.setText("6101");

        card.add(buildFieldGroup("Tên người dùng", tfUsername));
        card.add(Box.createVerticalStrut(8));
        groupMode.setOpaque(false);
        groupMode.setFont(UiUtils.F_SMALL);
        groupMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(groupMode);
        card.add(Box.createVerticalStrut(12));
        JPanel localPorts = new JPanel(new GridLayout(1, 2, 12, 0));
        localPorts.setOpaque(false);
        localPorts.setAlignmentX(Component.LEFT_ALIGNMENT);
        localPorts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        localPorts.add(buildFieldGroup("TCP máy này", tfLocalTcp));
        localPorts.add(buildFieldGroup("UDP máy này", tfLocalUdp));
        card.add(localPorts);
        card.add(Box.createVerticalStrut(12));

        waitForPeer.setOpaque(false);
        waitForPeer.setFont(UiUtils.F_SMALL);
        waitForPeer.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(waitForPeer);
        card.add(Box.createVerticalStrut(12));

        card.add(buildFieldGroup("Địa chỉ IP máy chủ / peer", tfIp));
        card.add(Box.createVerticalStrut(12));
        card.add(buildFieldGroup("Cổng TCP máy chủ / peer", tfPort));
        card.add(Box.createVerticalStrut(20));

        // 4. Nút đăng nhập Full-width
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConnect.setPreferredSize(new Dimension(340, 44));
        card.add(btnConnect);
        card.add(Box.createVerticalStrut(12));

        // 5. Thanh tiến trình và thông báo lỗi
        progress.setIndeterminate(true);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progress.setPreferredSize(new Dimension(340, 4));
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setVisible(false);
        progress.setBorderPainted(false);
        progress.setForeground(UiUtils.C_PRIMARY);
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

        JLabel lbl = new JLabel(labelText);
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

    // ===== Kiểm tra cấu hình và mở kết nối =====
    // Kiểm tra tên/địa chỉ/cổng; mở phiên P2P hoặc đăng nhập máy chủ nhóm trên luồng nền.
    private void attemptConnect() {
        if (pendingSession != null || groupConnecting) {
            return;
        }
        showError("");

        String username = tfUsername.getText().trim();
        String host = tfIp.getText().trim();
        boolean waiting = waitForPeer.isSelected();
        if (username.length() < 2) {
            showError("Tên người dùng cần ít nhất 2 ký tự.");
            tfUsername.requestFocusInWindow();
            return;
        }
        if (groupMode.isSelected()) {
            if (host.isBlank()) {
                showError("Hãy nhập địa chỉ máy chủ nhóm.");
                tfIp.requestFocusInWindow();
                return;
            }

            final int groupPort;
            try {
                groupPort = readPort(tfPort, "TCP máy chủ nhóm");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
                return;
            }

            connectGroup(username, host, groupPort);
            return;
        }

        final int localTcp;
        final int localUdp;
        final int remoteTcp;
        try {
            localTcp = readPort(tfLocalTcp, "TCP máy này");
            localUdp = readPort(tfLocalUdp, "UDP máy này");
            remoteTcp = waiting ? 0 : readPort(tfPort, "TCP của peer");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }
        if (!waiting && !host.matches("^[\\w.:\\-]+$")) {
            showError("Địa chỉ peer không hợp lệ.");
            tfIp.requestFocusInWindow();
            return;
        }

        setFormEnabled(false);
        progress.setVisible(true);
        try {
            DesktopSession session = new DesktopSession(username);
            pendingSession = session;
            session.open(localTcp, localUdp, waiting, host, remoteTcp,
                    () -> {
                        pendingSession = null;
                        progress.setVisible(false);
                        dispose();
                    }, error -> {
                        pendingSession = null;
                        progress.setVisible(false);
                        setFormEnabled(true);
                        showError("Không thể kết nối: " + error.getMessage());
                    });
        } catch (RuntimeException e) {
            if (pendingSession != null)
                pendingSession.close();
            pendingSession = null;
            progress.setVisible(false);
            setFormEnabled(true);
            showError("Không thể khởi tạo: " + e.getMessage());
        }
    }

    private int readPort(JTextField field, String label) {
        try {
            int port = Integer.parseInt(field.getText().trim());
            if (port < 1 || port > 65535)
                throw new NumberFormatException();
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " phải từ 1 đến 65535.");
        }
    }

    private void connectGroup(String username, String host, int port) {
        groupConnecting = true;
        setFormEnabled(false);
        progress.setVisible(true);

        new SwingWorker<View, Void>() {
            private GroupClient groupClient;

            @Override
            protected View doInBackground() throws Exception {
                groupClient = GroupClient.connect(username, host, port);
                return groupClient.request("login", null, username, 0);
            }

            @Override
            protected void done() {
                groupConnecting = false;
                progress.setVisible(false);

                try {
                    View initial = get();

                    if (!LoginFrame.this.isDisplayable()) {
                        groupClient.close();
                        return;
                    }

                    ChatFrame frame = new ChatFrame(
                            initial.username(), groupClient, initial);
                    frame.setVisible(true);

                    // ChatFrame tiếp nhận quyền đóng kết nối.
                    groupClient = null;
                    LoginFrame.this.dispose();
                } catch (Exception ex) {
                    if (groupClient != null) {
                        groupClient.close();
                    }

                    if (!LoginFrame.this.isDisplayable()) {
                        return;
                    }

                    setFormEnabled(true);

                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();

                    showError(
                            "Không thể đăng nhập nhóm: "
                                    + cause.getMessage());
                }
            }
        }.execute();
    }

    // ===== Cập nhật trạng thái biểu mẫu =====
    // Bật/tắt các ô theo chế độ kết nối và hiển thị thông báo lỗi.
    private void updatePeerFields() {
        boolean enabled = btnConnect.isEnabled();
        boolean group = groupMode.isSelected();

        tfLocalTcp.setEnabled(enabled && !group);
        tfLocalUdp.setEnabled(enabled && !group);
        waitForPeer.setEnabled(enabled && !group);

        boolean needsAddress = group || !waitForPeer.isSelected();
        tfIp.setEnabled(enabled && needsAddress);
        tfPort.setEnabled(enabled && needsAddress);
    }

    private void setFormEnabled(boolean enabled) {
        tfUsername.setEnabled(enabled);
        groupMode.setEnabled(enabled);
        btnConnect.setEnabled(enabled);
        updatePeerFields();
    }

    private void showError(String msg) {
        lblError.setText(msg.isEmpty() ? " " : msg);
        lblError.setToolTipText(msg);
    }
}
