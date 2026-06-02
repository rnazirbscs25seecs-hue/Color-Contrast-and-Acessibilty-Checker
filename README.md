# Color-Contrast-and-Acessibilty-Checker
A Java Swing desktop application that evaluates color pairs against WCAG 2.1 accessibility standards, simulates color vision deficiencies, and generates accessible palette suggestions  all with live visual feedback




<img width="1278" height="503" alt="image" src="https://github.com/user-attachments/assets/3c5c8d8a-8d01-430b-94be-96cc65f3fe36" />

<img width="507" height="462" alt="image" src="https://github.com/user-attachments/assets/d12bb232-5b63-4072-b4fd-5057f4aecadf" />

<img width="1425" height="421" alt="image" src="https://github.com/user-attachments/assets/82d8a131-89a9-4541-be4a-2ffa82768312" />

<img width="1914" height="453" alt="image" src="https://github.com/user-attachments/assets/a11b6a93-59f9-4858-a762-064e8e512ca7" />

<img width="960" height="455" alt="image" src="https://github.com/user-attachments/assets/73978f91-a363-48ed-a058-1c1c42f1971b" />

<img width="930" height="433" alt="image" src="https://github.com/user-attachments/assets/7b18f274-97fb-4416-bb49-a1d749fbbd64" />

<img width="1664" height="457" alt="image" src="https://github.com/user-attachments/assets/4397ea36-d52f-421a-8708-2eb746d2ef46" />

<img width="1907" height="984" alt="image" src="https://github.com/user-attachments/assets/19d84da3-d00a-44e0-aa54-dfde09e3798f" />



Features
1.WCAG 2.1 contrast engine — computes relative luminance and contrast ratio using the ITU-R BT.709 formula; evaluates AA / AAA for normal text, large text, and UI components
2.Multi-format color input — HEX, RGB, HSL, HSB spinners fully bidirectionally synced; alpha transparency slider for foreground
3.Live preview — renders sample text at all WCAG-relevant sizes (14 px normal, 24 px large, 18 px bold) plus button and input field mockups
4.Color blindness simulation — 7 modes using the Viénot 1999 / Brettel 1997 LMS pipeline: Protanopia, Deuteranopia, Tritanopia, Achromatopsia, Protanomaly, Deuteranomaly, Normal; severity slider for anomalous trichromacy
Analysis tab — CIE 1931 xy chromaticity diagram with sRGB gamut triangle + FG/BG dots; WCAG compliance bar chart across all CVD modes
5.Palette generator — nearest AA/AAA auto-fix, 11-step lightness shade strip, 6-color harmony explorer (complementary, triadic, analogous)
6.History — timestamped session log; double-click any row to restore
7.Export — PNG report card (600 × 340 px) and CSV with all CVD simulation data
8.Keyboard shortcuts — Ctrl+C copy ratio · Ctrl+S PNG · Ctrl+E CSV · Ctrl+Shift+S swap · Ctrl+H clear history
9.Light / dark theme toggle — Nimbus (built-in) with cross-platform Metal dark fallback


Project Structure
color-contrast-checker/
├── pom.xml
└── src/main/java/com/colorchecker/
    ├── Main.java
    ├── model/
    │   ├── AppColor.java          # Immutable color with HEX/RGB/HSL/HSB/XYZ conversions
    │   ├── ContrastResult.java    # WCAG evaluation result value object
    │   └── BlindnessType.java     # Enum of 7 CVD simulation modes
    ├── engine/
    │   ├── ContrastEngine.java    # WCAG luminance + ratio (singleton)
    │   ├── BlindnessSimulator.java# LMS matrix pipeline (singleton)
    │   └── PaletteGenerator.java  # Auto-fix, shades, harmony (singleton)
    ├── ui/
    │   ├── MainFrame.java         # Root window + reactive update chain
    │   ├── components/
    │   │   ├── RatioGauge.java    # Custom arc gauge (log scale)
    │   │   ├── WcagBadge.java     # Pass/fail pill badge
    │   │   └── ColorSwatch.java   # Clickable color preview → JColorChooser
    │   └── panels/
    │       ├── ColorInputPanel.java
    │       ├── ResultPanel.java
    │       ├── PreviewPanel.java
    │       ├── BlindnessPanel.java
    │       ├── AnalysisPanel.java
    │       ├── ChromaticityPanel.java
    │       ├── ComparisonChartPanel.java
    │       ├── PalettePanel.java
    │       └── HistoryPanel.java
    └── util/
        └── ExportUtil.java        # PNG + CSV export
WCAG 2.1 Thresholds
CriterionAAAAANormal text (< 18 pt / 14 pt bold)≥ 4.5 : 1≥ 7.0 : 1Large text (≥ 18 pt / 14 pt bold)≥ 3.0 : 1≥ 4.5 : 1UI components & graphics≥ 3.0 : 1—

Color Blindness Modes
ModeCone       affected                  Population
Protanopia     L-cone absent (red-blind)  ~1 % males
Deuteranopia   M-cone absent (green-blind)  ~1 % males
Tritanopia     S-cone absent (blue-blind)  ~0.001 %
Achromatopsia  All cones absent           Extremely rare
Protanomaly    L-cone weakened            ~1 % males
Deuteranomaly   M-cone weakened           ~5 % males
Simulation uses the Viénot 1999 LMS matrices for protanopia and deuteranopia, and the Brettel 1997 approach for tritanopia. Severity slider linearly interpolates between normal and full dichromacy.
Architecture
The project follows a strict MVC separation:

Model — ColorModel, ContrastResult, BlindnessType (data only, no logic, no UI)
Engine — ContrastEngine, BlindnessSimulator, PaletteGenerator (pure math, no UI, singletons)
UI — all panels in com.colorchecker.ui (rendering only, no business logic)

The reactive chain is driven by a single BiConsumer<AppColor, AppColor> observer registered in MainFrame. Every color change fires one call to onColorChanged() which fans out to all panels simultaneously.

Keyboard Shortcuts
ShortcutAction 
Ctrl + C Copy ratio + level + hex pair to clipboard
Ctrl + S  Export PNG report
Ctrl + E   Export CSV (includes all 7 CVD simulation rows)
Ctrl + Shift + S Swap foreground ↔ background
Ctrl + H Clear history


References

WCAG 2.1 Success Criterion 1.4.3
Viénot, Brettel, Mollon (1999) — Digital video colourmaps for dichromats
Brettel, Viénot, Mollon (1997) — Computerised simulation of colour appearance for dichromats
ITU-R BT.709 — relative luminance coefficient
