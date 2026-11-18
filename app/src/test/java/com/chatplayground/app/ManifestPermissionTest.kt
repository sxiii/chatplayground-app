package com.chatplayground.app

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ManifestPermissionTest {

    @Test
    fun hasRequiredPermissions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val packageName = context.packageName
        
        val packageInfo = packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
        val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()

        assertTrue(
            "Missing INTERNET permission",
            requestedPermissions.contains(Manifest.permission.INTERNET)
        )
        assertTrue(
            "Missing ACCESS_NETWORK_STATE permission",
            requestedPermissions.contains(Manifest.permission.ACCESS_NETWORK_STATE)
        )
    }
}
