package com.colorchecker.ui.panels;
import com.colorchecker.model.ContrastResult;
import com.colorchecker.ui.components.RatioGauge;
import com.colorchecker.ui.components.WcagBadge;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;



/*
 Displays the contrast ratio gauge and a full WCAG 2.1 pass/fail table
 (AA + AAA for each criterion: normal text, large text, UI components).
 */
public final class ResultPanel extends JPanel {
    private final RatioGauge  gauge     = new RatioGauge();
    private final JLabel      levelLabel = new JLabel("—", SwingConstants.CENTER);
    // Badge grid: rows = criteria, cols = [AA, AAA]
    private final WcagBadge   badgeAA_Normal  = new WcagBadge();
    private final WcagBadge   badgeAA_Large   = new WcagBadge();
    private final WcagBadge   badgeAA_UI      = new WcagBadge();
    private final WcagBadge   badgeAAA_Normal = new WcagBadge();
    private final WcagBadge   badgeAAA_Large  = new WcagBadge();
    private final JLabel      badgeAAA_UI     = naLabel();   // no AAA requirement for UI
    public ResultPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new TitledBorder("Contrast Analysis"));
        // Gauge + level badge at the top
        JPanel gaugePanel = new JPanel(new BorderLayout(0, 4));
        gaugePanel.setOpaque(false);
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gaugePanel.add(gauge, BorderLayout.CENTER);
        gaugePanel.add(levelLabel, BorderLayout.SOUTH);
        add(gaugePanel, BorderLayout.CENTER);
        // WCAG table at the bottom
        add(buildWcagTable(), BorderLayout.SOUTH);
    }
    // Refresh all components from a fresh  ContrastResult
    public void update(ContrastResult result) {
        gauge.setRatio(result.getRatio());
        // Level label with colored text
        levelLabel.setText(result.getFormattedRatio() + "  ·  " + result.getLevel());
        levelLabel.setForeground(levelColor(result));
        // Badges
        badgeAA_Normal .setPass(result.passesAA_Normal());
        badgeAA_Large  .setPass(result.passesAA_Large());
        badgeAA_UI     .setPass(result.passesAA_UI());
        badgeAAA_Normal.setPass(result.passesAAA_Normal());
        badgeAAA_Large .setPass(result.passesAAA_Large());
    }
    // Layout helpers
    private JPanel buildWcagTable() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.fill   = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        // Header row
        c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
        panel.add(headerLabel("WCAG Criterion"), c);
        c.gridx = 1; c.weightx = 0; c.anchor = GridBagConstraints.CENTER;
        panel.add(headerLabel("AA"), c);
        c.gridx = 2;
        panel.add(headerLabel("AAA"), c);
        // Separator
        c.gridx = 0; c.gridy = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), c);
        // Data rows
        c.gridwidth = 1; c.fill = GridBagConstraints.HORIZONTAL;
        addRow(panel, c, 2, "Normal text  (< 18 pt / 14 pt bold)", badgeAA_Normal,  badgeAAA_Normal);
        addRow(panel, c, 3, "Large text   (≥ 18 pt / 14 pt bold)", badgeAA_Large,   badgeAAA_Large);
        addRow(panel, c, 4, "UI components & graphical objects",    badgeAA_UI,      badgeAAA_UI);
        return panel;
    }
    private void addRow(JPanel panel, GridBagConstraints c,
                        int row, String criterion,
                        JComponent aaBadge, JComponent aaaBadge) {
        c.gridy = row; c.gridx = 0; c.weightx = 1; c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(criterion), c);
        c.gridx = 1; c.weightx = 0; c.anchor = GridBagConstraints.CENTER;
        panel.add(aaBadge, c);
        c.gridx = 2;
        panel.add(aaaBadge, c);
    }
    private static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(0x777777));
        return l;
    }
    private static JLabel naLabel() {
        JLabel l = new JLabel("—", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(0xAAAAAA));
        l.setPreferredSize(new Dimension(54, 22));
        return l;
    }
    private Color levelColor(ContrastResult r) {
        if (r.passesAAA_Normal()) return new Color(0x1B5E20);
        if (r.passesAA_Normal())  return new Color(0x2E7D32);
        if (r.passesAA_Large())   return new Color(0xE65100);
        return new Color(0xB71C1C);
    }
}
