package com.chatplayground.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies that the merged manifest actually advertises MainActivity for both
 * deep-link shapes, so an external app or the system browser can open it.
 */
@RunWith(RobolectricTestRunner::class)
class ManifestDeepLinkTest {

    private fun resolvesFor(uri: String): Boolean {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        return context.packageManager.queryIntentActivities(intent, 0)
            .any { it.activityInfo?.name == MainActivity::class.java.name }
    }

    @Test
    fun manifestResolvesHttpsAppLinkForMainActivity() {
        assertTrue(
            "MainActivity must handle https://chatplayground.ai links",
            resolvesFor("https://chatplayground.ai/chat/42")
        )
    }

    @Test
    fun manifestResolvesWwwHttpsAppLinkForMainActivity() {
        assertTrue(
            "MainActivity must handle https://www.chatplayground.ai links",
            resolvesFor("https://www.chatplayground.ai/chat/42")
        )
    }

    @Test
    fun manifestResolvesCustomSchemeForMainActivity() {
        assertTrue(
            "MainActivity must handle chatplayground:// links",
            resolvesFor("chatplayground://app/chat/42")
        )
    }
}