package com.chatplayground.app.utils

data class PushNotification(val title: String, val body: String)

class NotificationService {
    private var currentToken: String? = null
    private val displayedNotifications = mutableListOf<PushNotification>()

    fun handleTokenRefresh(token: String) {
        currentToken = token
    }

    fun getCurrentToken(): String? {
        currentToken?.let {
            // Token refreshed and stored
        }
        return currentToken
    }

    fun displayNotification(
        title: String,
        body: String,
    ): PushNotification {
        val notification = PushNotification(title, body)
        displayedNotifications.add(notification)
        return notification
    }

    fun getDisplayedNotifications(): List<PushNotification> {
        return displayedNotifications
    }
}
