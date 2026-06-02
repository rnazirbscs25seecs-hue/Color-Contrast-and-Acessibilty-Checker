package com.colorchecker.engine;
import com.colorchecker.model.ColorModel;
import com.colorchecker.model.ContrastResult;
import java.util.ArrayList;
import java.util.List;
/*
Generates accessible color suggestions given a fixed color (seed) and a
target role (foreground or background).*/
/*Strategy
 Walk the lightness axis in 1 % steps from the seed color.
 Collect candidates that pass WCAG AA normal-text threshold (4.5:1) against
 the partner color.
 Return the closest AA pass, the closest AAA pass, and complementary
 harmony shades.
 */

public class PaletteGenerator {
    private static final PaletteGenerator instance = new PaletteGenerator();
    private final ContrastEngine engine = ContrastEngine.getInstance();

    public static PaletteGenerator getInstance() {
        return instance;
    }

    public PaletteGenerator() {
    }

    //Auto fix
    //Find the nearest lightness adjustment of  color so that it passes
    //WCAG AA (4.5:1) against code partner.
    public ColorModel nearestAaFix(ColorModel color, ColorModel partner) {
        double current = engine.contrastRatio(color, partner);
        if (current >= ContrastResult.AA_NORMAL_THRESHOLD)
            return color;
        double[] hsl = color.toHsl();
        double h = hsl[0], s = hsl[1], l = hsl[2];
        // Try lightening and darkening simultaneously; return whichever first hits AA.
        for (double delta = 0.01; delta <= 1.0; delta += 0.01) {
            ColorModel lighter = ColorModel.fromHsl(h, s, Math.min(1.0, l + delta));
            if (engine.contrastRatio(lighter, partner) >= ContrastResult.AA_NORMAL_THRESHOLD) {
                return lighter;
            }
            ColorModel darker = ColorModel.fromHsl(h, s, Math.max(0.0, l - delta));
            if (engine.contrastRatio(darker, partner) >= ContrastResult.AA_NORMAL_THRESHOLD) {
                return darker;
            }
        }
        // Fallback: black or white, whichever has more contrast
        ColorModel black = new ColorModel(0, 0, 0);
        ColorModel white = new ColorModel(255, 255, 255);
        return engine.contrastRatio(black, partner) >= engine.contrastRatio(white, partner)
                ? black : white;
    }

    public ColorModel nearestAaaFix(ColorModel color, ColorModel partner) {
        double[] hsl = color.toHsl();
        double h = hsl[0], s = hsl[1], l = hsl[2];

        for (double delta = 0.0; delta <= 1.0; delta += 0.01) {
            ColorModel lighter = ColorModel.fromHsl(h, s, Math.min(1.0, l + delta));
            if (engine.contrastRatio(lighter, partner) >= ContrastResult.AAA_NORMAL_THRESHOLD) {
                return lighter;
            }
            ColorModel darker = ColorModel.fromHsl(h, s, Math.max(0.0, l - delta));
            if (engine.contrastRatio(darker, partner) >= ContrastResult.AAA_NORMAL_THRESHOLD) {
                return darker;
            }
        }
        ColorModel black = new ColorModel(0, 0, 0);
        ColorModel white = new ColorModel(255, 255, 255);
        return engine.contrastRatio(black, partner) >= engine.contrastRatio(white, partner)
                ? black : white;
    }
    //Harmony generation
    // Generate a palette of color shades from black to white along the lightness
    //axis, preserving the hue and saturation of the seed color
    /*
      Generate a palette of color shades from black to white along the lightness
     axis, preserving the hue and saturation of the seed color.
     */
    // seed :base color to derive from
    // partner  color to measure contrast against
    // steps:    number of swatches to generate (typically 9 or 11)
    //list of (color, contrastRatioVsPartner) pairs as PaletteEntry objects
    public List<PaletteEntry> generateShades(ColorModel seed, ColorModel partner, int steps) {
        double[] hsl = seed.toHsl();
        double h = hsl[0], s = hsl[1];
        List<PaletteEntry> entries = new ArrayList<>();

        for (int i = 0; i < steps; i++) {
            double l = (double) i / (steps - 1); // 0.0 to 1.0
            ColorModel shade =ColorModel.fromHsl(h, s, l);
            double ratio = engine.contrastRatio(shade, partner);
            entries.add(new PaletteEntry(shade, ratio));
        }
        return entries;
    }
    /*
     Generate complementary, triadic, and analogous colors from a seed hue,
     each adjusted to pass WCAG AA against .
     seed    the base hue donor
     partner the color to contrast against
     up to 6 harmonious, accessible color suggestions
     */
    public List<PaletteEntry> generateHarmony(ColorModel seed,ColorModel partner) {
        double[] hsl = seed.toHsl();
        double h = hsl[0], s = Math.max(0.4, hsl[1]);
        double l = hsl[2];

        double[] hues = {
                h,                          // original
                (h + 30)  % 360,            // analogous +30
                (h - 30 + 360) % 360,       // analogous -30
                (h + 180) % 360,            // complementary
                (h + 120) % 360,            // triadic 1
                (h + 240) % 360,            // triadic 2
        };
        List<PaletteEntry> results = new ArrayList<>();
        for (double hue : hues) {
            ColorModel candidate = ColorModel.fromHsl(hue, s, l);
            ColorModel fixed = nearestAaFix(candidate, partner);
            double ratio = engine.contrastRatio(fixed, partner);
            results.add(new PaletteEntry(fixed, ratio));
        }
        return results;
    }
    // Inner record
    // A color suggestion together with its computed contrast ratio vs. partner.
    public static final class PaletteEntry {
        private final ColorModel color;
        private final double  ratio;

        public PaletteEntry(ColorModel color, double ratio) {
            this.color = color;
            this.ratio = ratio;
        }

        public ColorModel getColor() { return color; }
        public double   getRatio() { return ratio; }

        public boolean passesAA()  { return ratio >= ContrastResult.AA_NORMAL_THRESHOLD; }
        public boolean passesAAA() { return ratio >= ContrastResult.AAA_NORMAL_THRESHOLD; }
        public String getBadge() {
            if (passesAAA()) return "AAA";
            if (passesAA())  return "AA";
            return "—";
        }
    }
}