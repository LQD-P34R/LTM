package com.voicechat.ui;

import java.awt.*;
import javax.swing.Icon;

/** VoiceLink controls are drawn in logical pixels, including on high-DPI displays. */
enum UiIcon implements Icon {
    CHAT, GROUP, HISTORY, PHONE, SMILE, SEND, MIC, SPEAKER, CLOSE;
    public int getIconWidth() { return 18; }
    public int getIconHeight() { return 18; }
    public void paintIcon(Component c, Graphics g, int x, int y) {
        draw(g,x,y,18,c.getForeground());
    }
    void draw(Graphics g,int x,int y,int size,Color color) {
        Graphics2D p = (Graphics2D) g.create();
        try {
            p.translate(x,y);
            p.scale(size / 20.0,size / 20.0);
            p.setColor(color);
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            p.setStroke(new BasicStroke(1.7f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            switch (this) {
                case CHAT -> {
                    p.drawRoundRect(2,3,16,11,5,5);
                    p.drawPolyline(new int[]{5,5,9},new int[]{14,18,14},3);
                }
                case GROUP -> {
                    p.fillOval(4,3,6,6); p.fillOval(12,5,5,5);
                    p.fillRoundRect(1,10,12,7,6,6); p.fillRoundRect(13,11,6,6,4,4);
                }
                case HISTORY -> { p.drawOval(2,2,16,16); p.drawLine(10,5,10,10); p.drawLine(10,10,14,12); }
                case PHONE -> p.fillPolygon(new int[]{3,6,9,7,12,14,18,17,14,9,5,2},new int[]{3,2,6,8,13,11,14,17,18,16,12,6},12);
                case SMILE -> { p.drawOval(2,2,16,16); p.fillOval(6,7,2,2); p.fillOval(12,7,2,2); p.drawArc(5,7,10,8,205,130); }
                case SEND -> p.fillPolygon(new int[]{2,18,2,5,13,5},new int[]{2,10,18,12,10,8},6);
                case MIC -> { p.fillRoundRect(7,2,6,10,5,5); p.drawArc(4,5,12,10,180,180); p.drawLine(10,15,10,18); }
                case SPEAKER -> { p.fillPolygon(new int[]{2,6,11,11,6,2},new int[]{8,8,4,16,12,12},6); p.drawArc(9,5,8,10,-60,120); }
                case CLOSE -> { p.drawLine(5,5,15,15); p.drawLine(15,5,5,15); }
            }
        } finally { p.dispose(); }
    }
}
