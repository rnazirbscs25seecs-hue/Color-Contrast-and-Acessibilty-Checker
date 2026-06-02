package com.colorchecker.ui.panels;
import com.colorchecker.model.ColorModel;
import com.colorchecker.ui.components.ColorSwatch;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
/*
  Color-selection panel for Foreground and Background.
  Each section provides HEX, RGB, HSL, HSB spinners.
  Foreground also includes an Alpha transparency slider.
  All controls are bidirectionally synchronised.
 A central Swap button exchanges FG and BG.

*/
public class ColorInputPanel extends JPanel {
    private ColorModel fgColor=new ColorModel(Color.BLACK);
    private ColorModel bgColor=new ColorModel(Color.WHITE);
    private int fgAlpha=255;
    private final List<BiConsumer<ColorModel, ColorModel>> changeListeners = new ArrayList<>();
    private boolean updating = false;
    // FG controls
    private final ColorSwatch fgSwatch;
    private final JTextField  fgHex     = new JTextField(7);
    private final JSpinner    fgR       = intSp(0, 255);
    private final JSpinner    fgG       = intSp(0, 255);
    private final JSpinner    fgB       = intSp(0, 255);
    private final JSpinner    fgHhsl    = dblSp(0, 360);
    private final JSpinner    fgShsl    = dblSp(0, 100);
    private final JSpinner    fgLhsl    = dblSp(0, 100);
    private final JSpinner    fgHhsb    = dblSp(0, 360);
    private final JSpinner    fgShsb    = dblSp(0, 100);
    private final JSpinner    fgBhsb    = dblSp(0, 100);
    private final JSlider     fgAlphaSl = new JSlider(0, 255, 255);
    private final JLabel      fgAlphaLbl= new JLabel("255 (100%)");
    // BG controls
    private final ColorSwatch bgSwatch;
    private final JTextField  bgHex     = new JTextField(7);
    private final JSpinner    bgR       = intSp(0, 255);
    private final JSpinner    bgG       = intSp(0, 255);
    private final JSpinner    bgB       = intSp(0, 255);
    private final JSpinner    bgHhsl    = dblSp(0, 360);
    private final JSpinner    bgShsl    = dblSp(0, 100);
    private final JSpinner    bgLhsl    = dblSp(0, 100);
    private final JSpinner    bgHhsb    = dblSp(0, 360);
    private final JSpinner    bgShsb    = dblSp(0, 100);
    private final JSpinner    bgBhsb    = dblSp(0, 100);
    public ColorInputPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        fgSwatch = new ColorSwatch(fgColor);
        bgSwatch = new ColorSwatch(bgColor);
        fgSwatch.setLabel("FG");
        bgSwatch.setLabel("BG");
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0; c.gridy = 0; c.weightx = 0.46;
        add(buildSection(true), c);
        c.gridx = 1; c.weightx = 0.08; c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        add(buildSwapBtn(), c);
        c.gridx = 2; c.weightx = 0.46; c.fill = GridBagConstraints.BOTH;
        add(buildSection(false), c);
        bindFg();
        bindBg();
        pushFg(fgColor);
        pushBg(bgColor);

    }
    public ColorModel getFgColor() {
        return fgColor;
    }
    public ColorModel getBgColor() {
        return bgColor;
    }
    public ColorModel getEffectiveFgColor() {
        if (fgAlpha >= 255) {
            return fgColor;
        }
        if (fgAlpha <= 0) {
            return bgColor;
        }

        double alpha = fgAlpha / 255.0;
        int r = blendChannel(fgColor.getR(), bgColor.getR(), alpha);
        int g = blendChannel(fgColor.getG(), bgColor.getG(), alpha);
        int b = blendChannel(fgColor.getB(), bgColor.getB(), alpha);
        return new ColorModel(r, g, b);
    }
    public int getFgAlpha(){
         return fgAlpha;
     }
    public void setFgAlpha(int alpha){
        fgAlpha = Math.max(0, Math.min(255, alpha));
        fgAlphaSl.setValue(fgAlpha);
        updateFgAlphaLabel();
    }
    public void setBgColor(ColorModel c){
        bgColor=c;pushBg(c);
    }
    public void setFgColor(ColorModel c){
        fgColor=c;
        pushFg(c);
    }
    public void addColorChangeListener(BiConsumer<ColorModel,ColorModel> l) {
        changeListeners.add(l);
    }
    private JPanel buildSection(boolean isFg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder(isFg ? "Foreground" : "Background"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 3, 2, 3);
        c.fill   = GridBagConstraints.HORIZONTAL;

        ColorSwatch swatch = isFg ? fgSwatch : bgSwatch;
        JTextField  hex    = isFg ? fgHex    : bgHex;
        JSpinner    r = isFg ? fgR : bgR, g = isFg ? fgG : bgG, b = isFg ? fgB : bgB;
        JSpinner    hh = isFg ? fgHhsl : bgHhsl;
        JSpinner    sh = isFg ? fgShsl : bgShsl;
        JSpinner    lh = isFg ? fgLhsl : bgLhsl;
        JSpinner    hb = isFg ? fgHhsb : bgHhsb;
        JSpinner    sb = isFg ? fgShsb : bgShsb;
        JSpinner    bb = isFg ? fgBhsb : bgBhsb;
        int row = 0;
        // Swatch
        c.gridx = 0; c.gridy = row++; c.gridwidth = 4; c.weightx = 1; c.ipady = 50;
        p.add(swatch, c);
        c.ipady = 0; c.gridwidth = 1;
        // HEX row
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        p.add(lbl("HEX"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        p.add(hex, c);
        row++; c.gridwidth = 1;
        // RGB row
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        p.add(lbl("RGB"), c);
        addSpin(p, c, r, "R", 1, row);
        addSpin(p, c, g, "G", 2, row);
        addSpin(p, c, b, "B", 3, row);
        row++;
        // HSL row
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        p.add(lbl("HSL"), c);
        addSpin(p, c, hh, "H°", 1, row);
        addSpin(p, c, sh, "S%", 2, row);
        addSpin(p, c, lh, "L%", 3, row);
        row++;
        // HSB row
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        p.add(lbl("HSB"), c);
        addSpin(p, c, hb, "H°", 1, row);
        addSpin(p, c, sb, "S%", 2, row);
        addSpin(p, c, bb, "B%", 3, row);
        row++;
        // Alpha slider (FG only)
        if (isFg) {
            c.gridy = row; c.gridx = 0; c.weightx = 0; c.gridwidth = 1;
            p.add(lbl("Alpha"), c);
            fgAlphaSl.setOpaque(false);
            fgAlphaSl.setPreferredSize(new Dimension(90, 24));
            fgAlphaSl.setToolTipText("Foreground alpha: blended contrast = FG*alpha + BG*(1-alpha)");
            c.gridx = 1; c.gridwidth = 2; c.weightx = 1;
            p.add(fgAlphaSl, c);
            fgAlphaLbl.setFont(new Font("Monospaced", Font.PLAIN, 10));
            c.gridx = 3; c.gridwidth = 1; c.weightx = 0;
            p.add(fgAlphaLbl, c);
        }
        return p;
    }
    private JButton buildSwapBtn() {
        JButton btn = new JButton("⇅");
        btn.setFont(new Font("SansSerif", Font.BOLD, 20));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setToolTipText("Swap FG ↔ BG  (Ctrl+Shift+S)");
        btn.addActionListener(e -> {
            ColorModel tmp = fgColor;
            setFgColor(bgColor);
            setBgColor(tmp);
            fire();
        });
        return btn;
    }
    //  Binding
    private void bindFg() {
        fgSwatch.addColorChangeListener(c -> { if (!updating) { fgColor=c; pushFg(c); fire(); } });
        docListen(fgHex, () -> {
            if (updating) return;
            String t = fgHex.getText().trim();
            if ((t.startsWith("#") && t.length()==7) || t.length()==6) {
                fgColor = ColorModel.fromHex(t); pushFg(fgColor, false); fire();
            }
        });
        spListen(fgR,  () -> { if (!updating) { fgColor=fromRGB(fgR,fgG,fgB);     pushFg(fgColor); fire(); } });
        spListen(fgG,  () -> { if (!updating) { fgColor=fromRGB(fgR,fgG,fgB);     pushFg(fgColor); fire(); } });
        spListen(fgB,  () -> { if (!updating) { fgColor=fromRGB(fgR,fgG,fgB);     pushFg(fgColor); fire(); } });
        spListen(fgHhsl,() -> { if (!updating) { fgColor=fromHSL(fgHhsl,fgShsl,fgLhsl); pushFg(fgColor); fire(); } });
        spListen(fgShsl,() -> { if (!updating) { fgColor=fromHSL(fgHhsl,fgShsl,fgLhsl); pushFg(fgColor); fire(); } });
        spListen(fgLhsl,() -> { if (!updating) { fgColor=fromHSL(fgHhsl,fgShsl,fgLhsl); pushFg(fgColor); fire(); } });
        spListen(fgHhsb,() -> { if (!updating) { fgColor=fromHSB(fgHhsb,fgShsb,fgBhsb); pushFg(fgColor); fire(); } });
        spListen(fgShsb,() -> { if (!updating) { fgColor=fromHSB(fgHhsb,fgShsb,fgBhsb); pushFg(fgColor); fire(); } });
        spListen(fgBhsb,() -> { if (!updating) { fgColor=fromHSB(fgHhsb,fgShsb,fgBhsb); pushFg(fgColor); fire(); } });
        fgAlphaSl.addChangeListener(e -> {
            fgAlpha = fgAlphaSl.getValue();
            updateFgAlphaLabel();
            fire();
        });
    }
    private void bindBg() {
        bgSwatch.addColorChangeListener(c -> { if (!updating) { bgColor=c; pushBg(c); fire(); } });
        docListen(bgHex, () -> {
            if (updating) return;
            String t = bgHex.getText().trim();
            if ((t.startsWith("#") && t.length()==7) || t.length()==6) {
                bgColor = ColorModel.fromHex(t); pushBg(bgColor, false); fire();
            }
        });
        spListen(bgR,   () -> { if (!updating) { bgColor=fromRGB(bgR,bgG,bgB);     pushBg(bgColor); fire(); } });
        spListen(bgG,   () -> { if (!updating) { bgColor=fromRGB(bgR,bgG,bgB);     pushBg(bgColor); fire(); } });
        spListen(bgB,   () -> { if (!updating) { bgColor=fromRGB(bgR,bgG,bgB);     pushBg(bgColor); fire(); } });
        spListen(bgHhsl,() -> { if (!updating) { bgColor=fromHSL(bgHhsl,bgShsl,bgLhsl); pushBg(bgColor); fire(); } });
        spListen(bgShsl,() -> { if (!updating) { bgColor=fromHSL(bgHhsl,bgShsl,bgLhsl); pushBg(bgColor); fire(); } });
        spListen(bgLhsl,() -> { if (!updating) { bgColor=fromHSL(bgHhsl,bgShsl,bgLhsl); pushBg(bgColor); fire(); } });
        spListen(bgHhsb,() -> { if (!updating) { bgColor=fromHSB(bgHhsb,bgShsb,bgBhsb); pushBg(bgColor); fire(); } });
        spListen(bgShsb,() -> { if (!updating) { bgColor=fromHSB(bgHhsb,bgShsb,bgBhsb); pushBg(bgColor); fire(); } });
        spListen(bgBhsb,() -> { if (!updating) { bgColor=fromHSB(bgHhsb,bgShsb,bgBhsb); pushBg(bgColor); fire(); } });
    }
    //Push (color to controls)
    private void pushFg(ColorModel c) {
        pushFg(c, true);
    }

    private void pushFg(ColorModel c, boolean updateHexField) {
        if (updating) return;
        updating = true;
        try {
            fgSwatch.setColor(c);
            if (updateHexField) {
                fgHex.setText(c.toHex());
            }
            fgR.setValue(c.getR()); fgG.setValue(c.getG()); fgB.setValue(c.getB());
            double[] hsl = c.toHsl();
            fgHhsl.setValue(r1(hsl[0])); fgShsl.setValue(r1(hsl[1]*100)); fgLhsl.setValue(r1(hsl[2]*100));
            double[] hsb = c.toHsb();
            fgHhsb.setValue(r1(hsb[0])); fgShsb.setValue(r1(hsb[1]*100)); fgBhsb.setValue(r1(hsb[2]*100));
        } finally { updating = false; }
    }

    private void pushBg(ColorModel c) {
        pushBg(c, true);
    }

    private void pushBg(ColorModel c, boolean updateHexField) {
        if (updating) return;
        updating = true;
        try {
            bgSwatch.setColor(c);
            if (updateHexField) {
                bgHex.setText(c.toHex());
            }
            bgR.setValue(c.getR()); bgG.setValue(c.getG()); bgB.setValue(c.getB());
            double[] hsl = c.toHsl();
            bgHhsl.setValue(r1(hsl[0])); bgShsl.setValue(r1(hsl[1]*100)); bgLhsl.setValue(r1(hsl[2]*100));
            double[] hsb = c.toHsb();
            bgHhsb.setValue(r1(hsb[0])); bgShsb.setValue(r1(hsb[1]*100)); bgBhsb.setValue(r1(hsb[2]*100));
        } finally { updating = false; }
    }
    private void fire() {
        ColorModel effectiveFg = getEffectiveFgColor();
        changeListeners.forEach(l -> l.accept(effectiveFg, bgColor));
    }
    private void updateFgAlphaLabel() {
        int pct = (int) Math.round(fgAlpha / 255.0 * 100);
        fgAlphaLbl.setText(fgAlpha + " (" + pct + "%)");
    }
    // Helpers
    private static ColorModel fromRGB(JSpinner r, JSpinner g, JSpinner b) {
        return new ColorModel((int)r.getValue(), (int)g.getValue(), (int)b.getValue());
    }
    private static ColorModel fromHSL(JSpinner h, JSpinner s, JSpinner l) {
        return ColorModel.fromHsl(d(h), d(s)/100.0, d(l)/100.0);
    }
    private static ColorModel fromHSB(JSpinner h, JSpinner s, JSpinner b) {
        return ColorModel.fromHsb(d(h), d(s)/100.0, d(b)/100.0);
    }
    private static double d(JSpinner sp) { return ((Number)sp.getValue()).doubleValue(); }
    private static double r1(double v)   { return Math.round(v*10.0)/10.0; }
    private static int blendChannel(int fg, int bg, double alpha) {
        return (int) Math.round(fg * alpha + bg * (1.0 - alpha));
    }

    private static void addSpin(JPanel p, GridBagConstraints c,
                                JSpinner sp, String lbl, int col, int row) {
        c.gridy = row; c.gridx = col; c.gridwidth = 1;
        c.weightx = (col==3) ? 1.0 : 0.33;
        JPanel sub = new JPanel(new BorderLayout(2, 0));
        sub.setOpaque(false);
        sub.add(new JLabel(lbl+":"), BorderLayout.WEST);
        sub.add(sp, BorderLayout.CENTER);
        p.add(sub, c);
    }
    private static JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        return l;
    }
    private static void docListen(JTextField f, Runnable r) {
        f.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { r.run(); }
            public void removeUpdate(DocumentEvent e)  { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }
    private static void spListen(JSpinner sp, Runnable r) { sp.addChangeListener(e -> r.run()); }

    private static JSpinner intSp(int min, int max) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
        sp.setPreferredSize(new Dimension(52, 24));
        return sp;
    }
    private static JSpinner dblSp(double min, double max) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(min, min, max, 0.1));
        sp.setEditor(new JSpinner.NumberEditor(sp, "0.0"));
        sp.setPreferredSize(new Dimension(62, 24));
        return sp;
    }
}
