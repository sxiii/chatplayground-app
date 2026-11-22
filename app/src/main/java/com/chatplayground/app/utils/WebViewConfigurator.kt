package com.chatplayground.app.utils

import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Applies the app-wide [WebView] configuration. `chatplayground.ai` is a
 * client-rendered single-page app, so the stock WebView defaults (JavaScript
 * and DOM storage both disabled) cannot render it at all.
 *
 * Call from `MainActivity.onCreate` right after `findViewById(R.id.webview)`
 * and before the first `loadUrl`, so no page can render under stock defaults.
 */
object WebViewConfigurator {
    /**
     * Configures [webView] with:
     * - JavaScript enabled (required by the ChatPlayground SPA)
     * - DOM storage enabled (session/conversation state survives reloads)
     * - Mixed content never allowed (HTTPS pages never load HTTP sub-resources)
     * - Local file and content:// access disabled (never render user-supplied files;
     bundled assets such as `file:///android_asset/offline.html` remain loadable
     because they are served by the asset/resource loaders, not the file:// scheme)
     *
     * Idempotent: safe to call repeatedly; later calls override earlier ones.
     */
    fun configure(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.allowFileAccess = false
        settings.allowContentAccess = false
    }
}
