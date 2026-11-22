package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behaviour contract for [DeepLinkResolver]: which incoming URLs the app accepts
 * and how they are mapped onto the canonical in-app WebView URL.
 */
class DeepLinkResolverTest {

    private val canonical = "https://chatplayground.ai"

    @Test
    fun resolvesCanonicalHttpsUrlUnchanged() {
        assertEquals(
            "https://chatplayground.ai/chat/42",
            DeepLinkResolver.resolve("https://chatplayground.ai/chat/42")
        )
    }

    @Test
    fun resolvesBareCanonicalUrl() {
        assertEquals(canonical, DeepLinkResolver.resolve("https://chatplayground.ai"))
    }

    @Test
    fun resolvesWwwHostToCanonicalHost() {
        assertEquals(
            "https://chatplayground.ai/settings?tab=theme",
            DeepLinkResolver.resolve("https://www.chatplayground.ai/settings?tab=theme")
        )
    }

    @Test
    fun upgradesHttpToHttps() {
        assertEquals(
            "https://chatplayground.ai/about",
            DeepLinkResolver.resolve("http://chatplayground.ai/about")
        )
    }

    @Test
    fun resolvesCustomSchemePath() {
        assertEquals(
            "https://chatplayground.ai/chat/42",
            DeepLinkResolver.resolve("chatplayground://app/chat/42")
        )
    }

    @Test
    fun resolvesCustomSchemeWithoutPathToCanonicalUrl() {
        assertEquals(canonical, DeepLinkResolver.resolve("chatplayground://app"))
    }

    @Test
    fun preservesQueryAndFragmentForCustomScheme() {
        assertEquals(
            "https://chatplayground.ai/chat?id=7#msg-9",
            DeepLinkResolver.resolve("chatplayground://app/chat?id=7#msg-9")
        )
    }

    @Test
    fun rejectsForeignHost() {
        assertNull(DeepLinkResolver.resolve("https://example.com/phish"))
    }

    @Test
    fun rejectsLookAlikeHost() {
        assertNull(DeepLinkResolver.resolve("https://chatplayground.ai.evil.com/steal"))
    }

    @Test
    fun rejectsUnknownSchemes() {
        assertNull(DeepLinkResolver.resolve("javascript:alert(1)"))
        assertNull(DeepLinkResolver.resolve("file:///etc/passwd"))
        assertNull(DeepLinkResolver.resolve("intent://chatplayground.ai#Intent;end"))
    }

    @Test
    fun rejectsNullBlankAndGarbage() {
        assertNull(DeepLinkResolver.resolve(null))
        assertNull(DeepLinkResolver.resolve(""))
        assertNull(DeepLinkResolver.resolve("   "))
        assertNull(DeepLinkResolver.resolve("not a url"))
    }

    @Test
    fun isDeepLinkMatchesResolveOutcome() {
        assertTrue(DeepLinkResolver.isDeepLink("https://chatplayground.ai/chat/42"))
        assertTrue(DeepLinkResolver.isDeepLink("chatplayground://app/chat/42"))
        assertFalse(DeepLinkResolver.isDeepLink("https://example.com"))
        assertFalse(DeepLinkResolver.isDeepLink(null))
    }
}