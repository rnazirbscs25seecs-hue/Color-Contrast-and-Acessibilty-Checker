package com.colorchecker.ui.panels;

import com.colorchecker.model.ColorModel;

import javax.swing.*;
import java.awt.*;

/*
 Renders a live text preview using the selected foreground / background pair.

 Shows several text styles that correspond to WCAG size thresholds:
 Normal text  (14 px regular)
 Large text   (24 px regular  = ~18 pt)
 Bold large   (18.66 px bold  = ~14 pt bold)
 A "button" mockup (UI component)
 A text field border mockup (UI component).
 */
public class PreviewPanel extends JPanel {
    private static final Color[] CARD_ACCENTS = {
            new Color(0xC084FC),
            new Color(0x60A5FA),
            new Color(0x34D399),
            new Color(0xFBBF24)
    };

    private ColorModel fgColor = new ColorModel(Color.BLACK);
    private ColorModel bgColor = new ColorModel(Color.WHITE);
    private final Canvas canvas = new Canvas();

    public PreviewPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Live Preview"));
        add(new JScrollPane(canvas), BorderLayout.CENTER);
    }

    public void update(ColorModel fgColor, ColorModel bgColor) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        canvas.repaint();
    }

    public final class Canvas extends JPanel {
        Canvas() {
            setPreferredSize(new Dimension(600, 460));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int pad = 24;
            int cardX = 15;
            int cardW = Math.max(220, w - 30);

            g2.setColor(new Color(0x242424));
            g2.fillRect(0, 0, w, h);

            int y = pad;

            y = drawSectionLabel(g2, "Normal text (14px)", pad, y);
            drawPreviewCard(g2, cardX, y, cardW, 76, CARD_ACCENTS[0]);
            g2.setColor(fgColor.toAwtColor());
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.drawString("The quick brown fox jumps over the lazy dog. 1234567890", cardX + 30, y + 30);
            g2.drawString("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", cardX + 30, y + 60);
            y += 90;

            y = drawSectionLabel(g2, "Large text (24px / ~18pt) - lower AA threshold (3:1)", pad, y);
            drawPreviewCard(g2, cardX, y, cardW, 86, CARD_ACCENTS[1]);
            g2.setColor(fgColor.toAwtColor());
            g2.setFont(new Font("SansSerif", Font.PLAIN, 24));
            g2.drawString("The quick brown fox jumps.", cardX + 30, y + 56);
            y += 100;

            y = drawSectionLabel(g2, "Bold large (18px bold = ~14pt bold)", pad, y);
            drawPreviewCard(g2, cardX, y, cardW, 80, CARD_ACCENTS[2]);
            g2.setColor(fgColor.toAwtColor());
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("Accessibility matters for everyone.", cardX + 30, y + 48);
            y += 94;

            y = drawSectionLabel(g2, "UI components - button & text field mockup", pad, y);
            drawPreviewCard(g2, cardX, y, cardW, 92, CARD_ACCENTS[3]);
            g2.setColor(fgColor.toAwtColor());
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawRoundRect(cardX + 30, y + 24, 110, 40, 8, 8);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString("Submit", cardX + 54, y + 49);

            g2.drawRoundRect(cardX + 156, y + 24, 208, 40, 8, 8);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            Color textFieldHint = fgColor.toAwtColor();
            g2.setColor(new Color(textFieldHint.getRed(), textFieldHint.getGreen(),
                    textFieldHint.getBlue(), 160));
            g2.drawString("Placeholder text...", cardX + 172, y + 49);
            g2.dispose();
        }

        private int drawSectionLabel(Graphics2D g2, String text, int x, int y) {
            g2.setColor(new Color(0x9A9A9A));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(text.toUpperCase(), x, y + 12);
            return y + 18;
        }

        private void drawPreviewCard(Graphics2D g2, int x, int y, int width, int height, Color accent) {
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(x, y + 4, width, height, 16, 16);

            g2.setColor(bgColor.toAwtColor());
            g2.fillRoundRect(x, y, width, height, 16, 16);

            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
            g2.drawRoundRect(x, y, width, height, 16, 16);

        }
    }
}
