package com.chatplayground.app.utils

/**
 * Controls the User-Agent and viewport settings for [android.webkit.WebView].
 * Switches between a mobile-like UA (for `chatplayground.ai` default view)
 * and a desktop-like UA (for desktop rendering).
 */
object DesktopModeHandler {
    private const val MOBILE_UA = "ChatPlayground/1.0"

    /** The desktop-like User-Agent string. */
    private const val DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36 ChatPlayground/1.0"

    var userAgent: String = MOBILE_UA
        set(value) {
            field = value
        }

    var useWideViewPort: Boolean = false
        set(value) {
            field = value
        }

    var loadWithOverviewMode: Boolean = false
        set(value) {
            field = value
        }

    var isDesktop: Boolean = false
        set(value) {
            field = value
        }

    /**
     * Toggles the current mode.
     */
    fun toggle() {
        isDesktop = !isDesktop
        userAgent = if (isDesktop) DESKTOP_UA else MOBILE_UA
        useWideViewPort = isDesktop
        loadWithOverviewMode = isDesktop
    }

    /**
     * Applies the configuration to the given [webView].
     * Call this after [webView] is created and before [loadUrl].
     */
    fun applyTo(webView: android.webkit.WebView) {
        webView.settings.userAgentString = userAgent
        webView.settings.loadWithOverviewMode = loadWithOverviewMode
        webView.settings.useWideViewPort = useWideViewPort
    }
}
