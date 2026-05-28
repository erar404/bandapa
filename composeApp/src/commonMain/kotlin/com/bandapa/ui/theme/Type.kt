package com.bandapa.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Kinetic Sound typeface: Sora (geometric sans-serif, weight 400/700)
// To activate: add sora_regular.ttf + sora_bold.ttf to
//   composeApp/src/commonMain/composeResources/font/
// then create a FontFamily here and assign to each TextStyle.
// Until loaded, the system's default sans-serif fills in gracefully.

val BandapaTypography = Typography(
    // ─── Display ─────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontWeight    = FontWeight.Bold,     // Sora 700
        fontSize      = 57.sp,
        lineHeight    = (57 * 1.2).sp,      // 68.4 sp
        letterSpacing = (-0.25).sp,
    ),

    // ─── Headlines ───────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        lineHeight = (32 * 1.2).sp,         // 38.4 sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        lineHeight = (28 * 1.2).sp,         // 33.6 sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,
        lineHeight = (24 * 1.2).sp,         // 28.8 sp
    ),

    // ─── Titles ───────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = (22 * 1.2).sp,         // 26.4 sp
    ),
    titleMedium = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = (16 * 1.5).sp,      // 24 sp
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = (14 * 1.5).sp,      // 21 sp
        letterSpacing = 0.1.sp,
    ),

    // ─── Body (1.5 leading per spec) ─────────────────────────────────────────
    bodyLarge = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = (16 * 1.5).sp,      // 24 sp
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = (14 * 1.5).sp,      // 21 sp
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = (12 * 1.5).sp,      // 18 sp
        letterSpacing = 0.4.sp,
    ),

    // ─── Labels ───────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
