package com.chatplayground.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24])
class SecureStorageTest {
    @Test
    fun testPutAndGetString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "test_key"
        val value = "test_value"

        SecureStorage.putString(context, key, value)
        val retrievedValue = SecureStorage.getString(context, key)

        assertEquals(value, retrievedValue)
    }
}
