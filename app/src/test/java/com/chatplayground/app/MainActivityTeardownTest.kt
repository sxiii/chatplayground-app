package com.chatplayground.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Verifies that [MainActivity] releases its WebView when the activity is
 * torn down. An Android WebView does not die with its activity: unless
 * `destroy()` is called it keeps the activity context and its renderer
 * binding alive, so every configuration change or finish leaks one.
 *
 * SDK level comes from `app/src/test/resources/robolectric.properties` (26).
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityTeardownTest {
    @Test
    fun activityDestroysItsWebViewWhenTornDown() {
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .create().start().resume()
        val activity = controller.get()
        val webView = activity.getWebView()
        assertNotNull("WebView should be created in onCreate", webView)
        assertFalse(
            "Precondition: the WebView must not be destroyed while the activity is alive",
            shadowOf(webView).wasDestroyCalled(),
        )

        controller.pause().stop().destroy()

        assertTrue(
            "MainActivity must call WebView.destroy() in onDestroy, otherwise the " +
                "WebView outlives the activity and leaks its context",
            shadowOf(webView).wasDestroyCalled(),
        )
    }

    @Test
    fun activityDetachesWebViewFromItsParentWhenTornDown() {
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .create().start().resume()
        val activity = controller.get()
        val webView = activity.getWebView()
        assertNotNull(
            "Precondition: the WebView must be attached to the activity layout",
            webView.parent,
        )

        controller.pause().stop().destroy()

        assertNull(
            "MainActivity must detach the WebView from its parent in onDestroy: a WebView " +
                "destroyed while still attached is removed on the next layout pass, which " +
                "can touch an already-freed renderer",
            webView.parent,
        )
    }
}
