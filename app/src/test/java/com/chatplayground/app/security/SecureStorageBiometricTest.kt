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
class SecureStorageBiometricTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testGetStringWithBiometricReturnsNullWhenAuthenticationDenied() {
        SecureStorage.putString(context, "biometric_key", "secret_value")

        // Default authenticator denies all → protected read fails closed.
        val result = SecureStorage.getStringWithBiometric(context, "biometric_key")

        assertNull(
            "Should return null when biometric authenticator denies access",
            result
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
            result
        )
    }

    @Test
    fun testGetStringWithBiometricReturnsNullForMissingKeyWhenAuthenticated() {
        SecureStorage.authenticator = BiometricAuthenticator { true }

        val result =
            SecureStorage.getStringWithBiometric(context, "does_not_exist")

        assertNull(
            "Should return null for a missing key even when authenticated",
            result
        )
    }
}
