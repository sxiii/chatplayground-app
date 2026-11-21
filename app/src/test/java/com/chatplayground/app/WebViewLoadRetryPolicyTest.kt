package com.chatplayground.app

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

        assertTrue(policy.shouldRetry(true))
        assertTrue(policy.shouldRetry(true))
        assertTrue(policy.shouldRetry(true))
    }

    @Test
    fun shouldRetryReturnsFalseForNonMainFrame() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)
        assertFalse(policy.shouldRetry(false))
    }

    @Test
    fun shouldRetryReturnsFalseWhenMaxRetriesExceeded() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 2)

        assertTrue(policy.shouldRetry(true))
        assertTrue(policy.shouldRetry(true))
        assertFalse(policy.shouldRetry(true))
    }

    @Test
    fun resetSetsRetryCountBackToZero() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)

        policy.shouldRetry(true)
        policy.shouldRetry(true)
        assertEquals(2, policy.retryCountValue)

        policy.reset()
        assertEquals(0, policy.retryCountValue)
        assertTrue(policy.shouldRetry(true))
    }

    @Test
    fun handlerIsBoundToMainLooper() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)
        assertEquals(android.os.Looper.getMainLooper(), policy.handlerLooper)
    }
}
