package com.chatplayground.app

import android.os.Handler
import android.os.Looper

/**
 * Bounded WebView load-failure retry policy.
 *
 * Retries only main-frame load failures (isMainFrame == true), capped at
 * [maxRetries] attempts with exponential backoff delivered through [Handler].
 * Sub-frame failures are never retried, preventing retry storms from
 * embedded iframes/ad networks.
 *
 * Thread-safe: [retryCount] is only mutated from the main thread via the
 * callback chain, but reads/writes are synchronized to be safe.
 */
class WebViewLoadRetryPolicy(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 1000
) {
    private var retryCount = 0
    private val lock = Any()
    val handlerLooper: Looper = Looper.getMainLooper()
    private val handler = Handler(Looper.getMainLooper())

    /** Number of retries already attempted. */
    fun retryCount(): Int {
        synchronized(lock) { return retryCount }
    }

    /**
     * Returns true if the load failure should be retried: only main-frame
     * failures and only while [retryCount] is below [maxRetries].
     */
    fun shouldRetry(isMainFrame: Boolean): Boolean {
        synchronized(lock) {
            if (!isMainFrame) return false
            return retryCount < maxRetries
        }
    }

    /**
     * Record that a retry was consumed. Call this before scheduling the
     * actual retry so the count advances even if the retry itself fails.
     */
    fun recordRetry() {
        synchronized(lock) { retryCount++ }
    }

    /** Reset the retry counter (e.g. after a successful load). */
    fun reset() {
        synchronized(lock) { retryCount = 0 }
    }

    /** Schedule [action] to run after [delayMs] on the main thread. */
    fun scheduleRetry(delayMs: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMs)
    }

    /** Schedule a retry with exponential backoff: baseDelayMs * 2^retryCount. */
    fun scheduleRetryWithBackoff(action: () -> Unit) {
        val delay = baseDelayMs * (1L shl retryCount())
        scheduleRetry(delay, action)
    }
}
