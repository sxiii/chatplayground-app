package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [DesktopModeHandler]: user-agent toggling
 * and viewport setting mapping.
 */
class DesktopModeHandlerTest {

    @Test
    fun testMobileMode() {
        val handler = DesktopModeHandler(isDesktop = false)
        assertEquals("ChatPlayground/1.0", handler.userAgent)
        assertFalse("Mobile mode must not use wide viewport", handler.useWideViewPort)
        assertFalse("Mobile mode must not load with overview", handler.loadWithOverviewMode)
    }

    @Test
    fun testDesktopMode() {
        val handler = DesktopModeHandler(isDesktop = true)
        // Using a standard desktop-like User-Agent
        assertTrue(
            "Desktop mode should use a different user agent",
            handler.userAgent.contains("Chrome") && handler.userAgent.contains("Windows"),
        )
        assertTrue("Desktop mode must use wide viewport", handler.useWideViewPort)
        assertTrue("Desktop mode must load with overview", handler.loadWithOverviewMode)
    }

    @Test
    fun testToggle() {
        val handler = DesktopModeHandler(isDesktop = false)
        assertFalse(handler.isDesktop)
        handler.toggle()
        assertTrue(handler.isDesktop)
    }
}
