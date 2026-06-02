package com.colorchecker.ui.panels;

import com.colorchecker.model.ColorModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
/*
Renders a simplified CIE 1931 xy chromaticity diagram showing:
The sRGB colour gamut triangle (R, G, B primaries connected by lines)
A spectral locus approximation drawn as a colourful boundary arc
The D65 white point
Two large dots: one for the Foreground colour, one for Background
A line connecting FG and BG showing their perceptual distance
The full spectral locus data is approximated by 81 sample wavelength
chromaticity (x, y) pairs from 380 nm to 780 nm (5 nm steps), which are
sufficient for a recognisable horseshoe shape at UI scale.</p>
*/
public class ChromaticityPanel extends JPanel {
    // sRGB primaries in CIE xy  (ITU-R BT.709)
    private static final double[] R_XY = { 0.6400, 0.3300 };
    private static final double[] G_XY = { 0.3000, 0.6000 };
    private static final double[] B_XY = { 0.1500, 0.0600 };
    private static final double[] W_XY = { 0.3127, 0.3290 };
    /*
      CIE 1931 spectral locus – (x, y) at 5 nm steps from 380 nm to 700 nm.
      Values from the official CIE colorimetric tables.
     */
    private static final double[][] LOCUS = {
            {0.1741,0.0050},{0.1740,0.0050},{0.1738,0.0049},{0.1736,0.0049},{0.1733,0.0048},
            {0.1730,0.0048},{0.1726,0.0048},{0.1721,0.0048},{0.1714,0.0051},{0.1703,0.0058},
            {0.1689,0.0072},{0.1669,0.0101},{0.1644,0.0138},{0.1611,0.0188},{0.1566,0.0255},
            {0.1510,0.0339},{0.1440,0.0440},{0.1355,0.0578},{0.1241,0.0759},{0.1096,0.0988},
            {0.0913,0.1327},{0.0687,0.1747},{0.0454,0.2247},{0.0235,0.2807},{0.0082,0.3383},
            {0.0039,0.3868},{0.0139,0.4370},{0.0389,0.4841},{0.0743,0.5114},{0.1142,0.5419},
            {0.1547,0.5678},{0.1929,0.5882},{0.2296,0.6042},{0.2658,0.6133},{0.3016,0.6162},
            {0.3373,0.6082},{0.3731,0.5896},{0.4087,0.5624},{0.4441,0.5309},{0.4788,0.4970},
            {0.5125,0.4617},{0.5448,0.4266},{0.5752,0.3916},{0.6029,0.3583},{0.6270,0.3275},
            {0.6482,0.2993},{0.6658,0.2748},{0.6801,0.2541},{0.6915,0.2374},{0.7006,0.2236},
            {0.7079,0.2124},{0.7140,0.2031},{0.7190,0.1954},{0.7230,0.1889},{0.7260,0.1834},
            {0.7283,0.1789},{0.7300,0.1750},{0.7311,0.1717},{0.7320,0.1689},{0.7327,0.1664},
            {0.7334,0.1642},{0.7340,0.1621},{0.7344,0.1600},{0.7346,0.1579},{0.7347,0.1561}
    };
    private ColorModel fg=new ColorModel(Color.BLACK);
    private ColorModel bg=new ColorModel(Color.WHITE);
    // Diagram bounds in xy space that we render
    private static final double X_MIN = 0.0, X_MAX = 0.8;
    private static final double Y_MIN = 0.0, Y_MAX = 0.9;
     public ChromaticityPanel(){
          setOpaque(false);
          setPreferredSize(new Dimension(460,380));
          setBorder(BorderFactory.createTitledBorder("CIE 1931 xy Chromaticity"));
     }
      public void update(ColorModel fg,ColorModel bg){
         this.fg = fg != null ? fg : new ColorModel(Color.BLACK);
         this.bg = bg != null ? bg : new ColorModel(Color.WHITE);
          repaint();
      }
      @Override
     protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2d = (Graphics2D) g.create();
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);
          int w=getWidth(),h=getHeight();
          int pad=48;
          int dw=Math.max(1, w-pad*2);
          int dh=Math.max(1, h-pad*2);
         // Spectral locus (filled horseshoe outline)
         int n = LOCUS.length;
         int[] lx = new int[n], ly = new int[n];
         for (int i = 0; i < n; i++) {
             lx[i] = toScreenX(LOCUS[i][0], pad, dw);
             ly[i] = toScreenY(LOCUS[i][1], pad, dh);
         }
         // Fill the horseshoe with a colour sweep
         for (int i = 0; i < n - 1; i++) {
             float hue = (float) i / (n - 1);
             g2d.setColor(Color.getHSBColor(hue * 0.85f, 0.8f, 0.9f));
             g2d.setStroke(new BasicStroke(3));
             g2d.drawLine(lx[i], ly[i], lx[i+1], ly[i+1]);
         }
         // Close with the purple line of purples
         g2d.setColor(new Color(0xCC44CC));
         g2d.setStroke(new BasicStroke(1.5f));
         g2d.drawLine(lx[n-1], ly[n-1], lx[0], ly[0]);

         // sRGB gamut triangle
         int rx = toScreenX(R_XY[0], pad, dw), ry = toScreenY(R_XY[1], pad, dh);
         int gx = toScreenX(G_XY[0], pad, dw), gy = toScreenY(G_XY[1], pad, dh);
         int bx = toScreenX(B_XY[0], pad, dw), by_ = toScreenY(B_XY[1], pad, dh);
         g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g2d.setColor(new Color(80, 80, 80, 160));
         g2d.drawLine(rx, ry, gx, gy);
         g2d.drawLine(gx, gy, bx, by_);
         g2d.drawLine(bx, by_, rx, ry);
         // Triangle fill
         int[] tx = {rx, gx, bx}, ty_ = {ry, gy, by_};
         g2d.setColor(new Color(180, 180, 255, 28));
         g2d.fillPolygon(tx, ty_, 3);
         // Primary labels
         primaryLabel(g2d, "R", rx + 6, ry - 4, new Color(0xDD2222));
         primaryLabel(g2d, "G", gx - 14, gy - 6, new Color(0x228822));
         primaryLabel(g2d, "B", bx - 14, by_ + 14, new Color(0x2222DD));
         // Axes and grid
         g2d.setColor(new Color(0, 0, 0, 40));
         g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                 1, new float[]{4, 4}, 0));
         // x=0.1 to 0.7 grid lines
         for (double xv = 0.1; xv < 0.8; xv += 0.1) {
             int xi = toScreenX(xv, pad, dw);
             g2d.drawLine(xi, pad, xi, pad + dh);
         }
         for (double yv = 0.1; yv < 0.9; yv += 0.1) {
             int yi = toScreenY(yv, pad, dh);
             g2d.drawLine(pad, yi, pad + dw, yi);
         }
         // Axis labels
         g2d.setColor(getForeground());
         g2d.setStroke(new BasicStroke(1));
         g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
         for (double xv = 0.0; xv <= 0.7; xv += 0.2) {
             int xi = toScreenX(xv, pad, dw);
             g2d.drawString(String.format("%.1f", xv), xi - 6, pad + dh + 14);
         }
         for (double yv = 0.0; yv <= 0.8; yv += 0.2) {
             int yi = toScreenY(yv, pad, dh);
             g2d.drawString(String.format("%.1f", yv), pad - 30, yi + 4);
         }
         // Axis titles
         g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
         g2d.drawString("x", pad + dw / 2, pad + dh + 28);
         AffineTransform orig = g2d.getTransform();
         g2d.rotate(-Math.PI / 2, pad - 38, pad + dh / 2);
         g2d.drawString("y", pad - 38, pad + dh / 2);
         g2d.setTransform(orig);
         // D65 white point
         int wx = toScreenX(W_XY[0], pad, dw), wy = toScreenY(W_XY[1], pad, dh);
         g2d.setColor(Color.WHITE);
         g2d.fillOval(wx - 5, wy - 5, 10, 10);
         g2d.setColor(new Color(0x555555));
         g2d.setStroke(new BasicStroke(1.5f));
         g2d.drawOval(wx - 5, wy - 5, 10, 10);
         g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
         g2d.drawString("D65", wx + 7, wy + 4);

         // ---- FG / BG connection line ---------------------------------------
          double[] fxy = chromaticityOrFallback(fg);
          double[] bxy = chromaticityOrFallback(bg);
          int fdx = toScreenX(fxy[0], pad, dw), fdy = toScreenY(fxy[1], pad, dh);
          int bdx = toScreenX(bxy[0], pad, dw), bdy = toScreenY(bxy[1], pad, dh);

         g2d.setColor(new Color(0, 0, 0, 90));
         g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                 BasicStroke.JOIN_ROUND, 1, new float[]{5, 4}, 0));
         g2d.drawLine(fdx, fdy, bdx, bdy);

         // ---- FG dot --------------------------------------------------------
         drawColorDot(g2d, fdx, fdy, fg.toAwtColor(), "FG  " + fg.toHex(), true);
         // ---- BG dot --------------------------------------------------------
         drawColorDot(g2d, bdx, bdy, bg.toAwtColor(), "BG  " + bg.toHex(), false);

         g2d.dispose();
     }
    // ---- Helpers -----------------------------------------------------------
    private void drawColorDot(Graphics2D g2, int x, int y,
                              Color fill, String label, boolean above) {
        g2.setColor(fill);
        g2.fillOval(x - 9, y - 9, 18, 18);
        g2.setColor(new Color(0, 0, 0, 160));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x - 9, y - 9, 18, 18);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(label);
        int ty = above ? y - 14 : y + 22;
        int tx = Math.max(4, Math.min(x - tw/2, getWidth() - tw - 4));
        // White halo for legibility
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(tx - 3, ty - fm.getAscent(), tw + 6, fm.getHeight(), 4, 4);
        g2.setColor(new Color(0x222222));
        g2.drawString(label, tx, ty);
    }
     private void primaryLabel(Graphics2D g2, String text, int x, int y, Color color) {
         g2.setFont(new Font("SansSerif", Font.BOLD, 12));
         g2.setColor(color);
         g2.drawString(text, x, y);
     }
    private double[] chromaticityOrFallback(ColorModel color) {
        if (color == null) {
            return W_XY;
        }

        double[] xy = color.toChromaticityXy();
        if (xy == null || xy.length < 2) {
            return W_XY;
        }
        if (Double.isNaN(xy[0]) || Double.isNaN(xy[1]) || Double.isInfinite(xy[0]) || Double.isInfinite(xy[1])) {
            return W_XY;
        }
        return xy;
    }
    private int toScreenX(double x, int pad, int dw) {
        double clampedX = Math.max(X_MIN, Math.min(X_MAX, x));
        return pad + (int)((clampedX - X_MIN) / (X_MAX - X_MIN) * dw);
    }
    private int toScreenY(double y, int pad, int dh) {
        // Y axis is inverted in screen coordinates
        double clampedY = Math.max(Y_MIN, Math.min(Y_MAX, y));
        return pad + dh - (int) ((clampedY - Y_MIN) / (Y_MAX - Y_MIN) * dh);
    }
}

