package com.colorchecker.ui.components;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
/*
Custom Swing component that renders a semicircular arc gauge displaying
the contrast ratio with colored zones corresponding to WCAG thresholds.
Zone boundaries (on a 1 – 21 scale):
1.0 – 3.0   red    (fails all)
3.0 – 4.5   amber  (passes AA large/UI only)
4.5 – 7.0   blue   (passes AA normal)
7.0 – 21.0  green  (passes AAA)
 */
public class RatioGauge extends JPanel {
    private double ratio=1.0;
    private static final double MAX_RATIO=21.0;
    private static final Color ZONE_FAIL   = new Color(0xFF5252); // red
    private static final Color ZONE_AA_LG  = new Color(0xFFB300); // amber
    private static final Color ZONE_AA     = new Color(0x42A5F5); // blue
    private static final Color ZONE_AAA    = new Color(0x66BB6A); // green
    private static final Color NEEDLE      = new Color(0x212121);
    private static final Color TRACK_BG    = new Color(0xE0E0E0);
    public RatioGauge(){
        setOpaque(false);
        setPreferredSize(new Dimension(220, 130));
    }
    public void setRatio(double ratio){
        this.ratio = Math.max(1.0, Math.min(MAX_RATIO, ratio));
        repaint();
    }
    public double getRatio(){
        return ratio;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int w = getWidth();
        int h = getHeight();
        // The arc spans 180° (from 180° to 0°, i.e., left to right along the top).
        // Centre the arc in the middle bottom of the component.
        int arcDiam = Math.min(w - 24, (h - 30) * 2);
        int cx = w / 2;
        int cy = h - 20;
        int r  = arcDiam / 2;
        // Track background (gray half-ring)
        drawArcTrack(g2, cx, cy, r, 0, 180, TRACK_BG, 22);
        // Colored zone segments (Fail / AA-Large / AA / AAA)
        double[] boundaries = { 1.0, 3.0, 4.5, 7.0, MAX_RATIO };
        Color[]  zoneColors  = { ZONE_FAIL, ZONE_AA_LG, ZONE_AA, ZONE_AAA };
        for (int i = 0; i < zoneColors.length; i++) {
            float startAngle = (float) ratioToAngle(boundaries[i]);
            float sweepAngle = (float) (ratioToAngle(boundaries[i + 1]) - startAngle);
            drawArcTrack(g2, cx, cy, r, startAngle, sweepAngle, zoneColors[i], 14);
        }
        // Needle
        drawNeedle(g2, cx, cy, r - 7);
        // Ratio text
        String text = String.format("%.2f:1", ratio);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D tb = fm.getStringBounds(text, g2);
        g2.setColor(getForeground());
        g2.drawString(text, (int)(cx - tb.getWidth() / 2), cy - 6);
        // Min / max labels
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(0x666666));
        g2.drawString("1:1",  cx - r + 4, cy + 14);
        g2.drawString("21:1", cx + r - 30, cy + 14);
        g2.dispose();
    }
    // Private drawing helpers
    /*
     Draw a thick arc segment (used for both the track and zone coloring).
     Angles are in degrees, where 0° = 3 o'clock, going counter-clockwise.
     We map the left end of the gauge to 180° and the right end to 0°.
     cx, cy   :    centre of the arc circle
     radius    :   radius to the arc midpoint
     startDeg :    start angle in screen degrees
     sweepDeg :    arc sweep in screen degrees
     color   :     fill color of the arc stroke
     strokeWidth : width of the painted stroke
     */
    private void drawArcTrack(Graphics2D g2, int cx, int cy, int radius,
                              float startDeg, float sweepDeg, Color color, int strokeWidth) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        Arc2D.Float arc = new Arc2D.Float(
                cx - radius, cy - radius, radius * 2, radius * 2,
                startDeg, sweepDeg, Arc2D.OPEN
        );
        g2.draw(arc);
    }
    //Draw a needle pointing at the current ratio value.
    private void drawNeedle(Graphics2D g2, int cx, int cy, int length) {
        double angleDeg = ratioToAngle(ratio);
        double angleRad = Math.toRadians(angleDeg);
        int tx = (int) (cx + length * Math.cos(angleRad));
        int ty = (int) (cy - length * Math.sin(angleRad));
        // Needle line
        g2.setColor(NEEDLE);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy, tx, ty);
        // Pivot circle
        g2.setColor(NEEDLE);
        g2.fillOval(cx - 7, cy - 7, 14, 14);
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 4, cy - 4, 8, 8);
    }
    /*
      Map a contrast ratio in [1, 21] to a screen angle in degrees.
     The gauge sweeps from 180° (ratio = 1) to 0° (ratio = 21).
     */
    private double ratioToAngle(double r) {
        // Normalise to [0,1] using logarithmic scale for better visual spread
        double normalized = Math.log(r) / Math.log(MAX_RATIO);
        return 180.0 - normalized * 180.0;
    }
}
