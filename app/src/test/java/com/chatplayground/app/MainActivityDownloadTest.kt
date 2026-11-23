package com.chatplayground.app

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class MainActivityDownloadTest {
    @Test
    fun activitySetsDownloadListenerOnWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        // This will fail because it is not yet implemented (TDD - RED)
        assertNotNull("DownloadListener should be set", shadowOf(webView).downloadListener)
    }
}
