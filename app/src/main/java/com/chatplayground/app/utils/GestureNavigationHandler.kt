package com.chatplayground.app.utils

import kotlin.math.abs

/**
 * Detects horizontal swipe gestures for WebView history navigation.
 *
 * Pure JVM (`kotlin.math` only) so the policy is plain-JUnit testable without
 * Robolectric, a MotionEvent or a live WebView. [onSwipeRight] maps to "back"
 * and [onSwipeLeft] maps to "forward", matching the direction a finger travels
 * when the page is dragged back to a previous document.
 */
class GestureNavigationHandler(
    private val onSwipeRight: () -> Unit,
    private val onSwipeLeft: () -> Unit,
    private val swipeThreshold: Int = DEFAULT_SWIPE_THRESHOLD,
    private val swipeVelocityThreshold: Int = DEFAULT_SWIPE_VELOCITY_THRESHOLD
) {

    /**
     * Evaluates one fling.
     *
     * Returns `true` only when the motion is a mostly-horizontal swipe that
     * clears both the distance and the velocity threshold, in which case the
     * matching callback has already been invoked. Returns `false` and invokes
     * nothing for taps, scrolls, and short or slow drags, so the WebView keeps
     * its normal touch behaviour.
     */
    fun handleFling(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val diffX = endX - startX
        val diffY = endY - startY

        val horizontal = abs(diffX) > abs(diffY)
        val travelled = abs(diffX) > swipeThreshold
        val fastEnough = abs(velocityX) > swipeVelocityThreshold
        if (!horizontal || !travelled || !fastEnough) {
            return false
        }

        if (diffX > 0) {
            onSwipeRight()
        } else {
            onSwipeLeft()
        }
        return true
    }

    companion object {
        const val DEFAULT_SWIPE_THRESHOLD: Int = 100
        const val DEFAULT_SWIPE_VELOCITY_THRESHOLD: Int = 200
    }
}
