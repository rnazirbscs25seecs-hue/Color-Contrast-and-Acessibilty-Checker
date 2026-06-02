package com.colorchecker.engine;

import com.colorchecker.model.ColorModel;
import com.colorchecker.model.ContrastResult;

import java.awt.Color;
import static com.colorchecker.model.ColorModel.linearChannel;

/*Pure-function engine that implements the WCAG 2.1 contrast ratio algorithm.
         Algorithm summary
  Convert each 8-bit sRGB channel to a linear value by removing gamma
      (the "linearise" step).
         Compute relative luminance L :
              { L = 0.2126 R + 0.7152 G + 0.0722 B}.</li>
          Compute contrast ratio from the two luminance's:
       { ratio = (L_lighter + 0.05) / (L_darker + 0.05)}.*/

public final class ContrastEngine{
    private static final ContrastEngine instance = new ContrastEngine();
    private ContrastEngine() {}
    public static ContrastEngine getInstance() {
        return instance;
    }
    //Compute the WCAG relative luminance of an AWT Color.
    //Parameters sRGB color.
    //luminancein [0.0,1.0]where 0=black 1=white.
    public double relativeLuminance(Color color)
    {
        if (color == null) {
            throw new IllegalArgumentException("color cannot be null");
        }
        double rLin=linearChannel(color.getRed());
        double gLin=linearChannel(color.getGreen());
        double bLin=linearChannel(color.getBlue());
        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;

    }
     public double relativeLuminance(ColorModel c){
        return relativeLuminance(c.toAwtColor());
     }


    // Compute WCAG contrast ratio between two AWT colors.
    //ratio>=1.0 returns 21.0 for black vs white
    public double contrastRatio(Color fg, Color bg) {
        double l1 = relativeLuminance(fg);
        double l2 = relativeLuminance(bg);
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        return (lighter + 0.05) / (darker + 0.05);
    }
    public double contrastRatio(ColorModel fg,ColorModel bg)
    {
        return contrastRatio(fg.toAwtColor(),bg.toAwtColor());
    }

    // Evaluate contrast ratio and thresholds for ColorModel values.
    public ContrastResult evaluate(ColorModel fg, ColorModel bg) {
        if (fg == null || bg == null) {
            throw new IllegalArgumentException("fg and bg cannot be null");
        }
        double ratio = contrastRatio(fg, bg);
        return new ContrastResult(fg, bg, ratio);
    }

}
