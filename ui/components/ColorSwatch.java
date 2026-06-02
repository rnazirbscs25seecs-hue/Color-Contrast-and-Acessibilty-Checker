package com.colorchecker.ui.components;
import com.colorchecker.model.ColorModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
//A rounded rectangular color swatch that:
//displays the current color as its fill.
//Shows the hex value of the color centered inside.

public class ColorSwatch extends JComponent {
    private ColorModel color;
    private final List<Consumer<ColorModel>> listeners=new ArrayList<>();
    private String label="";
    public ColorSwatch(ColorModel initial){
        this.color=initial;
        setPreferredSize(new Dimension(120,80));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Click to choose a color");

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openColorChooser();
            }
        });
    }
    public void setLabel(String label){
        this.label=label;
    }
    //Register a callback invoked whenever the user picks a new color.
    public void addColorChangeListener(Consumer<ColorModel> listener) {
        listeners.add(listener);
    }
    //Programatically set the color without opening the chooser.
    public void setColor(ColorModel c){
        this.color=c;
        repaint();
    }
    public ColorModel getColor(){
        return color;
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        RoundRectangle2D bg = new RoundRectangle2D.Float(0, 0, w, h, 16, 16);
        // Fill with current color
        g2.setColor(color.toAwtColor());
        g2.fill(bg);
        // Subtle border
        g2.setColor(new Color(0, 0, 0, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(bg);
        // HEX label :choose white or black for legibility
        double luminance = luminance(color);
        Color textColor = luminance > 0.35 ? new Color(0x222222) : new Color(0xEEEEEE);
        g2.setColor(textColor);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        String hex = color.toHex();
        int tx = (w - fm.stringWidth(hex)) / 2;
        int ty = h / 2 + fm.getAscent() / 2 - 2;
        g2.drawString(hex, tx, ty);
        // Role label (FG / BG)
        if (!label.isBlank()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            fm = g2.getFontMetrics();
            g2.drawString(label, (w - fm.stringWidth(label)) / 2, ty + 16);
        }
        g2.dispose();
    }
    // Private
    private void openColorChooser() {
        JColorChooser chooser = new JColorChooser(color.toAwtColor());
        int result = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                chooser,
                "Choose " + (label.isBlank() ? "color" : label) + " color",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            Color chosen = chooser.getColor();
            if (chosen != null) {
                color = new ColorModel(chosen);
                repaint();
                listeners.forEach(l -> l.accept(color));
            }
        }
    }
    /* Quick luminance estimate [0,1] for text-color decision. */
    private double luminance(ColorModel c) {
        return (0.2126 * c.getR() + 0.7152 * c.getG() + 0.0722 * c.getB()) / 255.0;
    }
}
