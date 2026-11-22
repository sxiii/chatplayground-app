package com.chatplayground.app.utils

import java.net.URI
import java.net.URISyntaxException

/**
 * Maps incoming deep-link URLs onto the canonical in-app WebView URL.
 *
 * Accepted shapes:
 *  - `https://chatplayground.ai/<path>?<query>#<fragment>` (and `http://`, `www.`)
 *  - `chatplayground://<host>/<path>?<query>#<fragment>`
 *
 * Everything else (foreign hosts, look-alike hosts, unknown schemes) resolves to
 * `null` so callers keep their default navigation target.
 */
object DeepLinkResolver {
    const val CANONICAL_URL = "https://chatplayground.ai"
    const val CUSTOM_SCHEME = "chatplayground"

    private val ALLOWED_HOSTS = setOf("chatplayground.ai", "www.chatplayground.ai")

    /** Returns the canonical HTTPS URL for [url], or `null` when it is not a ChatPlayground link. */
    fun resolve(url: String?): String? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        val uri =
            try {
                URI(trimmed)
            } catch (e: URISyntaxException) {
                return null
            }

        val scheme = uri.scheme?.lowercase() ?: return null
        return when (scheme) {
            "http", "https" -> resolveWebUrl(uri)
            CUSTOM_SCHEME -> resolveCustomSchemeUrl(uri)
            else -> null
        }
    }

    /** True when [url] is a deep link this app can handle. */
    fun isDeepLink(url: String?): Boolean = resolve(url) != null

    private fun resolveWebUrl(uri: URI): String? {
        val host = uri.host?.lowercase() ?: return null
        if (host !in ALLOWED_HOSTS) return null
        return CANONICAL_URL + suffix(uri)
    }

    private fun resolveCustomSchemeUrl(uri: URI): String {
        return CANONICAL_URL + suffix(uri)
    }

    /** Path + query + fragment of [uri], with a leading slash enforced on non-empty paths. */
    private fun suffix(uri: URI): String {
        val path = uri.rawPath.orEmpty()
        val builder = StringBuilder()
        if (path.isNotEmpty() && path != "/") {
            if (!path.startsWith("/")) builder.append('/')
            builder.append(path)
        }
        uri.rawQuery?.let { builder.append('?').append(it) }
        uri.rawFragment?.let { builder.append('#').append(it) }
        return builder.toString()
    }
}
