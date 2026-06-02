package com.colorchecker.model;
//Immutable result of WCAG contrast evaluation between two colors.
/* WCAG 2.1 thresholds encoded here:
 *   Level AA – normal text:    ≥ 4.5 : 1</li>
 *   Level AA – large text:     ≥ 3.0 : 1  (≥18pt regular or ≥14pt bold)</li>
 *   Level AA – UI components:  ≥ 3.0 : 1</li>
 *   Level AAA – normal text:   ≥ 7.0 : 1</li>
 *   Level AAA – large text:    ≥ 4.5 : 1</li>
 */
public final class ContrastResult {
    //WCAG 2:1 threshold
    private final ColorModel fg;
    private final ColorModel bg;
    private final double ratio;
    public static final double AA_NORMAL_THRESHOLD=4.5,
            AA_LARGE_THRESHOLD=3.0,
            AA_UI_THRESHOLD=3.0,
            AAA_NORMAL_THRESHOLD=7.0,
            AAA_LARGE_THRESHOLD=4.5;

    public ContrastResult(ColorModel fg, ColorModel bg, double ratio) {
        this.fg = fg;
        this.bg = bg;
        this.ratio = ratio;
    }
    //WCAG pass/fail
    public boolean passesAA_Normal()  { return ratio >= AA_NORMAL_THRESHOLD;  }
    public boolean passesAA_Large()   { return ratio >= AA_LARGE_THRESHOLD;   }
    public boolean passesAA_UI()      { return ratio >= AA_UI_THRESHOLD;      }
    public boolean passesAAA_Normal() { return ratio >= AAA_NORMAL_THRESHOLD; }
    public boolean passesAAA_Large()  { return ratio >= AAA_LARGE_THRESHOLD;  }
    //Gives pass//fail.
    public String getLevel()
    {
        if (passesAAA_Normal()) return "AAA";
        if (passesAA_Normal())  return "AA";
        if (passesAA_Large())   return "AA (large / UI only)";
        return "Fails";
    }
    public ColorModel getFg() { return fg; }
    public ColorModel getBg() { return bg; }
    public double getRatio() { return ratio; }
    public String getFormattedRatio() {
        return String.format("%.2f:1", ratio);
    }

    @Override
    public String toString()
    {
        return String.format("ContrastResult{fg=%s, bg=%s, ratio=%.2f, level=%s}",
                fg, bg, ratio, getLevel());
    }
}
