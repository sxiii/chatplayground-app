package com.chatplayground.app

import android.webkit.WebViewClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Regression guard for the retry budget's lifetime.
 *
 * `WebViewLoadRetryPolicy` is documented as a *bounded* policy ("capped at
 * maxRetries") whose `reset()` runs "e.g. after a successful load". Neither
 * half held at the [MainActivity] wiring layer:
 *
 *  - the give-up branch of `handleWebViewError` called `reset()`, re-arming
 *    the budget, so a permanently failing page retried forever instead of
 *    `maxRetries` times;
 *  - `onPageFinished` never reset, so a spent budget was inherited by the
 *    next, unrelated navigation — a single bad network window disabled
 *    retries for the rest of the activity's life.
 *
 * `ERROR_UNKNOWN` is not one of the codes that route to the offline page, so
 * these cases drive the retry branch. The policy schedules its retries through
 * a main-looper `Handler`, which Robolectric leaves pending unless a test
 * idles the looper, so no retry body runs here.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityRetryBudgetResetTest {
    private fun newActivity(): MainActivity =
        Robolectric.buildActivity(MainActivity::class.java).create().get()

    /** Feeds one non-network load failure into the activity's retry branch. */
    private fun failLoad(activity: MainActivity) {
        val webView = activity.getWebView()
        webView.webViewClient?.onReceivedError(
            webView,
            WebViewClient.ERROR_UNKNOWN,
            "Load failed",
            "https://chatplayground.ai",
        )
    }

    @Test
    fun spentBudgetStaysSpentUntilAPageCommits() {
        val activity = newActivity()

        // maxRetries = 3: the first three failures each consume one unit.
        repeat(3) { failLoad(activity) }
        assertEquals(
            "three failures must consume the whole budget",
            3,
            activity.loadRetryPolicy.retryCountValue,
        )

        // Give-up must not re-arm the policy: an unbounded retry loop against a
        // permanently failing page is exactly what maxRetries exists to stop.
        repeat(2) { failLoad(activity) }

        assertEquals(
            "further failures must not restart the budget",
            3,
            activity.loadRetryPolicy.retryCountValue,
        )
        assertFalse(
            "an exhausted policy must keep refusing retries",
            activity.loadRetryPolicy.shouldRetry(true),
        )
    }

    @Test
    fun committedPageLoadRestoresTheRetryBudget() {
        val activity = newActivity()
        repeat(3) { failLoad(activity) }
        assertFalse(
            "the budget must be exhausted before the reset is observed",
            activity.loadRetryPolicy.shouldRetry(true),
        )

        val webView = activity.getWebView()
        webView.webViewClient?.onPageFinished(webView, "https://chatplayground.ai")

        assertEquals(
            "a committed page load must restore the retry budget",
            0,
            activity.loadRetryPolicy.retryCountValue,
        )
        assertTrue(
            "a later failure must be retryable again once a page has loaded",
            activity.loadRetryPolicy.shouldRetry(true),
        )
    }
}