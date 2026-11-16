package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotificationServiceTest {

    @Test
    fun testHandleTokenRefresh() {
        val service = NotificationService()
        val token = "fcm_test_token_12345"
        service.handleTokenRefresh(token)
        assertEquals(token, service.getCurrentToken())
    }

    @Test
    fun testDisplayNotification() {
        val service = NotificationService()
        val title = "New Message"
        val body = "Hello from ChatPlayground!"
        
        val notification = service.displayNotification(title, body)
        assertNotNull(notification)
        assertEquals(title, notification.title)
        assertEquals(body, notification.body)
    }
}
