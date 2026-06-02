package com.colorchecker.util;

import com.colorchecker.model.ColorModel;
import com.colorchecker.model.ContrastResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/*
  Utility class that renders a contrast report as a BufferedImage
  and optionally saves it to disk via a save dialog.
 */
public class ExportUtil {
    private ExportUtil() {}
    //Show a file save dialog,then write a PNG report for the given result.
    //parent:parent component for the dialog
    //result:the contrast evaluation to export.
    public static void exportAsPng(Component parent,ContrastResult result) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save contrast report as PNG");
        String filename = "contrast_report" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".png";
        chooser.setSelectedFile(new File(filename));
        int choice = chooser.showSaveDialog(parent);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
        }
        BufferedImage image = renderReport(result);
        try {
            ImageIO.write(image, "PNG", file);
            JOptionPane.showMessageDialog(parent,
                    "Report saved to:\n" + file.getAbsolutePath(),
                    "Export successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Could not save file:\n" + e.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /*
      Show a file-save dialog, then write a CSV report for the given result
      including all colour-vision-deficiency simulated ratios.
     */
    public static void exportAsCsv(Component parent,ContrastResult result) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save contrast report as CSV");
        String filename = "contrast_report_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".csv";
        chooser.setSelectedFile(new File(filename));
        int choice=chooser.showSaveDialog(parent);
        if(choice!=JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if(!file.getName().toLowerCase().endsWith(".csv")) {
            file=new File(file.getAbsolutePath() + ".csv");
        }
        try(java.io.PrintWriter pw=new java.io.PrintWriter(file,"UTF-8")){
            pw.println("# Color Contrast Report — generated " + LocalDateTime.now());
            pw.println("Foreground,Background,Contrast Ratio,Level,AA Normal,AA Large,AA UI,AAA Normal,AAA Large");

            ColorModel fg = result.getFg();
            ColorModel bg = result.getBg();
            pw.printf("%s,%s,%.4f,%s,%s,%s,%s,%s,%s%n",
                    fg.toHex(), bg.toHex(),
                    result.getRatio(), result.getLevel(),
                    pass(result.passesAA_Normal()), pass(result.passesAA_Large()),
                    pass(result.passesAA_UI()),
                    pass(result.passesAAA_Normal()), pass(result.passesAAA_Large()));

            pw.println();
            pw.println("# Simulated ratios per colour-vision-deficiency mode");
            pw.println("CVD Mode,Simulated FG,Simulated BG,Contrast Ratio,Level");

            com.colorchecker.engine.BlindnessSimulator sim =
                    com.colorchecker.engine.BlindnessSimulator.getInstance();
            com.colorchecker.engine.ContrastEngine eng =
                    com.colorchecker.engine.ContrastEngine.getInstance();

            for (com.colorchecker.model.BlindnessType t :
                    com.colorchecker.model.BlindnessType.values()) {
                ColorModel sFg = sim.simulate(fg, t);
                ColorModel sBg = sim.simulate(bg, t);
                double   r   = eng.contrastRatio(sFg, sBg);
                ContrastResult sr = new ContrastResult(sFg, sBg, r);
                pw.printf("%s,%s,%s,%.4f,%s%n",
                        t.getDisplayName(), sFg.toHex(), sBg.toHex(), r, sr.getLevel());
            }
            JOptionPane.showMessageDialog(parent,
                    "CSV saved to:\n" + file.getAbsolutePath(),
                    "Export successful", JOptionPane.INFORMATION_MESSAGE);

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Could not save CSV:\n" + e.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private static String pass(boolean v) { return v ? "PASS" : "FAIL"; }
    /*
      Render a self-contained PNG report card (600 × 340 px) with:
     FG and BG color blocks with hex labels
     Contrast ratio displayed prominently
     WCAG AA / AAA pass-fail table
     Sample text preview
     */
    public static BufferedImage renderReport(ContrastResult result) {
        int W=600,H=340;
        BufferedImage image = new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
        Graphics2D G=image.createGraphics();
        G.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
         ColorModel fg = result.getFg();
         ColorModel bg = result.getBg();
        // Background
        G.setColor(new Color(0xF8F8F8));
        G.fillRect(0, 0, W, H);
        //Title bar
        G.setColor(new Color(0x2B2B2B));
        G.fillRect(0, 0, W, 48);
        G.setColor(Color.WHITE);
        G.setFont(new Font("SansSerif", Font.BOLD, 16));
        G.drawString("Color Contrast Report", 20, 30);
        G.setFont(new Font("SansSerif", Font.PLAIN, 12));
        G.drawString("WCAG 2.1", W - 90, 30);
        //FG SWATCH
        G.setColor(bg.toAwtColor());
        G.fillRoundRect(20, 64, 170, 90, 12, 12);
        G.setColor(fg.toAwtColor());
        G.fillRoundRect(36, 80, 140, 60, 8, 8);
        drawLabeledHex(G, fg.toHex(), 20, 162, 170, new Color(0x444444));
        G.setFont(new Font("SansSerif", Font.BOLD, 10));
        G.setColor(new Color(0x666666));
        G.drawString("FOREGROUND", 20, 177);

        // BG swatch
        G.setColor(bg.toAwtColor());
        G.fillRoundRect(210, 64, 170, 90, 12, 12);
        drawBorder(G, 210, 64, 170, 90);
        drawLabeledHex(G, bg.toHex(), 210, 162, 170, new Color(0x444444));
        G.setFont(new Font("SansSerif", Font.BOLD, 10));
        G.setColor(new Color(0x666666));
        G.drawString("BACKGROUND", 210, 177);

        // Contrast ratio
        G.setFont(new Font("SansSerif", Font.BOLD, 42));
        G.setColor(new Color(0x1A1A1A));
        String ratioText = result.getFormattedRatio();
        G.drawString(ratioText, 410, 120);
        G.setFont(new Font("SansSerif", Font.PLAIN, 13));
        G.setColor(new Color(0x666666));
        G.drawString("Contrast Ratio", 410, 140);

        // Level badge
        Color levelColor = levelColor(result);
        G.setColor(levelColor);
        G.fillRoundRect(410, 150, 120, 28, 8, 8);
        G.setColor(Color.WHITE);
        G.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawCentred(G, result.getLevel(), 410, 150, 120, 28);

        // WCAG table
        int ty = 200;
        G.setColor(new Color(0xE0E0E0));
        G.fillRect(20, ty, W - 40, 1);
        drawWcagTable(G, result, 20, ty + 12);

        // Preview text strip
        G.setColor(bg.toAwtColor());
        G.fillRoundRect(20, 290, W - 40, 36, 8, 8);
        drawBorder(G, 20, 290, W - 40, 36);
        G.setColor(fg.toAwtColor());
        G.setFont(new Font("SansSerif", Font.PLAIN, 14));
        G.drawString("The quick brown fox jumps over the lazy dog.", 30, 313);

        G.dispose();
        return image;
    }
    private static void drawLabeledHex(Graphics2D g, String hex, int x, int y, int w, Color c) {
        g.setColor(c);
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (w - fm.stringWidth(hex)) / 2;
        g.drawString(hex, textX, y);
    }

    private static void drawBorder(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(0xCCCCCC));
        g.drawRoundRect(x, y, w, h, 12, 12);
    }

    private static void drawCentred(Graphics2D g, String text, int x, int y, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, tx, ty);
    }

    private static void drawWcagTable(Graphics2D g, ContrastResult r, int x, int y) {
        String[][] rows = {
                { "Normal text (< 18pt)",  r.passesAA_Normal() ? "✓ Pass" : "✗ Fail", r.passesAAA_Normal() ? "✓ Pass" : "✗ Fail" },
                { "Large text (≥ 18pt)",   r.passesAA_Large()  ? "✓ Pass" : "✗ Fail", r.passesAAA_Large()  ? "✓ Pass" : "✗ Fail" },
                { "UI components / icons", r.passesAA_UI()     ? "✓ Pass" : "✗ Fail", "—" },
        };
        // Header
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(0x999999));
        g.drawString("WCAG CRITERION", x, y + 12);
        g.drawString("AA",  x + 280, y + 12);
        g.drawString("AAA", x + 360, y + 12);

        for (int i = 0; i < rows.length; i++) {
            int ry = y + 28 + i * 22;
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(new Color(0x333333));
            g.drawString(rows[i][0], x, ry);

            boolean aaPass  = rows[i][1].startsWith("✓");
            boolean aaaPass = rows[i][2].startsWith("✓");

            g.setFont(new Font("Sans Serif", Font.BOLD, 12));
            g.setColor(aaPass  ? new Color(0x2E7D32) : new Color(0xC62828));
            g.drawString(rows[i][1], x + 270, ry);
            if (!rows[i][2].equals("—")) {
                g.setColor(aaaPass ? new Color(0x2E7D32) : new Color(0xC62828));
                g.drawString(rows[i][2], x + 350, ry);
            } else {
                g.setColor(new Color(0xAAAAAA));
                g.drawString(rows[i][2], x + 350, ry);
            }
        }
    }

    private static Color levelColor(ContrastResult r) {
        if (r.passesAAA_Normal()) return new Color(0x1B5E20);
        if (r.passesAA_Normal())  return new Color(0x2E7D32);
        if (r.passesAA_Large())   return new Color(0xF57F17);
        return new Color(0xB71C1C);
    }
}
