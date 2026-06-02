package com.colorchecker.engine;
import com.colorchecker.model.ColorModel;
import com.colorchecker.model.BlindnessType;

import java.awt.Color;

import static com.colorchecker.model.ColorModel.linearChannel;
/*
  Simulates color vision deficiencies (CVD) using the scientifically validated
  LMS color-space pipeline recommended by Viénot, Brettel &amp; Mollon (1999).
 */
 /*
   Simulation pipeline (per pixel / per color)
   Convert sRGB [0,255] to linear RGB [0,1]  (remove gamma)
   Convert linear RGB to LMS
   Apply CVD projection matrix in LMS space
   Convert LMS to linear RGB
   Convert linear RGB to sRGB [0,255]  (re-apply gamma)
  */
/*
  Severity in [0,1] allows simulating anomalous trichromacy (partial deficit)
  by linearly interpolating between the original color and the fully simulated one.
 */
public class BlindnessSimulator {

    public static final BlindnessSimulator INSTANCE = new BlindnessSimulator();
    public BlindnessSimulator() {}
    public static BlindnessSimulator getInstance() {
        return INSTANCE;
    }
    //Hunt Pointer Estrevez sRGB to LMS matrix.
    private static final double[][] M_RGB_TO_LMS={
            { 0.31399022, 0.63951294, 0.04649755 },
            { 0.15537241, 0.75789446, 0.08670142 },
            { 0.01775239, 0.10944209, 0.87256922 }
    };
    //Inverse of M_RGB_TO_LMS (LMS TO Linear RGB).
    private static final double[][] M_LMS_TO_RGB = {
            {  5.47221206, -4.64196010,  0.16963448 },
            { -1.12524190,  2.29317094, -0.16789520 },
            {  0.02996825, -0.19318073,  1.16321248 }
    };
    //Deficiency matrix in LMS space.
    //Protanopia_L_cone missing .Row 0 replaced.
    private static final double[][] PROTANOPIA = {
            { 0.0,  1.05118294, -0.05116099 },
            { 0.0,  1.0,         0.0        },
            { 0.0,  0.0,         1.0        }
    };
    //Deuteranopia_M_cone missing.Row 1 replaced.
    private static final double[][] DEUTERANOPIA = {
            { 1.0,          0.0,  0.0        },
            { 0.9513092,    0.0,  0.04866992 },
            { 0.0,          0.0,  1.0        }
    };
    //Tritanopia_S_cone missing.Row 2 replaced.
    //this gives correct results for UI color pair comparison.
    private static final double[][] TRITANOPIA = {
            { 1.0,           0.0,         0.0 },
            { 0.0,           1.0,         0.0 },
            { -0.86744736,   1.86727089,  0.0 }
    };

    //Protanomaly(mild protanopia)_L_cone shifted
    //Interpolated at 0.6 severity toward protanopia.
    private static final double[][] PROTANOMALY = {
            { 0.458064,  0.679578, -0.137642 },
            { 0.092785,  0.846313,  0.060902 },
            { -0.007494, -0.016807, 1.024301 }
    };
    //Deutranomaly(mild deuteranopia)_M_cone shifted
    private static final double[][] DEUTERANOMALY = {
            { 0.547494,  0.607765, -0.155259 },
            { 0.181692,  0.781162,  0.037146 },
            { -0.010410,  0.027386,  0.983024 }
    };
    public ColorModel simulate(ColorModel color, BlindnessType type) {
        return simulate(color, type, 1.0);
    }
    public ColorModel simulate(ColorModel color,BlindnessType type,double severity)
    {
        if(type==BlindnessType.NORMAL || severity<=0.0) return color;
        Color simulated=applySimulation(color.toAwtColor(),type);
        if(severity>=1.0)
            return new ColorModel(simulated);
        // Interpolate between original and fully-simulated for anomalous trichromacy
        return new ColorModel(interpolate(color.toAwtColor(),simulated,severity));

    }
    //Simulate at full severity.
    private Color applySimulation(Color c,BlindnessType type)
    {
        //sRGB to linear RGB.
        double rLin = linearChannel(c.getRed());
        double gLin = linearChannel(c.getGreen());
        double bLin = linearChannel(c.getBlue());
        if (type == BlindnessType.ACHROMATOPSIA) {
            // Grayscale: luminance-weighted average (no LMS needed)
            double y = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
            int yInt = clamp((int) Math.round(y));
            return new Color(yInt, yInt, yInt);
        }
        //Linear RGB to LMS
        double[] lms = multiply3x3(M_RGB_TO_LMS, new double[]{rLin, gLin, bLin});
        //LMS to linear RGB
        double[][] cvdMatrix = selectMatrix(type);
        double[] rgbLinSim = multiply3x3(cvdMatrix, lms);
        //Linear RGB to sRGB[0,255]
        int rSim = clamp((int) Math.round(toSrgb(rgbLinSim[0]) * 255));
        int gSim = clamp((int) Math.round(toSrgb(rgbLinSim[1]) * 255));
        int bSim = clamp((int) Math.round(toSrgb(rgbLinSim[2]) * 255));
        return new Color(rSim, gSim, bSim);
    }
    private double[][] selectMatrix(BlindnessType type) {
        switch (type) {
            case PROTANOPIA:    return PROTANOPIA;
            case DEUTERANOPIA:  return DEUTERANOPIA;
            case TRITANOPIA:    return TRITANOPIA;
            case PROTANOMALY:   return PROTANOMALY;
            case DEUTERANOMALY: return DEUTERANOMALY;
            default:            return new double[][]{{ 1,0,0 },{ 0,1,0 },{ 0,0,1 }};
        }
    }
    //Multiply a 3*3 matrix by a 3 element column vector.
    private double[] multiply3x3(double[][] m, double[] v) {
        return new double[]{
                m[0][0]*v[0] + m[0][1]*v[1] + m[0][2]*v[2],
                m[1][0]*v[0] + m[1][1]*v[1] + m[1][2]*v[2],
                m[2][0]*v[0] + m[2][1]*v[1] + m[2][2]*v[2]
        };
    }
   //Apply sRGB gamma:channel[0,255] to sRGB[0,1]
    private double toSrgb(double lin)
    {
        lin = Math.max(0.0, Math.min(1.0, lin));
        return (lin <= 0.0031308) ? lin * 12.92 : 1.055 * Math.pow(lin, 1.0 / 2.4) - 0.055;
    }
    //Linearly interpolate between two Awt Colors.
    private Color interpolate(Color original, Color simulated, double t) {
        int r = clamp((int) Math.round(original.getRed()   * (1-t) + simulated.getRed()   * t));
        int g = clamp((int) Math.round(original.getGreen() * (1-t) + simulated.getGreen() * t));
        int b = clamp((int) Math.round(original.getBlue()  * (1-t) + simulated.getBlue()  * t));
        return new Color(r, g, b);
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
