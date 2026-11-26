package com.voicechat;

import com.voicechat.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    // ===== Khởi động ứng dụng Swing =====
    // Cấu hình giao diện hệ thống và mở LoginFrame trên EDT (luồng giao diện Swing).
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* use default look and feel */ }
            new LoginFrame().setVisible(true);
        });
    }
}
