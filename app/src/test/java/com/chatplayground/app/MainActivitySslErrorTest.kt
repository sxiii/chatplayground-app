package com.chatplayground.app

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebViewClient
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

    /** A recording [SslErrorHandler] double that remembers which action was taken. */
    private class RecordingSslErrorHandler : SslErrorHandler() {
        var proceedCalled = false
        var cancelCalled = false

        override fun proceed() {
            proceedCalled = true
        }

        override fun cancel() {
            cancelCalled = true
        }
    }

    private fun sslError(code: Int = SslError.SSL_DATE_INVALID): SslError {
        val cert = java.security.cert.CertificateFactory.getInstance("X.509")
            .generateCertificate(java.io.ByteArrayInputStream(
                byteArrayOf(48, 13, 2, 1, 1, 2, 1, 0),
            ))
        return SslError(
            code,
            cert as java.security.cert.X509Certificate,
            "https://chatplayground.ai",
        )
    }

    @Test
    fun sslErrorPolicyCancelsConnectionAndNeverProceeds() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val client = webView.webViewClient

        val handler = RecordingSslErrorHandler()
        client.onReceivedSslError(webView, handler, sslError())

        assertTrue(
            "SSL error policy must cancel the connection on any SSL error",
            handler.cancelCalled,
        )
        assertFalse(
            "SSL error policy must NEVER proceed with an insecure connection",
            handler.proceedCalled,
        )
    }
}
