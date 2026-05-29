package com.bandapa.core.notifications

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

class IosNotificationService : NotificationService {
    override fun showNotification(title: String, body: String, imageUrl: String?) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
            completionHandler = { granted, _ ->
                if (!granted) return@requestAuthorizationWithOptions
                dispatch_async(dispatch_get_main_queue()) {
                    val content = UNMutableNotificationContent().apply {
                        setTitle(title)
                        setBody(body)
                    }
                    val request = UNNotificationRequest.requestWithIdentifier(
                        "bandapa-${System.currentTimeMillis()}",
                        content,
                        null,
                    )
                    center.addNotificationRequest(request) {}
                }
            },
        )
    }
}
