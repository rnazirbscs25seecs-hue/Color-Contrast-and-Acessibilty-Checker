package com.colorchecker.ui.panels;
import com.colorchecker.engine.ContrastEngine;
import com.colorchecker.model.ColorModel;
import com.colorchecker.model.ContrastResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
/*
  Maintains a scrollable list of up to #MAX_ENTRIES recently evaluated
  color pairs.  Clicking a history row re-loads that pair into the main tool.
 */
public class HistoryPanel extends JPanel {
    private static final int MAX_ENTRIES=30;
    private final List<HistoryEntry> entries=new ArrayList<>();
    private final DefaultListModel<HistoryEntry>model = new DefaultListModel<>();
    private final JList<HistoryEntry>list=new JList<>();
    // Callback invoked when the user clicks a history row: (fg, bg).
    private BiConsumer<ColorModel,ColorModel> onRestore;
    private final ContrastEngine engine = ContrastEngine.getInstance();
    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("History"));
        list.setCellRenderer(new HistoryCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(54);
        list.setOpaque(false);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    HistoryEntry entry = list.getSelectedValue();
                    if (entry != null && onRestore != null) {
                        onRestore.accept(entry.fg, entry.bg);
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
        JLabel hint = new JLabel("Double-click any row to restore that pair", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(0x999999));
        hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        add(hint, BorderLayout.SOUTH);
    }
    // Record a new pair; duplicate consecutive entries are skipped.
    public void push(ColorModel fg,ColorModel bg)
    {
        // Skip exact duplicate of the most recent entry
        if (!entries.isEmpty()) {
            HistoryEntry last = entries.get(entries.size() - 1);
            if (last.fg.equals(fg) && last.bg.equals(bg)) return;
        }
        ContrastResult result = engine.evaluate(fg, bg);
        HistoryEntry entry = new HistoryEntry(fg, bg, result);
        entries.add(entry);
        model.insertElementAt(entry, 0); // newest at top
        // Trim to maximum
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
            model.remove(model.getSize() - 1);
        }
    }
    public void setOnRestore(BiConsumer<ColorModel, ColorModel> cb) { onRestore = cb; }
    public void clear() {
        entries.clear();
        model.clear();
    }
    public  final class HistoryCellRenderer implements ListCellRenderer<HistoryEntry> {
        @Override
        public Component getListCellRendererComponent(
                JList<? extends HistoryEntry> list,
                HistoryEntry entry, int index,
                boolean isSelected, boolean cellHasFocus) {

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            row.setBackground(isSelected ? new Color(0xE3F2FD) : list.getBackground());
            row.setOpaque(true);

            // Left: two color swatches side by side
            JPanel swatches = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            swatches.setOpaque(false);
            swatches.add(miniSwatch(entry.fg, "FG"));
            swatches.add(miniSwatch(entry.bg, "BG"));
            row.add(swatches, BorderLayout.WEST);

            // Centre: hex values + ratio
            JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
            info.setOpaque(false);
            JLabel colors = new JLabel(entry.fg.toHex() + "  on  " + entry.bg.toHex());
            colors.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JLabel ratioDet = new JLabel(entry.result.getFormattedRatio()
                    + "  ·  " + entry.result.getLevel());
            ratioDet.setFont(new Font("SansSerif", Font.BOLD, 11));
            ratioDet.setForeground(levelColor(entry.result));
            info.add(colors);
            info.add(ratioDet);
            row.add(info, BorderLayout.CENTER);

            // Right: timestamp
            JLabel ts = new JLabel(entry.timestamp, SwingConstants.RIGHT);
            ts.setFont(new Font("SansSerif", Font.PLAIN, 10));
            ts.setForeground(new Color(0xAAAAAA));
            row.add(ts, BorderLayout.EAST);

            return row;
        }
        private JPanel miniSwatch(ColorModel c, String role) {
            JPanel p = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(c.toAwtColor());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(new Color(0, 0, 0, 50));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                    g2.dispose();
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(40, 28));
            p.setToolTipText(role + " " + c.toHex());
            return p;
        }

        private Color levelColor(ContrastResult r) {
            if (r.passesAAA_Normal()) return new Color(0x1B5E20);
            if (r.passesAA_Normal())  return new Color(0x2E7D32);
            if (r.passesAA_Large())   return new Color(0xE65100);
            return new Color(0xB71C1C);
        }
    }public final class HistoryEntry {
        final ColorModel fg;
        final ColorModel     bg;
        final ContrastResult result;
        final String         timestamp;

        HistoryEntry(ColorModel fg, ColorModel bg, ContrastResult result) {
            this.fg        = fg;
            this.bg        = bg;
            this.result    = result;
            this.timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

}
