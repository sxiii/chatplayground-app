package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureNavigationHandlerTest {
    private var backCount = 0
    private var forwardCount = 0

    private fun handler(
        threshold: Int = 100,
        velocityThreshold: Int = 100,
    ) = GestureNavigationHandler(
        onSwipeRight = { backCount++ },
        onSwipeLeft = { forwardCount++ },
        swipeThreshold = threshold,
        swipeVelocityThreshold = velocityThreshold,
    )

    @Test
    fun rightwardSwipeTriggersBackNavigation() {
        val handled =
            handler().handleFling(
                startX = 0f,
                startY = 0f,
                endX = 400f,
                endY = 10f,
                velocityX = 800f,
                velocityY = 20f,
            )

        assertTrue(handled)
        assertEquals(1, backCount)
        assertEquals(0, forwardCount)
    }

    @Test
    fun leftwardSwipeTriggersForwardNavigation() {
        val handled =
            handler().handleFling(
                startX = 400f,
                startY = 0f,
                endX = 0f,
                endY = 10f,
                velocityX = -800f,
                velocityY = 20f,
            )

        assertTrue(handled)
        assertEquals(0, backCount)
        assertEquals(1, forwardCount)
    }

    @Test
    fun horizontalMovementBelowDistanceThresholdIsNotASwipe() {
        val handled =
            handler().handleFling(
                startX = 0f,
                startY = 0f,
                endX = 50f,
                endY = 0f,
                velocityX = 800f,
                velocityY = 0f,
            )

        assertFalse(handled)
        assertEquals(0, backCount)
        assertEquals(0, forwardCount)
    }

    @Test
    fun horizontalMovementBelowVelocityThresholdIsNotASwipe() {
        val handled =
            handler().handleFling(
                startX = 0f,
                startY = 0f,
                endX = 400f,
                endY = 0f,
                velocityX = 20f,
                velocityY = 0f,
            )

        assertFalse(handled)
        assertEquals(0, backCount)
        assertEquals(0, forwardCount)
    }

    @Test
    fun verticalMovementDominatingHorizontalIsNotAHorizontalSwipe() {
        val handled =
            handler().handleFling(
                startX = 0f,
                startY = 0f,
                endX = 150f,
                endY = 900f,
                velocityX = 800f,
                velocityY = 1200f,
            )

        assertFalse(handled)
        assertEquals(0, backCount)
        assertEquals(0, forwardCount)
    }

    @Test
    fun movementBelowConfiguredThresholdsIsNotASwipe() {
        val handled =
            handler(threshold = 1000, velocityThreshold = 1000).handleFling(
                startX = 0f,
                startY = 0f,
                endX = 400f,
                endY = 0f,
                velocityX = 800f,
                velocityY = 0f,
            )

        assertFalse(handled)
        assertEquals(0, backCount)
        assertEquals(0, forwardCount)
    }
}
