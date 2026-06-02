package com.colorchecker.ui.panels;
import com.colorchecker.engine.BlindnessSimulator;
import com.colorchecker.engine.ContrastEngine;
import com.colorchecker.model.ColorModel;
import com.colorchecker.model.BlindnessType;
import com.colorchecker.model.ContrastResult;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
/*
Displays how the foreground/background pair looks under each supported color
vision deficiency, arranged as a grid of preview tiles.
Each tile renders:
The deficiency name and description
A sample text preview using the simulated colors
The simulated contrast ratio and WCAG level badge
The simulated FG and BG hex values
A severity slider adjusts the simulation intensity from 0 (normal) to 1
(full dichromacy), enabling anomalous trichromacy simulation in between.
 */
public class BlindnessPanel extends JPanel {
    private static final BlindnessSimulator simulator = BlindnessSimulator.getInstance();
    private static final ContrastEngine engine = ContrastEngine.getInstance();
    private ColorModel fg = new ColorModel(Color.BLACK);
    private ColorModel bg = new ColorModel(Color.WHITE);
    double severity = 1.0;
    // The six simulation tiles (excluding NORMAL, which is just the main preview)
    private static final BlindnessType[] TYPES = {
            BlindnessType.NORMAL,
            BlindnessType.PROTANOPIA,
            BlindnessType.DEUTERANOPIA,
            BlindnessType.TRITANOPIA,
            BlindnessType.ACHROMATOPSIA,
            BlindnessType.PROTANOMALY,
            BlindnessType.DEUTERANOMALY
    };

    private final Map<BlindnessType, SimTile> tiles = new EnumMap<>(BlindnessType.class);

    public BlindnessPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createTitledBorder("Color Blindness Simulation"));

        // Severity slider at the top
        add(buildSeverityBar(), BorderLayout.NORTH);

        // Tile grid
        JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (BlindnessType type : TYPES) {
            SimTile tile = new SimTile(type);
            tiles.put(type, tile);
            grid.add(tile);
        }
        add(new JScrollPane(grid), BorderLayout.CENTER);

        // Populate initial state
        refreshAll();
    }

    private JPanel buildSeverityBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setOpaque(false);
        bar.add(new JLabel("Simulation severity:"));

        JSlider slider = new JSlider(0, 100, 100);
        slider.setPreferredSize(new Dimension(220, 28));
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setOpaque(false);

        JLabel valueLabel = new JLabel("100%");
        slider.addChangeListener(e -> {
            severity = slider.getValue() / 100.0;
            valueLabel.setText(slider.getValue() + "%");
            refreshAll();
        });

        bar.add(slider);
        bar.add(valueLabel);
        bar.add(Box.createHorizontalStrut(16));
        bar.add(new JLabel("0 = normal vision · 100 = full dichromacy"));
        return bar;
    }

    public void update(ColorModel fg, ColorModel bg) {
        this.fg = fg;
        this.bg = bg;
        refreshAll();
    }

    private void refreshAll() {
        for (BlindnessType type : TYPES) {
            ColorModel simFg = simulator.simulate(fg, type, severity);
            ColorModel simBg = simulator.simulate(bg, type, severity);
            double ratio = engine.contrastRatio(simFg, simBg);
            tiles.get(type).update(simFg, simBg, ratio);
        }
    }

    private static final class SimTile extends JPanel {

        private final BlindnessType type;

        // Internal sub-components
        private final JPanel colorBar = new JPanel();
        private final JLabel ratioLabel = new JLabel("—", SwingConstants.CENTER);
        private final JLabel fgLabel = new JLabel();
        private final JLabel bgLabel = new JLabel();
        private final JLabel levelLabel = new JLabel("—", SwingConstants.CENTER);

        SimTile(BlindnessType type) {
            this.type = type;
            setLayout(new BorderLayout(0, 3));
            setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD), 1, true));
            setOpaque(true);

            // Title
            JLabel title = new JLabel(type.getDisplayName(), SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 12));
            title.setBorder(BorderFactory.createEmptyBorder(5, 6, 2, 6));
            add(title, BorderLayout.NORTH);

            // Description
            JLabel desc = new JLabel("<html><center><font size=2 color='#888888'>"
                    + type.getDescription() + "</font></center></html>", SwingConstants.CENTER);
            add(desc, BorderLayout.CENTER);

            // Color bar (sample preview strip)
            colorBar.setPreferredSize(new Dimension(0, 40));
            colorBar.setLayout(new GridLayout(1, 1));
            add(colorBar, BorderLayout.CENTER);

            // Bottom info panel
            JPanel info = new JPanel(new GridLayout(3, 1, 0, 1));
            info.setOpaque(false);
            info.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));

            ratioLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            fgLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
            bgLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
            levelLabel.setFont(new Font("SansSerif", Font.BOLD, 11));

            info.add(ratioLabel);
            info.add(levelLabel);

            JPanel hexRow = new JPanel(new GridLayout(1, 2, 4, 0));
            hexRow.setOpaque(false);
            hexRow.add(fgLabel);
            hexRow.add(bgLabel);
            info.add(hexRow);

            add(info, BorderLayout.SOUTH);
        }

        void update(ColorModel simFg,ColorModel simBg, double ratio) {
            // Re-paint color bar as sample text on background
            colorBar.removeAll();
            colorBar.add(new SampleStrip(simFg, simBg));
            colorBar.revalidate();
            colorBar.repaint();

            // Ratio
            String ratioStr = String.format("%.2f:1", ratio);
            ratioLabel.setText(ratioStr);

            // WCAG level
            ContrastResult r = new ContrastResult(simFg, simBg, ratio);
            levelLabel.setText(r.getLevel());
            levelLabel.setForeground(levelColor(r));

            // HEX values
            fgLabel.setText("FG " + simFg.toHex());
            bgLabel.setText("BG " + simBg.toHex());
        }

        private static Color levelColor(ContrastResult r) {
            if (r.passesAAA_Normal()) return new Color(0x1B5E20);
            if (r.passesAA_Normal()) return new Color(0x2E7D32);
            if (r.passesAA_Large()) return new Color(0xE65100);
            return new Color(0xB71C1C);
        }
    }
    // Mini canvas that renders a text sample in simulated colors
    private static final class SampleStrip extends JPanel {
        private final ColorModel fg, bg;

        SampleStrip(ColorModel fg, ColorModel bg) {
            this.fg = fg;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fill background
            g2.setColor(bg.toAwtColor());
            g2.fillRect(0, 0, w, h);
            // Draw text
            g2.setColor(fg.toAwtColor());
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            String sample = "Aa Bb 123";
            int tx = (w - fm.stringWidth(sample)) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(sample, tx, ty);
            g2.dispose();
        }
    }
}
