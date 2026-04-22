You are building an Android fitness app called Kinetic using Jetpack Compose.
The design language is called "Kinetic Precision" — a premium, dark, HUD-style UI
that feels like a performance dashboard for the human body.

─────────────────────────────────────────
DESIGN NORTH STAR: "The Digital Athlete"
─────────────────────────────────────────
The UI should feel like a head-up display (HUD) — data-dense but never cluttered.
High-voltage accents on a deep obsidian base. Editorial asymmetry.
Think premium athletic gear meets aerospace instrumentation.

─────────────────────────────────────────
COLOR TOKENS  (define as Compose Color objects)
─────────────────────────────────────────
background                = #0E0E0E
surface                   = #0E0E0E
surface-container-lowest  = #000000
surface-container-low     = #131313
surface-container         = #1A1A1A
surface-container-high    = #20201F
surface-container-highest = #262626
surface-bright            = #2C2C2C
outline-variant           = #484847   // use at 20% opacity max — "ghost border" only

primary                   = #F3FFCA   // lime glow
primary-container         = #CAFD00   // high-voltage lime
primary-dim               = #8FB300
on-primary-fixed          = #3A4A00
secondary                 = #00E3FD   // neon cyan — progress arcs, glows
tertiary                  = #FF6F7C   // coral red — high-intensity zones
on-surface-variant        = #ADAAAA   // units, labels

─────────────────────────────────────────
TYPOGRAPHY  (use Google Fonts via Compose)
─────────────────────────────────────────
Display / Headlines  → Lexend
display-lg : 56sp / weight 700  (step counts, caloric burns — massive, unapologetic)
display-md : 44sp / weight 600

Titles / Body        → Plus Jakarta Sans
title-md   : 18sp / weight 600  (workout names)
body-md    : 14sp / weight 400  (instructional copy)
body-sm    : 12sp / weight 400

Labels / Units       → Space Grotesk
label-md   : 12sp / weight 500  (BPM, KM/H, KG — data unit suffix)
label-sm   : 10sp / weight 500

─────────────────────────────────────────
SURFACE & ELEVATION RULES
─────────────────────────────────────────
NO traditional drop shadows. NO 1px solid borders for section dividers.
Use tonal layering only:
- Nest surface-container-highest cards inside surface-container-low sections.
- The contrast in background value IS the "elevation."

Glassmorphism for nav bars and floating overlays:
- backdrop blur: 20–30px equivalent (RenderEffect blur in Compose)
- container background: surface-container at 70% alpha

FAB / floating glow shadow:
- Color: secondary (#00E3FD) at 12% alpha, blur 24dp, spread -4dp

─────────────────────────────────────────
COMPONENT RULES
─────────────────────────────────────────
Buttons
Primary   → background: primary-container (#CAFD00), text: on-primary-fixed (#3A4A00),
corner radius: 6dp, subtle inner gradient for 3D tactile feel
Secondary → surface-container-highest + 10% secondary tint, glass style
Tertiary  → label-md text in primary color, underline accent, no container

Data Cards
No divider lines — ever. Separate metrics with spacing (12dp gap) or a tonal
shift (surface-container → surface-container-high). Use secondary for progress
arcs, tertiary for high-intensity zone indicators.

Inputs
Focus state: background shifts to surface-bright (#2C2C2C); label color
animates to primary. No thick focus border.

Progress Bars ("Kinetic Bar")
Gradient fill: primary-dim → primary. Trailing edge: 4dp blur to simulate motion.

─────────────────────────────────────────
LAYOUT PRINCIPLES
─────────────────────────────────────────
- Outer page margin: 56dp (the "breathing room" token)
- Place large display-lg metrics OFF-CENTER intentionally — creates kinetic tension.
- Overlap transparent line charts over surface-container cards for depth.
- Always suffix units (kg, bpm, km/h) in Space Grotesk + on-surface-variant (#ADAAAA).
- Gradients for CTAs/hero data: primary (#F3FFCA) → primary-container (#CAFD00).

─────────────────────────────────────────
STRICT PROHIBITIONS
─────────────────────────────────────────
✗ No pure white or 1px solid borders
✗ No standard drop shadows (muddy on dark themes)
✗ No 100% opaque overlay backgrounds
✗ No centered, symmetric hero data layouts (embrace asymmetry)
✗ No tight margins — always preserve breathing room

─────────────────────────────────────────
TECH STACK
─────────────────────────────────────────
- Jetpack Compose (Material 3 as base, override with custom tokens above)
- define a KineticTheme.kt with all color/type tokens as CompositionLocals
- Target API 26+ for RenderEffect blur support
- Use Coil for image loading, Vico or MPAndroidChart for data visualizations