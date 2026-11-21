package com.voicechat.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/** Antialiased vector icons. Every icon is drawn in a 24 by 24 view box. */
public final class AppIcons implements Icon {
    public enum Kind { LOGO, PHONE, HEADSET, HANGUP, MIC, MIC_OFF, SPEAKER, SPEAKER_OFF,
        CHAT, GROUP, HISTORY, SEARCH, SEND, ARROW_IN, ARROW_OUT, ARROW_MISSED,
        OUTGOING, INCOMING, MISSED, CHECKS, MOON, SUN, USER, HOST, PORT, BACK, PLUS, SETTINGS, ATTACH, SMILE }

    private final Kind kind;
    private final int size;
    private final Color color;

    public AppIcons(Kind kind, int size, Color color) {
        this.kind = kind;
        this.size = size;
        this.color = color;
    }

    public Kind getKind() { return kind; }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.scale(size / 24.0, size / 24.0);
        g2.setColor(color == null ? Color.BLACK : color);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case LOGO -> {
                Path2D wave = new Path2D.Double();
                wave.moveTo(1,12); wave.lineTo(4,12); wave.lineTo(7,7); wave.lineTo(10,17);
                wave.lineTo(13,4); wave.lineTo(16,20); wave.lineTo(19,9); wave.lineTo(22,12); wave.lineTo(23,12);
                g2.draw(wave);
            }
            case PHONE -> {
                Path2D handset = new Path2D.Double();
                handset.moveTo(5,3); handset.quadTo(2,4,3,8); handset.quadTo(7,17,16,21);
                handset.quadTo(20,22,21,18); handset.lineTo(17,14); handset.quadTo(16,13,14,16);
                handset.quadTo(10,14,8,10); handset.quadTo(11,8,10,6); handset.closePath(); g2.fill(handset);
            }
            case HEADSET -> {
                g2.drawArc(4, 2, 16, 20, 205, 130);
                g2.drawRoundRect(2, 11, 5, 8, 2, 2);
                g2.drawRoundRect(17, 11, 5, 8, 2, 2);
            }
            case HANGUP -> {
                Path2D handset = new Path2D.Double();
                handset.moveTo(3,12); handset.quadTo(12,5,21,12); handset.lineTo(18,17);
                handset.quadTo(16,18,14,15); handset.quadTo(12,14,10,15);
                handset.quadTo(8,18,6,17); handset.closePath(); g2.fill(handset);
            }
            case MIC, MIC_OFF -> {
                g2.drawRoundRect(9, 2, 6, 13, 3, 3); g2.drawArc(5, 5, 14, 15, 180, 180);
                g2.drawLine(12,20,12,23); g2.drawLine(8,23,16,23);
                if (kind == Kind.MIC_OFF) g2.drawLine(3,3,21,21);
            }
            case SPEAKER, SPEAKER_OFF -> {
                g2.drawPolygon(new int[]{3,8,12,12,8,3}, new int[]{9,9,5,19,15,15}, 6);
                g2.drawArc(8,5,14,14,-55,110);
                if (kind == Kind.SPEAKER_OFF) g2.drawLine(16,8,22,16); else g2.drawArc(8,2,18,20,-55,110);
            }
            case CHAT -> {
                g2.drawRoundRect(3,3,18,15,5,5); g2.drawLine(8,18,5,21); g2.drawLine(8,18,13,18);
            }
            case GROUP -> {
                g2.drawOval(5,4,6,6); g2.drawOval(14,4,6,6);
                g2.drawArc(2,12,12,10,0,180); g2.drawArc(11,12,12,10,0,180);
            }
            case HISTORY -> {
                g2.drawOval(3,3,18,18); g2.drawLine(12,6,12,12); g2.drawLine(12,12,16,14);
            }
            case SEARCH -> { g2.drawOval(3,3,13,13); g2.drawLine(15,15,21,21); }
            case SEND -> {
                Path2D plane = new Path2D.Double();
                plane.moveTo(2,3); plane.lineTo(22,12); plane.lineTo(2,21); plane.lineTo(6,12); plane.closePath();
                g2.draw(plane); g2.drawLine(6,12,22,12);
            }
            case ARROW_IN, INCOMING -> {
                g2.drawLine(19,5,5,19); g2.drawLine(5,11,5,19); g2.drawLine(5,19,13,19);
            }
            case ARROW_OUT, OUTGOING -> {
                g2.drawLine(5,19,19,5); g2.drawLine(11,5,19,5); g2.drawLine(19,5,19,13);
            }
            case ARROW_MISSED, MISSED -> {
                g2.drawLine(5,5,19,19); g2.drawLine(19,5,5,19);
            }
            case CHECKS -> {
                g2.drawLine(1,12,5,16); g2.drawLine(5,16,11,8); g2.drawLine(9,12,13,16); g2.drawLine(13,16,21,7);
            }
            case MOON -> {
                g2.drawOval(4,3,16,18);
                g2.setColor(c == null || c.getBackground() == null ? Color.WHITE : c.getBackground());
                g2.fillOval(11,1,13,15);
            }
            case SUN -> {
                g2.drawOval(7,7,10,10); g2.drawLine(12,1,12,4); g2.drawLine(12,20,12,23);
                g2.drawLine(1,12,4,12); g2.drawLine(20,12,23,12); g2.drawLine(4,4,6,6);
                g2.drawLine(18,18,20,20); g2.drawLine(18,6,20,4); g2.drawLine(4,20,6,18);
            }
            case USER -> { g2.drawOval(8,3,8,8); g2.drawArc(4,13,16,12,0,180); }
            case HOST -> { g2.drawRoundRect(3,5,18,14,2,2); g2.drawLine(8,22,16,22); g2.drawLine(12,19,12,22); }
            case PORT -> { g2.drawRoundRect(6,3,12,18,3,3); g2.drawLine(10,7,14,7); g2.drawLine(10,11,14,11); g2.drawLine(10,15,14,15); }
            case BACK -> { g2.drawLine(19,12,5,12); g2.drawLine(5,12,11,6); g2.drawLine(5,12,11,18); }
            case PLUS -> { g2.drawLine(12,4,12,20); g2.drawLine(4,12,20,12); }
            case SETTINGS -> {
                g2.drawLine(4,6,20,6); g2.drawLine(4,12,20,12); g2.drawLine(4,18,20,18);
                g2.drawOval(7,3,6,6); g2.drawOval(13,9,6,6); g2.drawOval(6,15,6,6);
            }
            case ATTACH -> {
                Path2D clip=new Path2D.Double(); clip.moveTo(8,12); clip.lineTo(15,5);
                clip.quadTo(20,1,22,6); clip.quadTo(24,9,20,13); clip.lineTo(11,21);
                clip.quadTo(6,25,3,20); clip.quadTo(1,17,5,13); clip.lineTo(14,4); g2.draw(clip);
            }
            case SMILE -> {
                g2.drawOval(2,2,20,20); g2.fillOval(7,8,2,2); g2.fillOval(15,8,2,2); g2.drawArc(7,9,10,9,205,130);
            }
        }
        g2.dispose();
    }
}
