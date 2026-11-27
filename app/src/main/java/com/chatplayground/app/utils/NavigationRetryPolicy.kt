package com.chatplayground.app.utils

/**
 * Pure-JVM retry budget for WebView load failures.
 *
 * No Android dependencies: no Handler, no Looper, no Android types.
 * The retry count is stored internally and advanced only by [recordRetry].
 * Sub-frame failures never retry, preventing retry storms from embedded iframes/ad networks.
 */
class NavigationRetryPolicy(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000,
    private val maxDelayMs: Long = 60_000,
) {
    private var retryCount = 0
    private val lock = Any()

    /** Number of retries already attempted. */
    val retryCountValue: Int
        get() = synchronized(lock) { retryCount }

    /** Number of retries already attempted. */
    fun retryCount(): Int = retryCountValue

    /** Returns true if the load failure should be retried: only main-frame
     * failures and only while [retryCount] is below [maxRetries]. */
    fun shouldRetry(isMainFrame: Boolean): Boolean {
        synchronized(lock) {
            if (!isMainFrame) return false
            if (retryCount < maxRetries) {
                retryCount++
                return true
            }
            return false
        }
    }

    /** Record that a retry was consumed. Call this before scheduling the
     * actual retry so the count advances even if the retry itself fails. */
    fun recordRetry() {
        synchronized(lock) { retryCount++ }
    }

    /** Reset the retry counter (e.g. after a successful load). */
    fun reset() {
        synchronized(lock) { retryCount = 0 }
    }

    /** Exponential backoff delay for the next retry: baseDelayMs * 2^retryCount, clamped to
     * [maxDelayMs]. The exponent is bounded so a very large retry count cannot overflow a Long. */
    fun backoffDelayMs(): Long {
        synchronized(lock) {
            val exponent = retryCount.coerceIn(0, MAX_SAFE_EXPONENT)
            val multiplier = 1L shl exponent
            if (baseDelayMs > 0 && multiplier > Long.MAX_VALUE / baseDelayMs) {
                return maxDelayMs
            }
            return (baseDelayMs * multiplier).coerceAtMost(maxDelayMs)
        }
    }

    private companion object {
        /** 2^62 is the largest power of two that still fits in a positive Long. */
        const val MAX_SAFE_EXPONENT = 62
    }
}
