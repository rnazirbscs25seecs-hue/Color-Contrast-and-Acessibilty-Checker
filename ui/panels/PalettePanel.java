package com.colorchecker.ui.panels;

import com.colorchecker.engine.PaletteGenerator;
import com.colorchecker.engine.PaletteGenerator.PaletteEntry;
import com.colorchecker.model.ColorModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/*
Provides two tools powered by PaletteGenerator:
Auto-fix: shows the nearest AA-passing and AAA-passing tweak of
the active foreground color.
Shades strip - 9 lightness shades of the seed hue, colour-coded
by whether they pass AA, AAA, or fail.
Harmony swatches - complementary, triadic, and analogous colors
adjusted to pass WCAG AA against the partner.
*/
public class PalettePanel extends JPanel {
    private final PaletteGenerator paletteGenerator = PaletteGenerator.getInstance();
    private ColorModel fg = new ColorModel(Color.BLACK);
    private ColorModel bg = new ColorModel(Color.WHITE);
    private Consumer<ColorModel> onFgSelected;
    private final JPanel contentPanel = new JPanel();
    private final JPanel autoFixPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JPanel shadesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
    private final JPanel harmonyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

    public PalettePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Palette & Harmony Generator"));

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.add(sectionLabel("Auto-fix suggestions"));
        contentPanel.add(autoFixPanel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(sectionLabel("Lightness shades (click any swatch to use as FG)"));
        contentPanel.add(shadesPanel);
        contentPanel.add(Box.createVerticalStrut(6));
        contentPanel.add(sectionLabel("Accessible harmony colors (complementary · triadic · analogous)"));
        contentPanel.add(harmonyPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        autoFixPanel.setOpaque(false);
        shadesPanel.setOpaque(false);
        harmonyPanel.setOpaque(false);
        refreshAll();
    }

    public void update(ColorModel fg, ColorModel bg) {
        this.fg = fg;
        this.bg = bg;
        refreshAll();
    }

    public void setOnFgSelected(Consumer<ColorModel> onFgSelected) {
        this.onFgSelected = onFgSelected;
    }

    private void refreshAll() {
        buildAutoFixRow();
        buildShadesRow();
        buildHarmonyRow();
        revalidate();
        repaint();
    }

    private void buildAutoFixRow() {
        autoFixPanel.removeAll();
        ColorModel aaFix = paletteGenerator.nearestAaFix(fg, bg);
        ColorModel aaaFix = paletteGenerator.nearestAaFix(bg, fg);
        autoFixPanel.add(swatchCard(aaFix, bg, "Nearest AA", true));
        autoFixPanel.add(swatchCard(aaaFix, bg, "Nearest AAA", true));
        autoFixPanel.add(swatchCard(fg, bg, "Original FG", true));
    }

    private void buildShadesRow() {
        shadesPanel.removeAll();
        List<PaletteEntry> shades = paletteGenerator.generateShades(fg, bg, 11);
        for (PaletteEntry entry : shades) {
            shadesPanel.add(shadeChip(entry));
        }
    }

    private void buildHarmonyRow() {
        harmonyPanel.removeAll();
        String[] harmonyLabels = {
                "Original", "+30° analog", "-30° analog",
                "Complement", "Triadic 1", "Triadic 2"
        };
        List<PaletteEntry> harmony = paletteGenerator.generateHarmony(fg, bg);
        int i = 0;
        for (PaletteEntry entry : harmony) {
            String label = i < harmonyLabels.length ? harmonyLabels[i] : "";
            harmonyPanel.add(swatchCard(entry.getColor(), bg, label, true));
            i++;
        }
    }

    private JPanel swatchCard(ColorModel color, ColorModel partner, String label, boolean clickable) {
        double ratio = com.colorchecker.engine.ContrastEngine.getInstance().contrastRatio(color, partner);
        String level;
        Color levelColor;
        if (ratio >= 7.0) {
            level = "AAA";
            levelColor = new Color(0x1B5E20);
        } else if (ratio >= 4.5) {
            level = "AA";
            levelColor = new Color(0x2E7D32);
        } else if (ratio >= 3.0) {
            level = "AA*";
            levelColor = new Color(0xE65100);
        } else {
            level = "Fail";
            levelColor = new Color(0xB71C1C);
        }

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        JPanel block = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color.toAwtColor());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        block.setOpaque(false);
        block.setPreferredSize(new Dimension(90, 56));
        block.setMaximumSize(new Dimension(90, 56));
        if (clickable && onFgSelected != null) {
            block.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            block.setToolTipText("Click to use " + color.toHex() + " as FG");
            block.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (onFgSelected != null) {
                        onFgSelected.accept(color);
                    }
                }
            });
        }
        card.add(block);

        JLabel hexLabel = new JLabel(color.toHex(), SwingConstants.CENTER);
        hexLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        hexLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel ratioL = new JLabel(String.format("%.2f:1", ratio), SwingConstants.CENTER);
        ratioL.setFont(new Font("SansSerif", Font.BOLD, 10));
        ratioL.setForeground(levelColor);
        ratioL.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        nameLbl.setForeground(new Color(0x888888));
        nameLbl.setAlignmentX(CENTER_ALIGNMENT);

        card.add(hexLabel);
        card.add(ratioL);
        card.add(nameLbl);
        return card;
    }

    private JPanel shadeChip(PaletteEntry entry) {
        ColorModel color = entry.getColor();
        Color borderColor = entry.passesAAA() ? new Color(0x1B5E20)
                : entry.passesAA() ? new Color(0x2E7D32)
                : new Color(0xCCCCCC);

        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color.toAwtColor());
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 6, 6);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setPreferredSize(new Dimension(46, 40));
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setToolTipText(color.toHex() + "  " + String.format("%.2f:1", entry.getRatio())
                + "  " + entry.getBadge());
        chip.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onFgSelected != null) {
                    onFgSelected.accept(color);
                }
            }
        });
        return chip;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(0x666666));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        return l;
    }
}
