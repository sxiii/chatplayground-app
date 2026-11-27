package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-JVM regression suite for [NavigationRetryPolicy].
 *
 * No Robolectric runner, no @Config, no Android shadows: this class must stay
 * runnable on a plain JVM so the retry-budget rules can be verified without
 * an Android SDK. If any Android import creeps in, the build will fail here.
 */
class NavigationRetryPolicyTest {
    @Test
    fun shouldRetryReturnsTrueForMainFrameWhenUnderMaxRetries() {
        val policy = NavigationRetryPolicy(maxRetries = 3)

        assertTrue(policy.shouldRetry(true))
        assertTrue(policy.shouldRetry(true))
        assertTrue(policy.shouldRetry(true))
    }

    @Test
    fun shouldRetryReturnsFalseForNonMainFrame() {
        val policy = NavigationRetryPolicy(maxRetries = 3)
        assertFalse(policy.shouldRetry(false))
    }

    @Test
    fun shouldRetryReturnsFalseWhenMaxRetriesExceeded() {
        val policy = NavigationRetryPolicy(maxRetries = 2)

        assertTrue(policy.shouldRetry(true))
        assertTrue(policy.shouldRetry(true))
        assertFalse(policy.shouldRetry(true))
    }

    @Test
    fun resetSetsRetryCountBackToZero() {
        val policy = NavigationRetryPolicy(maxRetries = 3)

        policy.shouldRetry(true)
        policy.shouldRetry(true)
        assertEquals(2, policy.retryCountValue)

        policy.reset()
        assertEquals(0, policy.retryCountValue)
        assertTrue(policy.shouldRetry(true))
    }

    @Test
    fun recordRetryIncrementsCountWithoutConsumingBudget() {
        val policy = NavigationRetryPolicy(maxRetries = 2)

        policy.recordRetry()
        assertEquals(1, policy.retryCountValue)

        // A manually recorded retry still counts toward the budget.
        assertTrue(policy.shouldRetry(true))
        assertEquals(2, policy.retryCountValue)
        assertFalse(policy.shouldRetry(true))
    }

    @Test
    fun backoffDelayMsGrowsExponentially() {
        val policy = NavigationRetryPolicy(maxRetries = 3, baseDelayMs = 1000)

        policy.recordRetry() // count 1 -> delay 1000 * 2^1 = 2000
        assertEquals(2000, policy.backoffDelayMs())

        policy.recordRetry() // count 2 -> delay 1000 * 2^2 = 4000
        assertEquals(4000, policy.backoffDelayMs())

        policy.reset() // count 0 -> delay 1000 * 2^0 = 1000
        assertEquals(1000, policy.backoffDelayMs())
    }

    @Test
    fun backoffDelayMsIsCappedAtMaxDelay() {
        val policy = NavigationRetryPolicy(maxRetries = 10, baseDelayMs = 1000, maxDelayMs = 5000)

        repeat(5) { policy.recordRetry() } // 1000 * 2^5 = 32000, exceeds cap
        assertEquals(5000, policy.backoffDelayMs())
    }

    @Test
    fun backoffDelayMsDoesNotOverflowAtLargeRetryCount() {
        val policy = NavigationRetryPolicy(maxRetries = 100, baseDelayMs = 1000, maxDelayMs = 60000)

        repeat(100) { policy.recordRetry() } // 2^100 would overflow a Long
        // Must clamp to the positive cap, never wrap to a negative value.
        assertEquals(60000, policy.backoffDelayMs())
        assertTrue(policy.backoffDelayMs() > 0)
    }
}
