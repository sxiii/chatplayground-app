package com.chatplayground.app.utils

/**
 * The WebView capabilities [GestureNavigationHandler] needs before it may
 * claim a horizontal swipe for history navigation.
 *
 * Kept as a tiny interface (no Android types) so the gesture policy stays
 * plain-JUnit testable: tests supply a fake, while `MainActivity` adapts the
 * real `WebView` onto it.
 */
interface GestureNavigationTarget {
    /** True when the WebView has a previous document to return to. */
    fun canGoBack(): Boolean

    /** True when the WebView has a next document to move forward to. */
    fun canGoForward(): Boolean

    /**
     * True when the page content itself scrolls sideways at the touched
     * position (code blocks, carousels, swipeable rows). Swipes there belong
     * to the content, not to history navigation.
     */
    fun canContentScrollHorizontally(): Boolean
}
