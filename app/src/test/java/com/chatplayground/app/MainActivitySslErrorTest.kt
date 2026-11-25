package com.chatplayground.app

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebViewClient
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Asserts that the SSL error handling policy is secure: it must cancel the
 * request on any SSL error, never proceeding with an insecure connection.
 *
 * Runs on the suite-wide SDK from `app/src/test/resources/robolectric.properties`
 * (26).
 */
@RunWith(RobolectricTestRunner::class)
class MainActivitySslErrorTest {

    private fun sslError(): SslError {
        return mockk<SslError>(relaxed = true)
    }

    @Test
    fun sslErrorPolicyCancelsConnectionAndNeverProceeds() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val client = webView.webViewClient

        val handler = mockk<SslErrorHandler>(relaxed = true)
        client.onReceivedSslError(webView, handler, sslError())

        verify { handler.cancel() }
        verify(exactly = 0) { handler.proceed() }
    }
}
