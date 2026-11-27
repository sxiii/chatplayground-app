package com.chatplayground.app.utils

/**
 * Pure-JVM helper to identify and resolve URLs of bundled assets for WebViews.
 *
 * No Android dependencies: can be fully unit-tested with plain JUnit.
 */
object AssetRequestHandler {
    private val ALLOWED_ASSETS = setOf(
        "offline.html",
        "style.css",
        "app.js",
        "favicon.ico",
    )

    /**
     * Maps an incoming WebView request URL to a bundled asset path.
     * Returns the relative path (e.g. "offline.html") if the URL is of the format
     * "file:///android_asset/<path>" and the path is whitelisted.
     * Otherwise returns null.
     */
    fun assetPathFor(url: String?): String? {
        if (url == null || url.isBlank()) return null

        val prefix = "file:///android_asset/"
        if (!url.startsWith(prefix)) return null

        val path = url.substring(prefix.length)
        // Strip out query parameters and hash fragments to extract the bare path
        val cleanPath = path.substringBefore('?').substringBefore('#')

        if (cleanPath in ALLOWED_ASSETS) {
            return cleanPath
        }
        return null
    }

    /**
     * Returns true if the URL points to a whitelisted bundled asset.
     */
    fun shouldIntercept(url: String?): Boolean {
        return assetPathFor(url) != null
    }
}
