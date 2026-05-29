package com.bandapa.core.notifications

interface NotificationService {
    fun showNotification(title: String, body: String, imageUrl: String? = null)
}
