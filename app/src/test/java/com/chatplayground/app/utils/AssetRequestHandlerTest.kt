package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Pure-JVM regression suite for [AssetRequestHandler].
 *
 * No Robolectric runner, no @Config, no Android shadows: this class must stay
 * runnable on a plain JVM so the asset-interception policy can be verified
 * without an Android SDK. If any Android import creeps in, the build will fail
 * here.
 */
class AssetRequestHandlerTest {
    private val nullUrl: String? = null

    @Test
    fun assetPathForReturnsNullForNullUrl() {
        assertNull(
            "A null URL must never be intercepted",
            AssetRequestHandler.assetPathFor(nullUrl),
        )
    }

    @Test
    fun assetPathForReturnsNullForBlankUrls() {
        assertNull("Empty string must not be intercepted", AssetRequestHandler.assetPathFor(""))
        assertNull("Whitespace-only string must not be intercepted", AssetRequestHandler.assetPathFor("   "))
    }

    @Test
    fun assetPathForReturnsNullForRemoteUrls() {
        assertNull("https must not be intercepted", AssetRequestHandler.assetPathFor("https://chatplayground.ai"))
        assertNull("http must not be intercepted", AssetRequestHandler.assetPathFor("http://chatplayground.ai"))
        assertNull(
            "file on a non-android_asset host must not be intercepted",
            AssetRequestHandler.assetPathFor("file:///other/offline.html"),
        )
    }

    @Test
    fun assetPathForReturnsAssetPathForWhitelistedAsset() {
        assertEquals("offline.html", AssetRequestHandler.assetPathFor("file:///android_asset/offline.html"))
    }

    @Test
    fun assetPathForAcceptsWhitelistedCssAndJsAssets() {
        assertEquals("style.css", AssetRequestHandler.assetPathFor("file:///android_asset/style.css"))
        assertEquals("app.js", AssetRequestHandler.assetPathFor("file:///android_asset/app.js"))
    }

    @Test
    fun everyAllowedAssetExistsInBundledAssets() {
        val assetsDir = locateAssetsDir()
        val missing =
            AssetRequestHandler.ALLOWED_ASSETS.filter { name ->
                val file = File(assetsDir, name)
                !file.isFile || file.length() == 0L
            }
        assertTrue(
            "Whitelisted assets must ship in app/src/main/assets; missing or empty: $missing",
            missing.isEmpty(),
        )
    }

    /** Finds app/src/main/assets from either the module dir or the repo root. */
    private fun locateAssetsDir(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            File(dir, "app/src/main/assets").takeIf { it.isDirectory }?.let { return it }
            File(dir, "src/main/assets").takeIf { it.isDirectory }?.let { return it }
            dir = dir.parentFile
        }
        throw AssertionError("Could not locate app/src/main/assets from ${System.getProperty("user.dir")}")
    }

    @Test
    fun assetPathForRejectsNonWhitelistedAssetNames() {
        assertNull(
            "Asset names not on the allowed list must not be intercepted",
            AssetRequestHandler.assetPathFor("file:///android_asset/unknown.txt"),
        )
    }

    @Test
    fun assetPathForRejectsPathTraversal() {
        assertNull(
            ".. must never escape the assets directory",
            AssetRequestHandler.assetPathFor("file:///android_asset/../../../etc/passwd"),
        )
        assertNull(
            ".. segments must not match any allowed asset",
            AssetRequestHandler.assetPathFor("file:///android_asset/./offline.html"),
        )
    }

    @Test
    fun assetPathForRejectsSubdirectoryAssets() {
        assertNull(
            "Assets in subdirectories are not on the allowed list",
            AssetRequestHandler.assetPathFor("file:///android_asset/css/offline.html"),
        )
        assertNull(
            "Nested paths are rejected",
            AssetRequestHandler.assetPathFor("file:///android_asset/a/b/offline.html"),
        )
    }

    @Test
    fun assetPathForIgnoresQueryAndFragment() {
        assertEquals("offline.html", AssetRequestHandler.assetPathFor("file:///android_asset/offline.html?v=1"))
        assertEquals("offline.html", AssetRequestHandler.assetPathFor("file:///android_asset/offline.html#top"))
    }

    @Test
    fun shouldInterceptMirrorsAssetPathFor() {
        assertTrue("offline.html is interceptable", AssetRequestHandler.shouldIntercept("file:///android_asset/offline.html"))

        assertTrue(
            "style.css is whitelisted and must be interceptable",
            AssetRequestHandler.shouldIntercept("file:///android_asset/style.css"),
        )

        assertFalse("https is not interceptable", AssetRequestHandler.shouldIntercept("https://chatplayground.ai"))
        assertFalse("null is not interceptable", AssetRequestHandler.shouldIntercept(nullUrl))
        assertFalse(
            "non-whitelisted asset is not interceptable",
            AssetRequestHandler.shouldIntercept("file:///android_asset/unknown.txt"),
        )
        assertFalse("path traversal is not interceptable", AssetRequestHandler.shouldIntercept("file:///android_asset/../../../etc/passwd"))
    }
}
