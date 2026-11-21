package com.chatplayground.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Seam for biometric authentication so [SecureStorage] stays unit-testable
 * without a real system BiometricPrompt (which cannot run under Robolectric).
 */
fun interface BiometricAuthenticator {
    /** Returns true when the user has just authenticated biometrically. */
    fun authenticate(context: Context): Boolean
}

object SecureStorage {
    private const val PREF_NAME = "secure_prefs"

    /**
     * Active authenticator. Production code sets this from the UI layer to one
     * backed by [androidx.biometric.BiometricPrompt]; tests swap in a fake.
     * Defaults to a deny-all authenticator so protected reads fail closed.
     */
    @Volatile
    var authenticator: BiometricAuthenticator = BiometricAuthenticator { false }

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREF_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun putString(context: Context, key: String, value: String) {
        getPrefs(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, default: String? = null): String? {
        return getPrefs(context).getString(key, default)
    }

    /** Removes [key] from secure storage. Best-effort; no-op if absent. */
    fun remove(context: Context, key: String) {
        getPrefs(context).edit().remove(key).apply()
    }

    /** Returns true when [key] is present in secure storage; false otherwise. */
    fun contains(context: Context, key: String): Boolean {
        return getPrefs(context).contains(key)
    }

    /** Wipes all stored values from secure storage. */
    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    /**
     * Returns the decrypted value for [key] only after successful biometric
     * authentication. Returns `null` when authentication fails or is denied,
     * so protected reads fail closed.
     */
    fun getStringWithBiometric(context: Context, key: String): String? {
        return if (authenticator.authenticate(context)) getString(context, key) else null
    }
}
