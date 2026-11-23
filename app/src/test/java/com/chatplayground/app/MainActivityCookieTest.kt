package com.chatplayground.app

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
class MainActivityCookieTest {
    @Test
    fun cookieManagementMenuItemShowsDialog() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
        
        val menu = shadowOf(activity).optionsMenu
        val item = menu.findItem(R.id.action_manage_cookies)
        assertNotNull("Manage cookies menu item should exist", item)
        
        activity.onOptionsItemSelected(item)
        
        val dialog = ShadowAlertDialog.getLatestDialog()
        assertNotNull("Cookie management dialog should be shown", dialog)
    }
}
