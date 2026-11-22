package com.chatplayground.app.utils

import com.chatplayground.app.BuildConfig
import com.chatplayground.app.MainActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Pins the WebView remote-debugging policy: `chrome://inspect` must attach to
 * debug builds only, never to a release build.
 *
 * Robolectric's `ShadowWebView` implements `setWebContentsDebuggingEnabled` as a
 * no-op with no readable state, so the wrapper's injectable seam
 * (`WebViewDebugging.applyDebuggingEnabled`) records the decision instead of the
 * test inspecting device state.
 */
@RunWith(RobolectricTestRunner::class)
class WebViewDebuggingTest {
    private val recorded = mutableListOf<Boolean>()
    private val original = WebViewDebugging.applyDebuggingEnabled

    @Before
    fun recordCalls() {
        WebViewDebugging.applyDebuggingEnabled = { recorded.add(it) }
    }

    @After
    fun restoreSeam() {
        WebViewDebugging.applyDebuggingEnabled = original
    }

    @Test
    fun debugBuildEnablesWebContentsDebugging() {
        WebViewDebugging.configure(debugBuild = true)

        assertEquals(
            "Debug builds must expose chrome://inspect",
            listOf(true),
            recorded,
        )
    }

    @Test
    fun releaseBuildNeverEnablesWebContentsDebugging() {
        WebViewDebugging.configure(debugBuild = false)

        assertTrue(
            "Release builds must never expose the WebView renderer to adb",
            recorded.isEmpty(),
        )
    }

    @Test
    fun mainActivityEnablesDebuggingForDebugBuild() {
        // The unit tests run in the debug build type, so BuildConfig.DEBUG is true.
        assertTrue("Precondition: tests must run against a debug build", BuildConfig.DEBUG)

        Robolectric.buildActivity(MainActivity::class.java).create().get()

        assertEquals(
            "MainActivity must enable WebView debugging from BuildConfig.DEBUG",
            listOf(true),
            recorded,
        )
    }
}
