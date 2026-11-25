package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pure-JVM tests for [DesktopModeHandler]: user-agent toggling
 * and viewport setting mapping.
 */
class DesktopModeHandlerTest {

    @Before
    fun setUp() {
        val handler = DesktopModeHandler
        handler.isDesktop = false
        handler.userAgent = "ChatPlayground/1.0"
        handler.useWideViewPort = false
        handler.loadWithOverviewMode = false
    }

    @Test
    fun testMobileMode() {
        val handler = DesktopModeHandler
        // Assert starting default state is mobile
        assertEquals("ChatPlayground/1.0", handler.userAgent)
        assertFalse("Mobile mode must not use wide viewport", handler.useWideViewPort)
        assertFalse("Mobile mode must not load with overview", handler.loadWithOverviewMode)
    }

    @Test
    fun testDesktopMode() {
        val handler = DesktopModeHandler
        handler.toggle() // enter desktop mode
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
        val handler = DesktopModeHandler
        assertFalse(handler.isDesktop)
        handler.toggle()
        assertTrue(handler.isDesktop)
    }

    @Test
    fun testToggleBackToMobileResetsAllFields() {
        val handler = DesktopModeHandler
        handler.toggle() // desktop
        assertTrue(handler.isDesktop)
        assertTrue(handler.useWideViewPort)
        assertTrue(handler.loadWithOverviewMode)
        handler.toggle() // back to mobile
        assertFalse(handler.isDesktop)
        assertEquals("ChatPlayground/1.0", handler.userAgent)
        assertFalse(handler.useWideViewPort)
        assertFalse(handler.loadWithOverviewMode)
    }
}
