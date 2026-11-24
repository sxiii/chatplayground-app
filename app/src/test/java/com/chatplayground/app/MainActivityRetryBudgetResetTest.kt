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
 *  - the budget was never restored after a page actually loaded, so a spent
 *    budget leaked into the next, unrelated navigation.
 *
 * `ERROR_UNKNOWN` is not one of the codes that route to the offline page, so
 * these cases drive the retry branch. The policy schedules its retries through
 * a main-looper `Handler`, which Robolectric leaves pending unless a test
 * idles the looper, so no retry body runs here.
 *
 * A navigation is modelled as the platform's real callback sequence:
 * `onPageStarted` → `onReceivedError` → `onPageFinished`. The platform sends
 * `onPageFinished` for failed navigations too, which is why the budget is only
 * restored when no error was reported for that attempt.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityRetryBudgetResetTest {
    private fun newActivity(): MainActivity = Robolectric.buildActivity(MainActivity::class.java).create().get()

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

    private fun startLoad(activity: MainActivity) {
        val webView = activity.getWebView()
        webView.webViewClient?.onPageStarted(webView, "https://chatplayground.ai", null)
    }

    private fun finishLoad(activity: MainActivity) {
        val webView = activity.getWebView()
        webView.webViewClient?.onPageFinished(webView, "https://chatplayground.ai")
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
    fun failedNavigationFinishesWithoutRestoringTheBudget() {
        val activity = newActivity()
        repeat(3) { failLoad(activity) }

        // The platform still calls onPageFinished after a failed load. That
        // must not be mistaken for a committed page.
        startLoad(activity)
        failLoad(activity)
        finishLoad(activity)

        assertEquals(
            "finishing a failed navigation must not restore the budget",
            3,
            activity.loadRetryPolicy.retryCountValue,
        )
        assertFalse(
            "a failed navigation must not re-enable retries",
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

        // A navigation that starts and finishes with no error is a commit.
        startLoad(activity)
        finishLoad(activity)

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
