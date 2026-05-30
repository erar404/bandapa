package com.bandapa.feature.band.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bandapa.ui.theme.OnSurface
import com.bandapa.ui.theme.SurfaceVariant
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memcpy
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BandImagePicker(
    selectedBytes: ByteArray?,
    onImageSelected: (ByteArray?) -> Unit,
    modifier: Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .clickable { showPicker = true },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = Icons.Default.CameraAlt,
            contentDescription = "Pick band photo",
            tint               = OnSurface.copy(alpha = 0.45f),
            modifier           = Modifier.size(28.dp),
        )
    }

    if (showPicker) {
        ImagePickerController(
            onImageSelected = { bytes ->
                showPicker = false
                onImageSelected(bytes)
            },
            onDismiss = { showPicker = false },
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun ImagePickerController(
    onImageSelected: (ByteArray) -> Unit,
    onDismiss: () -> Unit,
) {
    val onImageSelectedRef = rememberUpdatedState(onImageSelected)
    val onDismissRef = rememberUpdatedState(onDismiss)

    DisposableEffect(Unit) {
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary
        picker.allowsEditing = false

        val delegate = object : platform.darwin.NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo info: Map<Any?, *>,
            ) {
                val image = info[UIImagePickerControllerOriginalImage] as? UIImage
                val jpeg  = image?.let { UIImageJPEGRepresentation(it, 0.8) }
                if (jpeg != null) {
                    val bytes = nsDataToByteArray(jpeg)
                    onImageSelectedRef.value(bytes)
                } else {
                    onDismissRef.value()
                }
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
                onDismissRef.value()
            }
        }
        picker.delegate = delegate

        @Suppress("DEPRECATION")
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(picker, animated = true, completion = null)

        onDispose {
            picker.dismissViewControllerAnimated(false, null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun nsDataToByteArray(data: NSData): ByteArray {
    val size = data.length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).also { array ->
        array.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
}
