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

import com.chatplayground.app.WebViewLoadRetryPolicy
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WebViewLoadRetryPolicyTest {

    @Test
    fun testInitialState() {
        val policy = WebViewLoadRetryPolicy()
        assertEquals(0, policy.retryCount())
        assertTrue(policy.shouldRetry(true)) // Should retry initially
    }

    @Test
    fun testRetryLimit() {
        val maxRetries = 3
        val policy = WebViewLoadRetryPolicy(maxRetries = maxRetries)

        // Simulate 3 failures
        for (i in 1..maxRetries) {
            policy.recordRetry()
            assertEquals(i, policy.retryCount())
        }

        // After maxRetries, it should not retry
        assertFalse(policy.shouldRetry(true))
        assertEquals(maxRetries, policy.retryCount())
    }

    @Test
    fun testNonMainFrameIsNeverRetried() {
        val policy = WebViewLoadRetryPolicy()
        // Non-main frame failures should never trigger retry count increment
        policy.recordRetry()
        assertFalse(policy.shouldRetry(false))
        assertEquals(1, policy.retryCount()) // Note: The initial recordRetry() still happens, but shouldRetry(false) is false.

        // If we manually reset count for isolated test logic
        policy.reset()
        policy.recordRetry() // Should still increment
        assertEquals(1, policy.retryCount())

        // Force a clean state check:
        val policyClean = WebViewLoadRetryPolicy()
        policyClean.recordRetry() // Count becomes 1
        // If we test non-main frame:
        policyClean.recordRetry() // Count should still increment if logic is sequential
        // Since the logic checks isMainFrame FIRST, the retryCount should only advance when shouldRetry(true) is called.
        // Let's refine the logic check: shouldRetry(false) must be false regardless of count.
        policyClean.reset()
        assertFalse(policyClean.shouldRetry(false))
    }

    @Test
    fun testResetResetsCounter() {
        val policy = WebViewLoadRetryPolicy()
        policy.recordRetry()
        assertEquals(1, policy.retryCount())
        policy.reset()
        assertEquals(0, policy.retryCount())
        assertTrue(policy.shouldRetry(true))
    }
}
