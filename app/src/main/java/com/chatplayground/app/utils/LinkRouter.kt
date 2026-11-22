package com.chatplayground.app.utils

import java.net.URI
import java.net.URISyntaxException

/**
 * Routing policy for WebView navigations:
 *  - in-app `http(s)` URLs stay in the WebView,
 *  - other `http(s)` URLs leave through a Custom Tab,
 *  - non-web schemes (`mailto:`, `tel:`, ...) are delegated to the system.
 *
 * Parsing uses [java.net.URI] so the policy is plain-JVM testable and shares
 * its host rules with [DeepLinkResolver].
 */
object LinkRouter {

    const val IN_APP_HOST = "chatplayground.ai"

    private val IN_APP_HOSTS = setOf(IN_APP_HOST, "www.$IN_APP_HOST")

    /** True when [url] is an `http(s)` URL on a ChatPlayground host. */
    fun isInAppUrl(url: String?): Boolean {
        val uri = parse(url) ?: return false
        if (!isWebScheme(uri)) return false
        val host = uri.host?.lowercase() ?: return false
        return host in IN_APP_HOSTS
    }

    /** True when [url] is an `http(s)` URL that belongs outside the app. */
    fun isExternalWebUrl(url: String?): Boolean {
        val uri = parse(url) ?: return false
        return isWebScheme(uri) && !isInAppUrl(url)
    }

    /** True when [url] uses a non-`http(s)` scheme the system should resolve. */
    fun isExternalScheme(url: String?): Boolean {
        val uri = parse(url) ?: return false
        return !isWebScheme(uri)
    }

    private fun isWebScheme(uri: URI): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return false
        return scheme == "http" || scheme == "https"
    }

    private fun parse(url: String?): URI? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return try {
            URI(trimmed)
        } catch (e: URISyntaxException) {
            null
        }
    }
}