package com.colorchecker.ui;

import com.colorchecker.engine.ContrastEngine;
import com.colorchecker.model.ColorModel;
import com.colorchecker.model.ContrastResult;
import com.colorchecker.ui.panels.AnalysisPanel;
import com.colorchecker.ui.panels.BlindnessPanel;
import com.colorchecker.ui.panels.ColorInputPanel;
import com.colorchecker.ui.panels.HistoryPanel;
import com.colorchecker.ui.panels.PalettePanel;
import com.colorchecker.ui.panels.PreviewPanel;
import com.colorchecker.ui.panels.ResultPanel;
import com.colorchecker.util.ExportUtil;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    private final ColorInputPanel colorInputPanel = new ColorInputPanel();
    private final ResultPanel resultPanel = new ResultPanel();
    private final PreviewPanel previewPanel = new PreviewPanel();
    private final BlindnessPanel blindnessPanel = new BlindnessPanel();
    private final AnalysisPanel analysisPanel = new AnalysisPanel();
    private final PalettePanel palettePanel = new PalettePanel();
    private final HistoryPanel historyPanel = new HistoryPanel();
    private final JLabel ratioDisplay = new JLabel();
    private final ContrastEngine engine = ContrastEngine.getInstance();

    private ContrastResult lastResult;
    private boolean darkMode = false;

    public MainFrame() {
        super("Color Contrast & Accessibility Checker - WCAG 2.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 720));
        setPreferredSize(new Dimension(1180, 820));
        buildLayout();
        wireEventChain();
        installKeyboardShortcuts();
        onColorChanged(colorInputPanel.getEffectiveFgColor(), colorInputPanel.getBgColor());
        pack();
        setLocationRelativeTo(null);
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane vertSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildTopRow(),
                buildTabs()
        );
        vertSplit.setDividerLocation(300);
        vertSplit.setResizeWeight(0.35);
        vertSplit.setBorder(BorderFactory.createEmptyBorder());

        root.add(vertSplit, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)));

        JLabel title = new JLabel("Color Contrast & Accessibility Checker");
        title.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        bar.add(title);
        bar.add(Box.createHorizontalGlue());

        ratioDisplay.setText("--:1");
        ratioDisplay.setName("ratioDisplay");
        ratioDisplay.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 14));
        ratioDisplay.setForeground(new Color(0x444444));
        ratioDisplay.setToolTipText("Current contrast ratio (Ctrl+C to copy)");
        bar.add(ratioDisplay);
        bar.addSeparator();

        JButton copyBtn = new JButton("Copy ratio");
        copyBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        copyBtn.setFocusPainted(false);
        copyBtn.setToolTipText("Copy contrast ratio to clipboard (Ctrl+C)");
        copyBtn.addActionListener(e -> copyRatioToClipboard());
        bar.add(copyBtn);
        bar.addSeparator();

        JToggleButton themeBtn = new JToggleButton("Light");
        themeBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        themeBtn.setFocusPainted(false);
        themeBtn.addActionListener(e -> {
            darkMode = themeBtn.isSelected();
            themeBtn.setText(darkMode ? "Dark" : "Light");
            applyTheme(darkMode);
        });
        bar.add(themeBtn);
        bar.addSeparator();

        JButton pngBtn = new JButton("PNG");
        pngBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        pngBtn.setFocusPainted(false);
        pngBtn.setToolTipText("Export PNG report (Ctrl+S)");
        pngBtn.addActionListener(e -> exportPng());
        bar.add(pngBtn);

        JButton csvBtn = new JButton("CSV");
        csvBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        csvBtn.setFocusPainted(false);
        csvBtn.setToolTipText("Export CSV data (Ctrl+E)");
        csvBtn.addActionListener(e -> exportCsv());
        bar.add(csvBtn);
        bar.add(Box.createHorizontalStrut(8));

        return bar;
    }

    private JPanel buildTopRow() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, colorInputPanel, resultPanel);
        split.setDividerLocation(460);
        split.setResizeWeight(0.47);
        split.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        tabs.addTab("\uD83D\uDCC4  Preview", previewPanel);
        tabs.addTab("\uD83D\uDC41  Blindness", blindnessPanel);
        tabs.addTab("\uD83D\uDCC8  Analysis", analysisPanel);
        tabs.addTab("\uD83C\uDFA8  Palette", palettePanel);
        tabs.addTab("\uD83D\uDD52  History", historyPanel);
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return tabs;
    }

    private void wireEventChain() {
        colorInputPanel.addColorChangeListener(this::onColorChanged);

        historyPanel.setOnRestore((fg, bg) -> {
            colorInputPanel.setFgColor(fg);
            colorInputPanel.setBgColor(bg);
            onColorChanged(colorInputPanel.getEffectiveFgColor(), colorInputPanel.getBgColor());
        });

        palettePanel.setOnFgSelected(newFg -> {
            colorInputPanel.setFgColor(newFg);
            onColorChanged(colorInputPanel.getEffectiveFgColor(), colorInputPanel.getBgColor());
        });
    }

    private void onColorChanged(ColorModel fg, ColorModel bg) {
        lastResult = engine.evaluate(fg, bg);

        ratioDisplay.setText(lastResult.getFormattedRatio() + "  " + lastResult.getLevel());
        ratioDisplay.setForeground(levelColor(lastResult));

        resultPanel.update(lastResult);
        previewPanel.update(fg, bg);
        blindnessPanel.update(fg, bg);
        analysisPanel.update(fg, bg);
        palettePanel.update(fg, bg);
        historyPanel.push(fg, bg);
    }

    private void installKeyboardShortcuts() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copyRatio");
        am.put("copyRatio", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                copyRatioToClipboard();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "exportPng");
        am.put("exportPng", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportPng();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "exportCsv");
        am.put("exportCsv", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                exportCsv();
            }
        });

        im.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                "swap"
        );
        am.put("swap", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ColorModel tmp = colorInputPanel.getFgColor();
                colorInputPanel.setFgColor(colorInputPanel.getBgColor());
                colorInputPanel.setBgColor(tmp);
                onColorChanged(colorInputPanel.getEffectiveFgColor(), colorInputPanel.getBgColor());
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK), "clearHistory");
        am.put("clearHistory", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                historyPanel.clear();
            }
        });
    }

    private void copyRatioToClipboard() {
        if (lastResult == null) {
            return;
        }

        String text = lastResult.getFormattedRatio() + " - " + lastResult.getLevel()
                + " - FG " + lastResult.getFg().toHex()
                + " on BG " + lastResult.getBg().toHex();

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "Copied to clipboard:\n" + text, "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportPng() {
        if (lastResult != null) {
            ExportUtil.exportAsPng(this, lastResult);
        }
    }

    private void exportCsv() {
        if (lastResult != null) {
            ExportUtil.exportAsCsv(this, lastResult);
        }
    }

    private void applyTheme(boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("Panel.background", new Color(0x3C3F41));
                UIManager.put("Label.foreground", new Color(0xBBBBBB));
                UIManager.put("TextField.background", new Color(0x4A4E50));
                UIManager.put("TextField.foreground", new Color(0xDDDDDD));
                UIManager.put("Spinner.background", new Color(0x4A4E50));
            } else {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }

            SwingUtilities.updateComponentTreeUI(this);
            pack();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not switch theme:\n" + ex.getMessage(),
                    "Theme error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static Color levelColor(ContrastResult result) {
        if (result == null) {
            return new Color(0x444444);
        }
        if (result.passesAAA_Normal()) {
            return new Color(0x1B5E20);
        }
        if (result.passesAA_Normal()) {
            return new Color(0x2E7D32);
        }
        if (result.passesAA_Large()) {
            return new Color(0xE65100);
        }
        return new Color(0xB71C1C);
    }
}
