package com.chatplayground.app

import android.webkit.WebViewClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Regression guard for the retry-budget contract at the `MainActivity` wiring
 * layer.
 *
 * `NavigationRetryPolicy.shouldRetry()` both decides *and* records a retry
 * attempt (see `NavigationRetryPolicyTest`). `MainActivity.onReceivedError`
 * used to call `recordRetry()` as well, so every failed load consumed two
 * budget units and doubled the exponential-backoff exponent — a
 * `maxRetries = 3` policy gave up after only two real retries.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityRetryBudgetTest {
    @Test
    fun oneNonNetworkErrorConsumesExactlyOneRetry() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        // ERROR_UNKNOWN is not one of the network-error codes that route to the
        // offline page, so this drives the retry branch of onReceivedError.
        webView.webViewClient?.onReceivedError(
            webView,
            WebViewClient.ERROR_UNKNOWN,
            "Load failed",
            "https://chatplayground.ai",
        )

        assertEquals(
            "One failed load must consume exactly one retry-budget unit",
            1,
            activity.navigationRetryPolicy.retryCountValue,
        )
    }
}
