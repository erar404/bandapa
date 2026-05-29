package com.bandapa.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bandapa.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

class AndroidNotificationService(private val context: Context) : NotificationService {

    private val idCounter = AtomicInteger(1000)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        try { createChannel() } catch (_: Exception) {}
    }

    private fun createChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Announcements",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Bandapa app announcements" }
        manager.createNotificationChannel(channel)
    }

    override fun showNotification(title: String, body: String, imageUrl: String?) {
        if (!hasPermission()) return
        scope.launch {
            val bitmap = imageUrl?.let { fetchBitmap(it) }
            postNotification(title, body, bitmap)
        }
    }

    private suspend fun fetchBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout    = 10_000
            conn.doInput        = true
            conn.connect()
            BitmapFactory.decodeStream(conn.inputStream)
        }.getOrNull()
    }

    private suspend fun postNotification(title: String, body: String, bitmap: Bitmap?) =
        withContext(Dispatchers.Main) {
            val style = if (bitmap != null) {
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?)
                    .setSummaryText(body)
            } else {
                NotificationCompat.BigTextStyle().bigText(body)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .apply { if (bitmap != null) setLargeIcon(bitmap) }
                .setStyle(style)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(idCounter.getAndIncrement(), notification)
        }

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    companion object {
        private const val CHANNEL_ID = "bandapa_announcements"
    }
}
