package com.chatplayground.app.utils

import android.webkit.WebView

/** Enables WebView remote debugging (`chrome://inspect` over adb) for debug builds only. */
object WebViewDebugging {

    /**
     * When [debugBuild] is `true`, calls `WebView.setWebContentsDebuggingEnabled(true)` so a
     * developer can attach DevTools via `chrome://inspect`. When it is `false` (release),
     * nothing happens — the release build never exposes its renderer or page state.
     *
     * Idempotent: safe to call repeatedly; later calls override earlier ones.
     *
     * @param apply injectable seam defaulting to the real static call. Tests substitute a
     * recorder to assert the wrapper's decision because Robolectric's `ShadowWebView` implements
     * `setWebContentsDebuggingEnabled` as a no-op with no readable state.
     */
    fun configure(debugBuild: Boolean, apply: (Boolean) -> Unit = ::setDebuggingEnabled) {
        if (debugBuild) apply(true)
    }

    private fun setDebuggingEnabled(enabled: Boolean) {
        WebView.setWebContentsDebuggingEnabled(enabled)
    }
}
