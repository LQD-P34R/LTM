package com.voicechat;

import com.voicechat.ui.LoginFrame;
import com.voicechat.ui.UiTheme;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            UIManager.put("defaultFont", new javax.swing.plaf.FontUIResource(UiTheme.REGULAR_FONT));
            UIManager.put("Label.font", new javax.swing.plaf.FontUIResource(UiTheme.REGULAR_FONT));
            UIManager.put("Button.font", new javax.swing.plaf.FontUIResource(UiTheme.REGULAR_FONT));
            new LoginFrame().setVisible(true);
        });
    }
}
