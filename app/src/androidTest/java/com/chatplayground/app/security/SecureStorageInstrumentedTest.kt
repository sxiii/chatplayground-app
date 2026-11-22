package com.chatplayground.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNull
import org.junit.Test

class SecureStorageInstrumentedTest {
    @Test
    fun testGetStringReturnsNullWhenAbsent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val value = SecureStorage.getString(context, "nonexistent")
        assertNull(value)
    }
}
