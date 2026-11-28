package com.chatplayground.app

import com.chatplayground.app.utils.SingletonCleanupRule
import com.chatplayground.app.utils.WebStorageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
class MainActivitySiteDataTest {
    @Rule @JvmField
    val singletonCleanupRule = SingletonCleanupRule()

    @Test
    fun siteDataMenuItemShowsDialogListingOrigins() {
        WebStorageManager.originFetcher = { callback ->
            callback.onReceiveValue(
                mapOf(
                    "https://chatplayground.ai" to "data1",
                    "https://example.com" to "data2",
                ),
            )
        }

        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
        val menu = org.robolectric.fakes.RoboMenu(activity)
        activity.onCreateOptionsMenu(menu)
        val item = menu.findItem(R.id.action_manage_site_data)
        assertNotNull("Site Data menu item should exist", item)

        activity.onOptionsItemSelected(item)

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull("Site Data dialog should be shown", dialog)
        val listView = dialog.listView
        assertNotNull("Site Data dialog should list origins", listView)
        assertEquals(2, listView.count)
    }

    @Test
    fun siteDataDialogDeleteOriginInvokesSeamForSelectedOrigin() {
        var deletedOrigin: String? = null
        WebStorageManager.originFetcher = { callback ->
            callback.onReceiveValue(
                mapOf(
                    "https://chatplayground.ai" to "data1",
                    "https://example.com" to "data2",
                ),
            )
        }
        WebStorageManager.originDeleter = { origin ->
            deletedOrigin = origin
        }

        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
        WebStorageManager.showSiteDataManagementDialog(activity)

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull(dialog)
        // Robolectric may not attach list children; index-based click still fires the listener.
        dialog.listView.performItemClick(null, 0, 0L)

        assertEquals("https://chatplayground.ai", deletedOrigin)
    }

    @Test
    fun siteDataDialogShowsEmptyMessageWhenNoOrigins() {
        WebStorageManager.originFetcher = { callback ->
            callback.onReceiveValue(emptyMap<Any, Any>())
        }

        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
        WebStorageManager.showSiteDataManagementDialog(activity)

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull(dialog)
        assertTrue(
            "Empty Site Data dialog should explain there is no stored data",
            dialog.message?.toString()?.contains("No site data") == true,
        )
    }
}
