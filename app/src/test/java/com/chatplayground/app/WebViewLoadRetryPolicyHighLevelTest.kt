package com.chatplayground.app

import android.os.Looper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebViewLoadRetryPolicyHighLevelTest {
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
        for (i in 1..maxRetries) {
            policy.recordRetry()
            assertEquals(i, policy.retryCount())
        }
        assertFalse(policy.shouldRetry(true))
        assertEquals(maxRetries, policy.retryCount())
    }

    @Test
    fun testNonMainFrameIsNeverRetried() {
        val policy = WebViewLoadRetryPolicy()
        policy.recordRetry()
        assertFalse(policy.shouldRetry(false))
        assertEquals(1, policy.retryCount())
        policy.reset()
        policy.recordRetry()
        assertEquals(1, policy.retryCount())
        val policyClean = WebViewLoadRetryPolicy()
        policyClean.recordRetry()
        policyClean.recordRetry()
        policyClean.reset()
        assertFalse(policyClean.shouldRetry(false))
    }

    @Test
    fun testResetResetsCounter() {
        val policy = WebViewLoadRetryPolicy(maxRetries = 3)
        policy.recordRetry()
        assertEquals(1, policy.retryCount())
        policy.reset()
        assertEquals(0, policy.retryCount())
        assertTrue(policy.shouldRetry(true))
    }

    @Test
    fun handlerIsBoundToMainLooper() {
        val policy = WebViewLoadRetryPolicy()
        assertEquals(Looper.getMainLooper(), policy.handlerLooper)
    }
}
