package com.chatplayground.app

import android.webkit.CookieManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
class MainActivityCookieTest {
    @Test
    fun cookieManagementMenuItemShowsDialog() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()

        val menu = org.robolectric.fakes.RoboMenu(activity)
        activity.onCreateOptionsMenu(menu)
        val item = menu.findItem(R.id.action_manage_cookies)
        assertNotNull("Manage cookies menu item should exist", item)

        activity.onOptionsItemSelected(item)

        val dialog = ShadowAlertDialog.getLatestDialog()
        assertNotNull("Cookie management dialog should be shown", dialog)
    }

    @Test
    fun cookieStateSurvivesActivityRecreation() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val cookieManager = CookieManager.getInstance()

        cookieManager.setCookie("https://chatplayground.ai", "session=abc123; Path=/")
        cookieManager.flush()

        assertTrue("Cookie should be set before recreation", cookieManager.hasCookies())

        activity.recreate()

        assertTrue("Cookie should survive activity recreation", cookieManager.hasCookies())
        assertEquals(
            "session=abc123; Path=/",
            cookieManager.getCookie("https://chatplayground.ai"),
        )
    }
}
