package com.chatplayground.app.utils

/**
 * Pure-JVM content filter for WebView sub-resource and main-frame requests.
 *
 * Known ad and tracker hostnames are blocked (intercepted) so they never reach
 * the network, keeping pages cleaner and faster without client-side JS
 * injection. Host matching is case-insensitive and ignores query strings /
 * fragments; subdomains of protected hosts are blocked, but look-alike hosts
 * (e.g. `googleads.g.doubleclick.net.evil.com`) are not.
 *
 * No Android dependencies: can be fully unit-tested with plain JUnit.
 */
object ContentFilterRequestHandler {
    private val BLOCKED_HOSTS =
        setOf(
            "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "securepubads.g.doubleclick.net",
        "tpc.googlesyndication.com",
        "www.google-analytics.com",
        "ssl.google-analytics.com",
        "connect.facebook.net",
        "www.googletagservices.com",
    )

    /**
     * Returns true when [url] points to a known ad/tracker host that the
     * WebView should not load.
     */
    fun shouldIntercept(url: String?): Boolean {
        if (url == null || url.isBlank()) return false

        val schemeEnd = url.indexOf("://")
        val hostStart = if (schemeEnd != -1) schemeEnd + 3 else 0
        val pathStart = url.indexOfAny(charArrayOf('/', '?', '#'), hostStart)
        val host = if (pathStart != -1) url.substring(hostStart, pathStart) else url.substring(hostStart)

        return BLOCKED_HOSTS.any { blocked ->
            host.equals(blocked, ignoreCase = true) ||
                host.endsWith(".$blocked", ignoreCase = true)
        }
    }
}
