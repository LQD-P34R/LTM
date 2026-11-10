package com.voicechat.ui;

import javax.swing.*;
import java.awt.*;

/** TV3 TODO: noi nut voi VoiceGateway va trang thai cuoc goi. */
public final class CallPanel extends JPanel {
    public CallPanel() {
        super(new FlowLayout(FlowLayout.LEFT));
        JButton call = new JButton("Goi (chua ho tro)");
        JButton mute = new JButton("Mute (chua ho tro)");
        JButton end = new JButton("Ket thuc (chua ho tro)");
        call.setEnabled(false); mute.setEnabled(false); end.setEnabled(false);
        add(call); add(mute); add(end);
    }
}
