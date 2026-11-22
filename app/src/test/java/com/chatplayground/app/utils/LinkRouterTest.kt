package com.chatplayground.app.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioral tests for [LinkRouter]: which WebView navigations stay in the app
 * and which must leave it through a Custom Tab.
 */
class LinkRouterTest {

    @Test
    fun chatPlaygroundHostStaysInWebView() {
        assertTrue(LinkRouter.isInAppUrl("https://chatplayground.ai/pricing"))
        assertFalse(LinkRouter.isExternalWebUrl("https://chatplayground.ai/pricing"))
    }

    @Test
    fun wwwHostWithExplicitPortStaysInWebView() {
        assertTrue(LinkRouter.isInAppUrl("https://www.chatplayground.ai:443/chat"))
        assertFalse(LinkRouter.isExternalWebUrl("https://www.chatplayground.ai:443/chat"))
    }

    @Test
    fun hostComparisonIsCaseInsensitive() {
        assertTrue(LinkRouter.isInAppUrl("HTTPS://ChatPlayground.AI/About"))
    }

    @Test
    fun foreignHttpsHostOpensExternally() {
        assertTrue(LinkRouter.isExternalWebUrl("https://github.com/sxiii/chatplayground-app"))
        assertTrue(LinkRouter.isExternalWebUrl("http://example.com/checkout"))
    }

    @Test
    fun lookAlikeHostOpensExternally() {
        assertTrue(LinkRouter.isExternalWebUrl("https://chatplayground.ai.evil.com/login"))
        assertFalse(LinkRouter.isInAppUrl("https://chatplayground.ai.evil.com/login"))
    }

    @Test
    fun subdomainIsNotTreatedAsInApp() {
        assertFalse(LinkRouter.isInAppUrl("https://docs.chatplayground.ai/guide"))
        assertTrue(LinkRouter.isExternalWebUrl("https://docs.chatplayground.ai/guide"))
    }

    @Test
    fun nonHttpSchemesAreExternalSchemesButNotWebUrls() {
        assertTrue(LinkRouter.isExternalScheme("mailto:support@chatplayground.ai"))
        assertTrue(LinkRouter.isExternalScheme("tel:+15551234"))
        assertFalse(LinkRouter.isExternalWebUrl("mailto:support@chatplayground.ai"))
        assertFalse(LinkRouter.isInAppUrl("mailto:support@chatplayground.ai"))
    }

    @Test
    fun httpAndHttpsAreWebUrlsNotExternalSchemes() {
        assertFalse(LinkRouter.isExternalScheme("https://example.com"))
        assertFalse(LinkRouter.isExternalScheme("https://chatplayground.ai"))
    }

    @Test
    fun blankAndMalformedUrlsAreRejected() {
        assertFalse(LinkRouter.isExternalWebUrl(null))
        assertFalse(LinkRouter.isExternalWebUrl(""))
        assertFalse(LinkRouter.isExternalWebUrl("   "))
        assertFalse(LinkRouter.isExternalWebUrl("not a url"))
        assertFalse(LinkRouter.isInAppUrl(null))
        assertFalse(LinkRouter.isInAppUrl("not a url"))
        assertFalse(LinkRouter.isExternalScheme(null))
        assertFalse(LinkRouter.isExternalScheme("not a url"))
    }
}