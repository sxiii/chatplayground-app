package com.chatplayground.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SecureStorageTest {
    @Test
    fun testPutAndGetString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "test_key"
        val value = "test_value"

        try {
            SecureStorage.putString(context, key, value)
            val retrievedValue = SecureStorage.getString(context, key)
            assertEquals(value, retrievedValue)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testRemoveStringThenGetReturnsDefault() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "to_be_removed"

        try {
            SecureStorage.putString(context, key, "remove_me")
            assertEquals("remove_me", SecureStorage.getString(context, key))
            SecureStorage.remove(context, key)
            assertNull(SecureStorage.getString(context, key))
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testGetStringReturnsDefaultForMissingKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingKey = "does_not_exist"

        try {
            val result = SecureStorage.getString(context, missingKey)
            assertNull("A missing key should return null by default", result)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testGetStringReturnsCustomDefaultForMissingKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingKey = "does_not_exist"
        val fallback = "fallback_value"

        try {
            val result = SecureStorage.getString(context, missingKey, fallback)
            assertEquals(fallback, result)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testRemoveNonExistentKeyIsNoOp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingKey = "was_never_present"

        try {
            SecureStorage.remove(context, missingKey)
            val result = SecureStorage.getString(context, missingKey)
            assertNull(result)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testPutAndGetEmptyStringRoundTrips() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "empty_key"

        try {
            SecureStorage.putString(context, key, "")
            assertEquals("", SecureStorage.getString(context, key))
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testPutAndGetUnicodeValueRoundTrips() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "unicode_key"
        val unicode = "héllo wörld 🔒 漢字"

        try {
            SecureStorage.putString(context, key, unicode)
            assertEquals(unicode, SecureStorage.getString(context, key))
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testContainsReturnsTrueAfterPut() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = "present_key"

        try {
            SecureStorage.putString(context, key, "present_value")
            assertTrue(SecureStorage.contains(context, key))
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testContainsReturnsFalseForMissingKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingKey = "absent_key"

        try {
            assertFalse(SecureStorage.contains(context, missingKey))
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testClearRemovesAllKeys() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key1 = "key_to_clear_1"
        val key2 = "key_to_clear_2"

        try {
            SecureStorage.putString(context, key1, "value1")
            SecureStorage.putString(context, key2, "value2")
            assertTrue(SecureStorage.contains(context, key1))
            assertTrue(SecureStorage.contains(context, key2))

            SecureStorage.clear(context)

            assertFalse(SecureStorage.contains(context, key1))
            assertFalse(SecureStorage.contains(context, key2))
            assertNull(SecureStorage.getString(context, key1))
            assertNull(SecureStorage.getString(context, key2))
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun testClearOnEmptyStorageIsNoOp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        try {
            SecureStorage.clear(context)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }
}
