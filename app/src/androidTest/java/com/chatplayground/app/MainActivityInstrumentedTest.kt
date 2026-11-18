//package com.chatplayground.app
//
//import android.webkit.WebView
//import androidx.test.core.app.ActivityScenario
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.chatplayground.app.utils.UrlProvider
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNotNull
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//class MainActivityInstrumentedTest {
//
//    @Test
//    fun testWebViewLoadsCorrectUrl() {
//        val scenario = ActivityScenario.launch(MainActivity::class.java)
//        scenario.onActivity { activity ->
//            val webViewField = activity.javaClass.getDeclaredField("webView")
//            webViewField.isAccessible = true
//            val webView = webViewField.get(activity) as WebView
//            assertNotNull("WebView should be initialized", webView)
//            val expectedUrl = UrlProvider.getChatPlaygroundUrl()
//            // WebView loads URL asynchronously; we can check the current URL if loaded
//            assertEquals(expectedUrl, webView.url)
//        }
//    }
//}
