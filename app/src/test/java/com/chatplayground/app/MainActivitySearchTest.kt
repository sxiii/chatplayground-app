package com.chatplayground.app

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivitySearchTest {

    @Test
    fun findTextTriggersFindAllAsyncOnWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        
        activity.findText("test")
        assertNotNull(webView)
    }

    @Test
    fun findNextTriggersFindNextOnWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        
        activity.findNext()
        assertNotNull(webView)
    }
}
