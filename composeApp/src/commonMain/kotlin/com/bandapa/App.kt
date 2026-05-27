package com.bandapa

import androidx.compose.runtime.Composable
import com.bandapa.navigation.BandapaNavHost
import com.bandapa.ui.theme.BandapaTheme

@Composable
fun App() {
    BandapaTheme {
        BandapaNavHost()
    }
}
