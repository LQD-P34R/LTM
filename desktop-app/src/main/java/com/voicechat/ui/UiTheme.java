package com.voicechat.ui;
import java.awt.*;
import java.io.InputStream;
// Class chỉ chứa static constants
public final class UiTheme {
  private UiTheme() {}

    public static final Font REGULAR_FONT = loadFont("/fonts/BeVietnamPro-Regular.ttf", Font.PLAIN);
    public static final Font SEMIBOLD_FONT = loadFont("/fonts/BeVietnamPro-SemiBold.ttf", Font.PLAIN);
  private static Font loadFont(String resource, int style) {
    try (InputStream in = UiTheme.class.getResourceAsStream(resource)) {
      if (in != null) {
        Font font = Font.createFont(Font.TRUETYPE_FONT, in);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
        return font.deriveFont(style, 13f);
      }
    } catch (Exception ignored) { }
    return new Font("Segoe UI", style, 13);
  }

  // Shared brand colors
  public static final Color MIDNIGHT_NAVY = new Color(0x4A,0x1D,0x22);
  public static final Color DEEP_NAVY = new Color(0xFB,0xF4,0xE3);
  public static final Color COBALT_BLUE = new Color(0x8D,0xB4,0xD2);
  public static final Color SOFT_LAVENDER = new Color(0xDC,0xE8,0xF1);
  public static final Color MINT_ACCENT = new Color(0xD6,0xE4,0xED);
  public static final Color WARM_CHAMPAGNE = new Color(0xE8,0xD9,0xB9);
  // Warm light surfaces and readable text
  public static final Color BG = new Color(0xFB,0xF4,0xE3);
  public static final Color SURFACE = new Color(0xFF,0xFB,0xF2);
  public static final Color BG_SECONDARY = new Color(0xFF,0xFB,0xF2);
  public static final Color TEXT_PRIMARY = new Color(0x3B,0x16,0x1B);
  public static final Color TEXT_SECONDARY = new Color(0x65,0x50,0x55);
  public static final Color BORDER = new Color(0xC8,0xBE,0xAA);
  // Status
  public static final Color ONLINE_GREEN = new Color(0x8D,0xB4,0xD2);
  public static final Color OFFLINE_GRAY = new Color(0x78,0x64,0x68);
  public static final Color DANGER_RED = new Color(0xA8,0x2A,0x35);
  public static final Color WARNING_AMBER = new Color(0xE8,0xD9,0xB9);
  // Typography
  public static final Font F_XL        = SEMIBOLD_FONT.deriveFont(22f);
    public static final Font F_LG        = SEMIBOLD_FONT.deriveFont(18f);
    public static final Font F_HEAD      = SEMIBOLD_FONT.deriveFont(15f);
    public static final Font F_BODY      = REGULAR_FONT.deriveFont(13f);
    public static final Font F_BODY_BOLD = SEMIBOLD_FONT.deriveFont(13f);
    public static final Font F_SM        = REGULAR_FONT.deriveFont(11f);
    public static final Font F_SM_BOLD   = SEMIBOLD_FONT.deriveFont(11f);
  // Arc radii
  public static final int ARC_SM = 10;
  public static final int ARC_MD = 14;
  public static final int ARC_LG = 18;
  public static final int ARC_XL = 24;

  public static void enableAA(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
  }

  // Avatar color palette
  public static final Color[] AVATAR_COLORS = {
    new Color(0xD6,0xE4,0xED), new Color(0xE8,0xD9,0xB9),
    new Color(0xE8,0xC9,0xCE), new Color(0xD7,0xB9,0xA2),
    new Color(0xDC,0xE8,0xF1), new Color(0xF0,0xDE,0xB8),
    new Color(0xE7,0xD2,0xC8), new Color(0xE6,0xC9,0xD0),
  };

  public static Color avatarColor(String name) {
    if (name==null||name.isEmpty()) return AVATAR_COLORS[0];
    return AVATAR_COLORS[Math.abs(name.hashCode())%AVATAR_COLORS.length];
  }

  public static String initials(String name) {
    if (name==null||name.trim().isEmpty()) return "?";
    String clean=name.trim();
    if (clean.equalsIgnoreCase("Bạn")) return "BN";
    String[] p = clean.split("[^\\p{L}\\p{N}]+");
    if (p.length==0) return "?";
    if (p.length==1) { String w=p[0]; return w.length()>=2?w.substring(0,2).toUpperCase():w.toUpperCase(); }
    return (""+p[0].charAt(0)+p[1].charAt(0)).toUpperCase();
  }
}
