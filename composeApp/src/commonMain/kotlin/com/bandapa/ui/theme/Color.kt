package com.bandapa.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Kinetic Sound — Surface scale (midnight green) ──────────────────────────
val Background               = Color(0xFF0f1509)   // surface / deepest base
val SurfaceContainerLowest   = Color(0xFF091005)
val SurfaceContainerLow      = Color(0xFF171e11)   // default card/container fill
val Surface                  = Color(0xFF1b2215)   // surface-container
val SurfaceContainerHigh     = Color(0xFF252c1f)
val SurfaceVariant           = Color(0xFF303729)   // surface-container-highest
val SurfaceBright            = Color(0xFF343b2d)

// ─── Text / icon on-surface ───────────────────────────────────────────────────
val OnSurface                = Color(0xFFe1e3d9)   // primary text
val OnBackground             = Color(0xFFe1e3d9)
val OnSurfaceVariant         = Color(0xFFc2c9b7)   // secondary/muted text
val Outline                  = Color(0xFF8c9383)   // dividers, borders
val OutlineVariant           = Color(0xFF42493c)   // subtle borders

// ─── Primary — Electric Lime ──────────────────────────────────────────────────
val ElectricLime             = Color(0xFF6ee304)   // primary accent (CTA, active)
val ElectricPurple           = ElectricLime        // legacy alias — all UI refs unchanged
val OnPrimary                = Color(0xFF083900)   // dark green text on lime
val OnAccent                 = OnPrimary           // legacy alias
val PrimaryContainer         = Color(0xFF115300)
val OnPrimaryContainer       = Color(0xFF89ff4c)

// ─── Secondary — Sage ────────────────────────────────────────────────────────
val Secondary                = Color(0xFFbccbb0)
val OnSecondary              = Color(0xFF273421)
val SecondaryContainer       = Color(0xFF3d4b36)
val OnSecondaryContainer     = Color(0xFFd8e7cb)

// ─── Tertiary — Muted Teal ───────────────────────────────────────────────────
val ElectricCyan             = Color(0xFFa0cfd2)   // tertiary (band events, info)
val OnTertiary               = Color(0xFF00373a)
val TertiaryContainer        = Color(0xFF1e4e51)
val OnTertiaryContainer      = Color(0xFFbbecef)

// ─── Accent used for personal events ─────────────────────────────────────────
val NeonGreen                = OnPrimaryContainer  // #89ff4c — personal event accent

// ─── Error ────────────────────────────────────────────────────────────────────
val ErrorRed                 = Color(0xFFffb4ab)   // error / destructive text
val ErrorContainer           = Color(0xFF93000a)   // error background
val OnError                  = Color(0xFF690005)
val OnErrorContainer         = Color(0xFFffdad6)
