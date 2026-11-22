package com.chatplayground.app.utils

import android.webkit.WebView

/** Enables WebView remote debugging (`chrome://inspect` over adb) for debug builds only. */
object WebViewDebugging {
    /**
     * Injectable seam: production calls the real static API, tests substitute a recorder to
     * assert the decision — Robolectric's `ShadowWebView` implements
     * `setWebContentsDebuggingEnabled` as a no-op with no readable state.
     */
    internal var applyDebuggingEnabled: (Boolean) -> Unit = { enabled ->
        WebView.setWebContentsDebuggingEnabled(enabled)
    }

    /**
     * When [debugBuild] is `true`, enables WebView remote debugging so a developer can attach
     * DevTools via `chrome://inspect`. When it is `false` (release), nothing happens — the
     * release build never exposes its renderer or page state to an attached debugger.
     *
     * Idempotent: safe to call repeatedly; later calls override earlier ones.
     */
    fun configure(debugBuild: Boolean) {
        if (debugBuild) applyDebuggingEnabled(true)
    }
}
