package com.chatplayground.app.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioral tests for [ContentFilterRequestHandler]: known ad/tracker
 * domains are intercepted (blocked) while legitimate content passes through.
 *
 * No Robolectric runner, no @Config, no Android shadows: this class must stay
 * runnable on a plain JVM so the content-filter policy can be verified without
 * an Android SDK. If any Android import creeps in, the build will fail here.
 */
class ContentFilterRequestHandlerTest {
    @Test
    fun shouldInterceptReturnsFalseForNullAndBlankUrls() {
        assertFalse("null URL must not be blocked", ContentFilterRequestHandler.shouldIntercept(null))
        assertFalse("empty string must not be blocked", ContentFilterRequestHandler.shouldIntercept(""))
        assertFalse("whitespace-only string must not be blocked", ContentFilterRequestHandler.shouldIntercept("   "))
    }

    @Test
    fun shouldInterceptReturnsFalseForChatPlaygroundAndOtherLegitimateHosts() {
        assertFalse(
            "chatplayground.ai must pass through",
            ContentFilterRequestHandler.shouldIntercept("https://chatplayground.ai/pricing"),
        )
        assertFalse(
            "subdomain must pass through",
            ContentFilterRequestHandler.shouldIntercept("https://docs.chatplayground.ai/guide"),
        )
        assertFalse(
            "unrelated legitimate host must pass through",
            ContentFilterRequestHandler.shouldIntercept("https://developer.mozilla.org/en-US/docs/Web"),
        )
        assertFalse(
            "non-http scheme must not be blocked",
            ContentFilterRequestHandler.shouldIntercept("mailto:support@chatplayground.ai"),
        )
    }

    @Test
    fun shouldInterceptReturnsTrueForKnownAdDomains() {
        val adUrls =
            listOf(
                "https://googleads.g.doubleclick.net/pagead/ad",
                "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js",
                "https://securepubads.g.doubleclick.net/gampad/ads",
                "https://tpc.googlesyndication.com/sodar/sodar2/224/runner.html",
            )
        adUrls.forEach { url ->
            assertTrue(
                "$url must be blocked by the content filter",
                ContentFilterRequestHandler.shouldIntercept(url),
            )
        }
    }

    @Test
    fun shouldInterceptReturnsTrueForKnownTrackerDomains() {
        val trackerUrls =
            listOf(
                "https://www.google-analytics.com/analytics.js",
                "https://ssl.google-analytics.com/collect",
                "https://connect.facebook.net/en_US/fbevents.js",
                "https://www.googletagservices.com/tag/js/gpt.js",
            )
        trackerUrls.forEach { url ->
            assertTrue(
                "$url must be blocked by the content filter",
                ContentFilterRequestHandler.shouldIntercept(url),
            )
        }
    }

    @Test
    fun shouldInterceptIsCaseInsensitiveOnHost() {
        assertTrue(
            "GOOGLEADS.G.DOUBLECLICK.NET must be blocked regardless of case",
            ContentFilterRequestHandler.shouldIntercept("HTTPS://GOOGLEADS.G.DOUBLECLICK.NET/pagead/ad"),
        )
        assertTrue(
            "Pagead2.Googlesyndication.com must be blocked regardless of case",
            ContentFilterRequestHandler.shouldIntercept("https://Pagead2.Googlesyndication.com/pagead/js/adsbygoogle.js"),
        )
    }

    @Test
    fun shouldInterceptIgnoresQueryAndFragment() {
        assertTrue(
            "query string must not let an ad host pass through",
            ContentFilterRequestHandler.shouldIntercept("https://googleads.g.doubleclick.net/pagead/ad?id=123&foo=bar"),
        )
        assertTrue(
            "fragment must not let an ad host pass through",
            ContentFilterRequestHandler.shouldIntercept("https://www.google-analytics.com/analytics.js#section"),
        )
    }

    @Test
    fun shouldInterceptDoesNotBlockLookAlikeDomains() {
        assertFalse(
            "doubleclick.net.evil.com is not an ad domain",
            ContentFilterRequestHandler.shouldIntercept("https://googleads.g.doubleclick.net.evil.com/track"),
        )
        assertFalse(
            "analytics.com is not google-analytics.com",
            ContentFilterRequestHandler.shouldIntercept("https://www.analytics.com/beacon"),
        )
    }

    @Test
    fun shouldInterceptBlocksAdHostWithPort() {
        assertTrue(
            "ad host with port must be blocked",
            ContentFilterRequestHandler.shouldIntercept("https://googleads.g.doubleclick.net:8080/pagead/ad"),
        )
    }

    @Test
    fun shouldInterceptBlocksAdHostWithPortAndQuery() {
        assertTrue(
            "ad host with port and query must be blocked",
            ContentFilterRequestHandler.shouldIntercept("https://googleads.g.doubleclick.net:8080/pagead/ad?id=123&foo=bar"),
        )
    }

    @Test
    fun shouldInterceptDoesNotBlockLegitimateHostWithPort() {
        assertFalse(
            "legitimate host with port must pass through",
            ContentFilterRequestHandler.shouldIntercept("https://chatplayground.ai:8080/pricing"),
        )
    }

    @Test
    fun shouldInterceptDoesNotBlockLegitimateHostWithPortAndFragment() {
        assertFalse(
            "legitimate host with port and fragment must pass through",
            ContentFilterRequestHandler.shouldIntercept("https://docs.chatplayground.ai:8443/guide#section"),
        )
    }
}
