package com.colorchecker.ui.panels;
import com.colorchecker.engine.BlindnessSimulator;
import com.colorchecker.engine.ContrastEngine;
import com.colorchecker.model.ColorModel;
import com.colorchecker.model.BlindnessType;
import com.colorchecker.model.ContrastResult;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.AffineTransform;
/*
Custom-painted bar chart that shows the WCAG contrast ratio for the selected
colour pair under every supported colour-vision-deficiency simulation mode.
Each bar is colour-coded by WCAG compliance level:
Green  – passes AAA  (≥ 7:1)
Blue   – passes AA   (≥ 4.5:1)
Amber  – passes AA large / UI only (≥ 3:1)
Red    – fails all   (&lt; 3:1)
Three horizontal threshold lines (3:1, 4.5:1, 7:1) are drawn over the bars.
 */

public class ComparisonChartPanel extends JPanel {
    private static final BlindnessType[] TYPES = BlindnessType.values();
    private static final double MAX_DISPLAYED_RATIO = 21.0;
    // WCAG threshold colors
    private static final Color C_AAA  = new Color(0x2E7D32);
    private static final Color C_AA   = new Color(0x1565C0);
    private static final Color C_AALG = new Color(0xE65100);
    private static final Color C_FAIL = new Color(0xB71C1C);
    // Threshold line colors
    private static final Color LINE_3   = new Color(0xE65100, true);
    private static final Color LINE_45  = new Color(0x1565C0, true);
    private static final Color LINE_7   = new Color(0x2E7D32, true);
    private ColorModel fg=new ColorModel(Color.BLACK);
    private ColorModel bg=new ColorModel(Color.WHITE);
    private final ContrastEngine engine=ContrastEngine.getInstance();
    private final BlindnessSimulator simulator=BlindnessSimulator.getInstance();
    public ComparisonChartPanel() {
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(600,300));
        setBorder(BorderFactory.createTitledBorder(
                "Contrast ratio across all colour-vision-deficiency modes"));
    }
    public void update(ColorModel fg,ColorModel bg){
            this.fg=fg;
            this.bg=bg;
            repaint();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        int w = getWidth(), h = getHeight();
        int padL = 52, padR = 20, padT = 20, padB = 60;
        int chartW = w - padL - padR;
        int chartH = h - padT - padB;
        // Chart background
        g2.setColor(new Color(0xF5F5F5));
        g2.fillRoundRect(padL, padT, chartW, chartH, 6, 6);
        int n = TYPES.length;
        int barW   = Math.max(20, (chartW - (n + 1) * 8) / n);
        int gapX   = (chartW - barW * n) / (n + 1);
        int barSpacing = barW + gapX;
        // Compute ratios
        double[] ratios = new double[n];
        for (int i = 0; i < n; i++) {
            ColorModel simFg = simulator.simulate(fg, TYPES[i]);
            ColorModel simBg = simulator.simulate(bg, TYPES[i]);
            ratios[i] = engine.contrastRatio(simFg, simBg);
        }
        // Y-axis
        drawYAxis(g2, padL, padT, chartH);
        // Threshold lines
        drawThresholdLine(g2, 3.0,  "3:1",   LINE_3,  padL, padT, chartW, chartH);
        drawThresholdLine(g2, 4.5,  "4.5:1", LINE_45, padL, padT, chartW, chartH);
        drawThresholdLine(g2, 7.0,  "7:1",   LINE_7,  padL, padT, chartW, chartH);
        // Bars
        for (int i = 0; i < n; i++) {
            double ratio = ratios[i];
            double clampedRatio = Math.min(ratio, MAX_DISPLAYED_RATIO);
            int barH = (int)(clampedRatio / MAX_DISPLAYED_RATIO * chartH);

            int bx = padL + gapX + i * barSpacing;
            int by = padT + chartH - barH;

            // Bar color by compliance level
            Color barColor = barColor(ratio);

            // Bar shadow
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fill(new RoundRectangle2D.Float(bx + 2, by + 2, barW, barH, 4, 4));

            // Bar fill
            g2.setColor(barColor);
            g2.fill(new RoundRectangle2D.Float(bx, by, barW, barH, 4, 4));

            // Bar top glow
            GradientPaint gp = new GradientPaint(
                    bx, by, new Color(255, 255, 255, 50),
                    bx, by + barH / 3, new Color(255, 255, 255, 0));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(bx, by, barW, barH / 3 + 4, 4, 4));
            g2.setPaint(null);

            // Ratio label on top of bar
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.setColor(getForeground());
            String ratioStr = ratio >= 10 ? String.format("%.1f", ratio) : String.format("%.2f", ratio);
            FontMetrics fm = g2.getFontMetrics();
            int lx = bx + (barW - fm.stringWidth(ratioStr)) / 2;
            int ly = by - 3;
            if (ly < padT + 12) ly = by + fm.getAscent() + 2; // inside bar if too close to top
            g2.drawString(ratioStr, lx, ly);
            // X-axis label (rotated)
            drawRotatedLabel(g2, TYPES[i].getDisplayName(),
                    bx + barW / 2, padT + chartH + 8, barW + gapX - 2);
        }
        // Border
        g2.setColor(new Color(0, 0, 0, 30));
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawRoundRect(padL, padT, chartW, chartH, 6, 6);
        // Legend
        drawLegend(g2, padL, padT + chartH + 44);
        g2.dispose();
    }
    // Drawing helpers
    private void drawYAxis(Graphics2D g2, int padL, int padT, int chartH) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(0x888888));
        double[] marks = {0, 3, 4.5, 7, 10, 14, 21};
        for (double mark : marks) {
            int y = padT + chartH - (int)(mark / MAX_DISPLAYED_RATIO * chartH);
            g2.drawString(mark == (int) mark
                            ? String.valueOf((int) mark) : String.valueOf(mark),
                    padL - 30, y + 4);
            g2.setColor(new Color(0, 0, 0, 15));
            g2.drawLine(padL, y, padL - 4, y);
            g2.setColor(new Color(0x888888));
        }
    }
    private void drawThresholdLine(Graphics2D g2, double ratio, String label,
                                   Color color, int padL, int padT,
                                   int chartW, int chartH) {
        int y = padT + chartH - (int)(ratio / MAX_DISPLAYED_RATIO * chartH);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1, new float[]{6, 4}, 0));
        g2.drawLine(padL, y, padL + chartW, y);

        // Label on right side
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(label, padL + chartW + 3, y + 4);
        g2.setStroke(new BasicStroke(1));
    }
    private void drawRotatedLabel(Graphics2D g2, String text, int cx, int topY, int maxW) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        AffineTransform saved = g2.getTransform();
        g2.translate(cx, topY);
        g2.rotate(Math.toRadians(-40));
        g2.setColor(getForeground());
        // Truncate if too long
        FontMetrics fm = g2.getFontMetrics();
        String t = text;
        while (t.length() > 3 && fm.stringWidth(t) > maxW + 20) {
            t = t.substring(0, t.length() - 1);
        }
        g2.drawString(t, 0, 0);
        g2.setTransform(saved);
    }
    private void drawLegend(Graphics2D g2, int x, int y) {
        Object[][] entries = {
                { C_AAA,  "AAA  ≥ 7:1"  },
                { C_AA,   "AA   ≥ 4.5:1"},
                { C_AALG, "AA*  ≥ 3:1 (large/UI)" },
                { C_FAIL, "Fail < 3:1"  }
        };
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        int lx = x;
        for (Object[] entry : entries) {
            g2.setColor((Color) entry[0]);
            g2.fillRoundRect(lx, y - 9, 12, 12, 3, 3);
            g2.setColor(getForeground());
            String label = (String) entry[1];
            g2.drawString(label, lx + 15, y);
            lx += g2.getFontMetrics().stringWidth(label) + 30;
        }
    }
    private static Color barColor(double ratio) {
        if (ratio >= ContrastResult.AAA_NORMAL_THRESHOLD) return C_AAA;
        if (ratio >= ContrastResult.AA_NORMAL_THRESHOLD)  return C_AA;
        if (ratio >= ContrastResult.AA_LARGE_THRESHOLD)   return C_AALG;
        return C_FAIL;
    }
}
