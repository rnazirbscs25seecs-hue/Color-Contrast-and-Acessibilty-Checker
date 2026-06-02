package com.colorchecker.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
/*
  A pill-shaped badge that displays "Pass" or "Fail" with an appropriate
  background color for a single WCAG success criterion.
 */
public class WcagBadge extends JLabel {
    // Status colors
    private static final Color PASS_BG  = new Color(0xE8F5E9);
    private static final Color PASS_FG  = new Color(0x1B5E20);
    private static final Color FAIL_BG  = new Color(0xFFEBEE);
    private static final Color FAIL_FG  = new Color(0xB71C1C);
    private boolean passing=false;
    public WcagBadge(){
        setOpaque(false);
        setHorizontalAlignment(JLabel.CENTER);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setPreferredSize(new Dimension(54,12));
        setPass(false);
    }
    public void setPass(boolean pass){
        this.passing=pass;
        setText(pass?"Pass":"Fail");
        setForeground(pass ? PASS_FG : FAIL_FG);
        repaint();
    }public boolean isPassing() { return passing; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bg = passing ? PASS_BG : FAIL_BG;
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 11, 11));
        g2.dispose();
        super.paintComponent(g);
    }

}
