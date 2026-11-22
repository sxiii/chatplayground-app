package com.chatplayground.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies [SecureStorage.getStringWithBiometric] enforces biometric
 * authentication and fails closed.
 *
 * Android Keystore (required by EncryptedSharedPreferences) is unavailable
 * under Robolectric, so these tests inject an in-memory [EncryptedStore]
 * through [SecureStorage.storeFactory]. That keeps the read/write assertions
 * meaningful instead of being unreachable behind a KeyStoreException.
 */
@RunWith(RobolectricTestRunner::class)
class SecureStorageBiometricTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    /** In-memory stand-in for [EncryptedSharedPreferencesStore]. */
    private class InMemoryEncryptedStore : EncryptedStore {
        private val data = mutableMapOf<String, String>()

        override fun putString(
            key: String,
            value: String,
        ) {
            data[key] = value
        }

        override fun getString(
            key: String,
            default: String?,
        ): String? {
            return if (data.containsKey(key)) data[key] else default
        }

        override fun remove(key: String) {
            data.remove(key)
        }

        override fun contains(key: String): Boolean = data.containsKey(key)

        override fun clear() {
            data.clear()
        }
    }

    private val store = InMemoryEncryptedStore()

    @Before
    fun setUp() {
        // Fail-closed authenticator by default; individual tests opt in to auth.
        SecureStorage.authenticator = BiometricAuthenticator { false }
        SecureStorage.storeFactory = { store }
        store.clear()
    }

    @After
    fun tearDown() {
        // Restore production wiring so other tests are unaffected by the seam.
        SecureStorage.authenticator = BiometricAuthenticator { false }
        SecureStorage.storeFactory = { ctx -> EncryptedSharedPreferencesStore(ctx) }
    }

    @Test
    fun testGetStringWithBiometricReturnsNullWhenAuthenticationDenied() {
        SecureStorage.putString(context, "biometric_key", "secret_value")

        // Default authenticator denies all → protected read fails closed.
        val result = SecureStorage.getStringWithBiometric(context, "biometric_key")

        assertNull(
            "Should return null when biometric authenticator denies access",
            result,
        )
    }

    @Test
    fun testGetStringWithBiometricReturnsValueWhenAuthenticated() {
        SecureStorage.putString(context, "biometric_key", "secret_value")

        SecureStorage.authenticator = BiometricAuthenticator { true }

        val result = SecureStorage.getStringWithBiometric(context, "biometric_key")

        assertEquals(
            "Should return the stored value after successful biometric auth",
            "secret_value",
            result,
        )
    }

    @Test
    fun testGetStringWithBiometricReturnsNullForMissingKeyWhenAuthenticated() {
        SecureStorage.authenticator = BiometricAuthenticator { true }

        val result =
            SecureStorage.getStringWithBiometric(context, "does_not_exist")

        assertNull(
            "Should return null for a missing key even when authenticated",
            result,
        )
    }

    @Test
    fun testInjectedStorePowersPlainReadsAndRemoval() {
        SecureStorage.putString(context, "plain_key", "plain_value")
        assertTrue(SecureStorage.contains(context, "plain_key"))
        assertEquals("plain_value", SecureStorage.getString(context, "plain_key"))

        SecureStorage.remove(context, "plain_key")
        assertFalse(SecureStorage.contains(context, "plain_key"))
        assertNull(SecureStorage.getString(context, "plain_key"))
    }
}
