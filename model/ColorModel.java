package com.colorchecker.model;

import java.awt.Color;
import java.util.Objects;

//Immutable colormodel that performs conversion between
//HEX,RGB and HSL representations.
public class ColorModel {
    private final Color color;
    private final int r, g, b;

    public ColorModel(Color color) {
        Objects.requireNonNull(color, "color cannot be null");
        this.color = color;
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
    }

    public ColorModel(int r, int g, int b) {
        this(new Color(clamp(r), clamp(g), clamp(b)));
    }

    //Parse a six_digit HEX string such as "#1A2B3C"
    public static ColorModel fromHex(String hex) {
        if (hex == null || hex.isBlank())
            return new ColorModel(Color.BLACK);
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        if (clean.length() != 6)
            return new ColorModel(Color.BLACK);
        try {
            int ri = Integer.parseInt(clean.substring(0, 2), 16);
            int gi = Integer.parseInt(clean.substring(2, 4), 16);
            int bi = Integer.parseInt(clean.substring(4, 6), 16);
            return new ColorModel(ri, gi, bi);
        } catch (NumberFormatException e) {
            return new ColorModel(Color.BLACK);
        }
    }

    // Construct from HSL values: h in [0,360], s and l in [0,1].
    public static ColorModel fromHsl(double h, double s, double l) {
        h = ((h % 360.0) + 360.0) % 360.0;
        s = clamp01(s);
        l = clamp01(l);
        //HSL to RGB conversion
        double c = (1.0 - Math.abs(2.0 * l - 1.0)) * s;
        double x = c * (1.0 - Math.abs((h / 60.0) % 2.0 - 1.0));
        double m = l - c / 2.0;
        double r1, g1, b1;
        if (h < 60) {
            r1 = c;
            g1 = x;
            b1 = 0;
        } else if (h < 120) {
            r1 = x;
            g1 = c;
            b1 = 0;
        } else if (h < 180) {
            r1 = 0;
            g1 = c;
            b1 = x;
        } else if (h < 240) {
            r1 = 0;
            g1 = x;
            b1 = c;
        } else if (h < 300) {
            r1 = x;
            g1 = 0;
            b1 = c;
        } else {
            r1 = c;
            g1 = 0;
            b1 = x;
        }
        return new ColorModel((int) Math.round((r1 + m) * 255),
                (int) Math.round((g1 + m) * 255),
                (int) Math.round((b1 + m) * 255)
        );
    }

    public Color toAwtColor() {
        return color;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public String toHex() {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    //Return HSL representation in a three element array.
    //index 0=hue[0,360],index 1=saturation[0,1],index 2=lighteness[0,1].
    public double[] toHsl() {
        double r1 = r / 255.0;
        double g1 = g / 255.0;
        double b1 = b / 255.0;
        double max = Math.max(r1, Math.max(g1, b1));
        double min = Math.min(r1, Math.min(g1, b1));
        double delta = max - min;

        double l = (max + min) / 2.0;
        double s = 0;
        if (delta != 0) {
            s = delta / (1.0 - Math.abs(2.0 * l - 1.0));
        }
        double h = 0;
        if (delta != 0) {
            if (max == r1) h = 60.0 * (((g1 - b1) / delta) % 6);
            else if (max == g1) h = 60.0 * ((b1 - r1) / delta + 2);
            else h = 60.0 * ((r1 - g1) / delta + 4);
        }
        if (h < 0) h += 360;
        return new double[]{h, s, l};
    }

    //Produce a lighter version of this color by increasing lightness n HSL.
    public ColorModel lighter(double amount) {
        double[] hsl = toHsl();
        return fromHsl(hsl[0], hsl[1], Math.min(1.0, hsl[2] + amount));
    }

    //Produce a darker version of this color by decreasing lightness n HSL.
    public ColorModel darker(double amount) {
        double[] hsl = toHsl();
        return fromHsl(hsl[0], hsl[1], Math.max(0.0, hsl[2] - amount));
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorModel)) return false;
        ColorModel that = (ColorModel) o;
        return r == that.r && g == that.g && b == that.b;
    }

    @Override
    public int hashCode() { return Objects.hash(r, g, b); }

    @Override
    public String toString() { return toHex(); }
    /*
      Returns HSB (Hue-Saturation-Brightness) as a 3-element array:
      index 0 = hue [0,360], index 1 = saturation [0,1], index 2 = brightness [0,1].
      Uses the same algorithm as {@link Color#RGBtoHSB}.
     */
    public double[] toHsb() {
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        return new double[]{ hsb[0] * 360.0, hsb[1], hsb[2] };
    }

    /* Construct from HSB values: h in [0,360), s and b in [0,1]. */
    public static ColorModel fromHsb(double h, double s, double brightness) {
        double normalizedHue = ((h % 360.0) + 360.0) % 360.0;
        int rgb = Color.HSBtoRGB((float) (normalizedHue / 360.0), (float) clamp01(s), (float) clamp01(brightness));
        return new ColorModel((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    /*
     * Returns the linearised (gamma-removed) CIE XYZ coordinates [X, Y, Z]
     * computed from the sRGB D65 matrix. Used for chromaticity diagram plotting.
     * Values are in [0, ~1] with Y = relative luminance.
     */
    public double[] toXyz() {
        double rLin = linearChannel(r);
        double gLin = linearChannel(g);
        double bLin = linearChannel(b);
        // sRGB → CIE XYZ (D65) matrix
        double x = 0.4124564 * rLin + 0.3575761 * gLin + 0.1804375 * bLin;
        double y = 0.2126729 * rLin + 0.7151522 * gLin + 0.0721750 * bLin;
        double z = 0.0193339 * rLin + 0.1191920 * gLin + 0.9503041 * bLin;
        return new double[]{ x, y, z };
    }

    /*
      Returns CIE xy chromaticity coordinates (not to be confused with XYZ).
      x = X/(X+Y+Z),  y = Y/(X+Y+Z).
      Returns {0.333, 0.333} (white point) if XYZ sum is near zero.
     */
    public double[] toChromaticityXy() {
        double[] xyz = toXyz();
        double sum = xyz[0] + xyz[1] + xyz[2];
        if (sum <= 1e-9 || Double.isNaN(sum) || Double.isInfinite(sum)) {
            // Pure black has undefined chromaticity; fall back to D65 so UI code can still render it safely.
            return new double[]{0.3127, 0.3290};
        }

        double x = xyz[0] / sum;
        double y = xyz[1] / sum;
        return new double[]{x, y};
    }
    //Remove sRGB gamma from a single 8 bit channel [0,255] to produce
    //linear light value in [0.0,1.0]
    //c_sRGB=channel/255
    //c_lin=c_sRRGB/12.92
    // if c_sRGB ≤ 0.04045
    //       *          = ((c_sRGB + 0.055) / 1.055) ^ 2.4

   public static double linearChannel(int channel) {
        double c = channel / 255.0;
        return (c <= 0.04045) ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
