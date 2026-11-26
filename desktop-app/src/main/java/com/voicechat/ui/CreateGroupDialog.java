package com.voicechat.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Hộp thoại chỉ thu nhận tên nhóm; bên gọi thực hiện tạo phòng. */
final class CreateGroupDialog extends JDialog {
    // ===== Cấu hình và trạng thái =====
    // Các hằng số và biến được các khối chức năng bên dưới sử dụng.
    private String roomName;

    // ===== Dựng hộp thoại nhập tên nhóm =====
    // Tạo ô tên và các nút xác nhận/hủy; lưu lựa chọn của người dùng.
    private CreateGroupDialog(JFrame owner) {
        super(owner, "Tạo nhóm mới", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(380, 220);
        setLocationRelativeTo(owner);

        UiUtils.RoundedPanel card = new UiUtils.RoundedPanel(
                UiUtils.ARC_LG, UiUtils.C_CARD_BG, UiUtils.C_BORDER, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel title = new JLabel("Tạo nhóm trò chuyện mới");
        title.setFont(UiUtils.F_HEADING);
        title.setForeground(UiUtils.C_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        UiUtils.RoundedTextField nameField =
                new UiUtils.RoundedTextField("Nhập tên nhóm...");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameField);
        card.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        UiUtils.RoundedButton cancel = new UiUtils.RoundedButton(
                "Huỷ", UiUtils.C_SIDEBAR_LIGHT, UiUtils.C_TEXT_PRIMARY, UiUtils.ARC_SM);
        cancel.addActionListener(e -> dispose());

        UiUtils.RoundedButton confirm = new UiUtils.RoundedButton(
                "Tạo nhóm", UiUtils.C_PRIMARY, Color.WHITE, UiUtils.ARC_SM);
        confirm.addActionListener(e -> {
            String value = nameField.getText().trim();
            if (!value.isEmpty()) {
                roomName = value;
                dispose();
            }
        });
        nameField.addActionListener(e -> confirm.doClick());

        buttons.add(cancel);
        buttons.add(confirm);
        card.add(buttons);
        setContentPane(card);
    }

    // ===== Mở hộp thoại và trả kết quả =====
    // Hiển thị hộp thoại modal rồi trả tên nhóm hoặc null khi hủy.
    static String showDialog(JFrame owner) {
        CreateGroupDialog dialog = new CreateGroupDialog(owner);
        dialog.setVisible(true);
        return dialog.roomName;
    }
}
