package com.colorchecker.model;
//Enumeration of color vision deficiency (CVD) types.
/*
 Scientific basis for each entry:
 PROTANOPIA   – missing L-cones (red-sensitive). ~1 % of males.
 DEUTERANOPIA – missing M-cones (green-sensitive). ~1 % of males.
 TRITANOPIA   – missing S-cones (blue-sensitive). Very rare, ~0.001 %.
 ACHROMATOPSIA – complete monochromacy, no color perception. Extremely rare.
 NORMAL        – unaffected vision (identity transform, for comparison).
 */
public enum BlindnessType
{
    NORMAL       ("Normal vision",    "Full color perception"),
    PROTANOPIA   ("Protanopia",       "Red-blind  (L-cone absent)"),
    DEUTERANOPIA ("Deuteranopia",     "Green-blind (M-cone absent)"),
    TRITANOPIA   ("Tritanopia",       "Blue-blind  (S-cone absent)"),
    ACHROMATOPSIA("Achromatopsia",    "Total color-blindness (all cones)"),
    PROTANOMALY  ("Protanomaly",      "Red-weak  (L-cone anomalous)"),
    DEUTERANOMALY("Deuteranomaly",    "Green-weak (M-cone anomalous)");
    private final String displayName;
    private final String description;
    BlindnessType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
    }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    @Override
    public String toString() { return displayName; }
}


