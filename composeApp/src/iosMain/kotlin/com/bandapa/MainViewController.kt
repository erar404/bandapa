package com.bandapa

import androidx.compose.ui.window.ComposeUIViewController
import com.bandapa.core.di.appModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(appModules())
        }
    },
) {
    App()
}
