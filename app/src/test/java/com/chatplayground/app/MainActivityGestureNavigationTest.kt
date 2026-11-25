package com.chatplayground.app

import android.view.MotionEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Asserts the activity actually routes horizontal flings into the WebView
 * history stack: a real MotionEvent stream goes through
 * [MainActivity.dispatchTouchEvent], hits the [android.view.GestureDetector]
 * and ends in `WebView.goBack()` / `WebView.goForward()`.
 */
@RunWith(RobolectricTestRunner::class)
class MainActivityGestureNavigationTest {
    private fun activityWithHistory(): Pair<MainActivity, android.webkit.WebView> {
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .create()
                .start()
                .resume()
        val activity = controller.get()
        val webView = activity.getWebView()
        val shadowWebView = shadowOf(webView)
        shadowWebView.pushEntryToHistory("https://chatplayground.ai/")
        shadowWebView.pushEntryToHistory("https://chatplayground.ai/pricing")
        return activity to webView
    }

    private fun fling(
        activity: MainActivity,
        startX: Float,
        endX: Float,
    ) {
        val downTime = 1_000L
        val down =
            MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, startX, 10f, 0)
        val move =
            MotionEvent.obtain(downTime, downTime + 50, MotionEvent.ACTION_MOVE, endX, 10f, 0)
        val up =
            MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_UP, endX, 10f, 0)
        try {
            activity.dispatchTouchEvent(down)
            activity.dispatchTouchEvent(move)
            activity.dispatchTouchEvent(up)
        } finally {
            down.recycle()
            move.recycle()
            up.recycle()
        }
    }

    @Test
    fun rightwardFlingNavigatesBack() {
        val (activity, webView) = activityWithHistory()

        fling(activity, startX = 10f, endX = 700f)

        assertEquals(
            "A rightward swipe must walk the WebView history back",
            1,
            shadowOf(webView).goBackInvocations,
        )
    }

    @Test
    fun verticalDragDoesNotNavigateBack() {
        val (activity, webView) = activityWithHistory()
        val downTime = 2_000L
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 10f, 10f, 0)
        val move = MotionEvent.obtain(downTime, downTime + 50, MotionEvent.ACTION_MOVE, 10f, 900f, 0)
        val up = MotionEvent.obtain(downTime, downTime + 100, MotionEvent.ACTION_UP, 10f, 900f, 0)
        try {
            activity.dispatchTouchEvent(down)
            activity.dispatchTouchEvent(move)
            activity.dispatchTouchEvent(up)
        } finally {
            down.recycle()
            move.recycle()
            up.recycle()
        }

        assertEquals(
            "A vertical scroll must never trigger history navigation",
            0,
            shadowOf(webView).goBackInvocations,
        )
    }
}
