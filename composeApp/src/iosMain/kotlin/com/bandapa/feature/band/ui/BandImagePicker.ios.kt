package com.bandapa.feature.band.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.SurfaceVariant

@Composable
actual fun BandImagePicker(
    selectedBytes: ByteArray?,
    onImageSelected: (ByteArray?) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = Icons.Default.CameraAlt,
            contentDescription = "Pick band photo",
            tint               = OnSurface.copy(alpha = 0.45f),
            modifier           = Modifier.size(28.dp),
        )
    }
}
