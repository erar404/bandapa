package com.bandapa.feature.band.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BandImagePicker(
    selectedBytes: ByteArray?,
    onImageSelected: (ByteArray?) -> Unit,
    modifier: Modifier = Modifier,
)
