package com.bandapa.feature.band.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bandapa.ui.theme.ElectricPurple
import com.bandapa.ui.theme.OnAccent
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.SurfaceVariant

@Composable
actual fun BandImagePicker(
    selectedBytes: ByteArray?,
    onImageSelected: (ByteArray?) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            onImageSelected(bytes)
        }
    }

    val hasImage = selectedBytes != null

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (hasImage) ElectricPurple.copy(alpha = 0.18f) else SurfaceVariant)
            .clickable {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (hasImage) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint               = ElectricPurple,
                    modifier           = Modifier.size(28.dp),
                )
                Text(
                    text      = "Photo ready",
                    style     = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color     = ElectricPurple,
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.CameraAlt,
                    contentDescription = "Pick band photo",
                    tint               = OnSurface.copy(alpha = 0.45f),
                    modifier           = Modifier.size(28.dp),
                )
                Text(
                    text  = "Add photo",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurface.copy(alpha = 0.45f),
                )
            }
        }
    }
}
