package com.bandapa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KineticSoundColorScheme = darkColorScheme(
    primary         = ElectricPurple,
    onPrimary       = OnAccent,
    secondary       = NeonGreen,
    onSecondary     = OnAccent,
    tertiary        = ElectricCyan,
    onTertiary      = OnAccent,
    background      = Background,
    onBackground    = OnBackground,
    surface         = Surface,
    onSurface       = OnSurface,
    surfaceVariant  = SurfaceVariant,
    onSurfaceVariant = OnSurface,
    error           = ErrorRed,
    onError         = OnError,
)

@Composable
fun BandapaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KineticSoundColorScheme,
        typography  = BandapaTypography,
        shapes      = BandapaShapes,
        content     = content,
    )
}
