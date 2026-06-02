package com.colorchecker.ui.panels;

import com.colorchecker.model.ColorModel;

import javax.swing.*;
import java.awt.*;
//Analysis Tab
//XY Chromaticity diagram
//contrast-ratio bar chart across all CVD modes
public class AnalysisPanel extends JPanel {
    private final ChromaticityPanel chromaPanel = new ChromaticityPanel();
    private final ComparisonChartPanel chartPanel = new ComparisonChartPanel();

    public AnalysisPanel() {
        setLayout(new GridLayout(1, 2, 10, 0));
        setBorder(BorderFactory.createTitledBorder("Analysis and Visualization"));

        JScrollPane leftScroll = new JScrollPane(chromaPanel);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane rightScroll = new JScrollPane(chartPanel);
        rightScroll.setBorder(BorderFactory.createEmptyBorder());
        rightScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        add(leftScroll);
        add(rightScroll);
    }

    public void update(ColorModel fg, ColorModel bg) {
        chromaPanel.update(fg, bg);
        chartPanel.update(fg, bg);
    }
}


