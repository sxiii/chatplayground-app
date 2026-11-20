package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlProviderTest {
    @Test
    fun testGetChatPlaygroundUrl() {
        val expected = "https://chatplayground.ai"
        val actual = UrlProvider.getChatPlaygroundUrl()
        assertEquals(expected, actual)
    }
}
