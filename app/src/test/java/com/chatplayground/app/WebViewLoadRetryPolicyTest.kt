package com.chatplayground.app

import android.os.Handler
import android.os.Looper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class WebViewLoadRetryPolicyTest {

    @Test
    fun shouldRetryReturnsTrueForMainFrameWhenUnderMaxRetries() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)

        assertTrue("Main-frame request should be retried on first failure", policy.shouldRetry(true))
        assertTrue("Main-frame request should be retried on second failure", policy.shouldRetry(true))
        assertTrue("Main-frame request should be retried on third failure", policy.shouldRetry(true))
    }

    @Test
    fun shouldRetryReturnsFalseForNonMainFrame() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)

        assertFalse("Sub-frame failures should never be retried", policy.shouldRetry(false))
    }

    @Test
    fun shouldRetryReturnsFalseWhenMaxRetriesExceeded() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 2)

        assertTrue("First failure should be retried", policy.shouldRetry(true))
        assertTrue("Second failure should be retried", policy.shouldRetry(true))
        assertFalse("Third failure should exceed max retries and not be retried", policy.shouldRetry(true))
    }

    @Test
    fun resetSetsRetryCountBackToZero() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)

        policy.shouldRetry(true) // count = 1
        policy.shouldRetry(true) // count = 2
        assertEquals(2, policy.retryCount)

        policy.reset()
        assertEquals(0, policy.retryCount)
        assertTrue("After reset, main-frame failures should be retried again", policy.shouldRetry(true))
    }

    @Test
    fun handlerIsBoundToMainLooper() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)

        assertEquals(Looper.getMainLooper(), policy.handlerLooper)
    }
}
