package com.chatplayground.app.security

import android.content.Context

/**
 * Seam for biometric authentication so [SecureStorage] stays unit-testable
 * without a real system BiometricPrompt (which cannot run under Robolectric).
 */
fun interface BiometricAuthenticator {
    /** Returns true when the user has just authenticated biometrically. */
    fun authenticate(context: Context): Boolean
}

object SecureStorage {
    internal const val PREF_NAME = "secure_prefs"

    /**
     * Active authenticator. Production code sets this from the UI layer to one
     * backed by [androidx.biometric.BiometricPrompt]; tests swap in a fake.
     * Defaults to a deny-all authenticator so protected reads fail closed.
     */
    @Volatile
    var authenticator: BiometricAuthenticator = BiometricAuthenticator { false }

    /**
     * Factory for the encrypted backing store. Production uses
     * [EncryptedSharedPreferencesStore] (Android Keystore); unit tests inject an
     * in-memory implementation so read/write behaviour can be asserted under
     * Robolectric, which has no Android Keystore.
     */
    @Volatile
    var storeFactory: (Context) -> EncryptedStore = { context ->
        EncryptedSharedPreferencesStore(context)
    }

    private fun getStore(context: Context): EncryptedStore = storeFactory(context)

    fun putString(
        context: Context,
        key: String,
        value: String,
    ) {
        getStore(context).putString(key, value)
    }

    fun getString(
        context: Context,
        key: String,
        default: String? = null,
    ): String? {
        return getStore(context).getString(key, default)
    }

    /** Removes [key] from secure storage. Best-effort; no-op if absent. */
    fun remove(
        context: Context,
        key: String,
    ) {
        getStore(context).remove(key)
    }

    /** Returns true when [key] is present in secure storage; false otherwise. */
    fun contains(
        context: Context,
        key: String,
    ): Boolean {
        return getStore(context).contains(key)
    }

    /** Wipes all stored values from secure storage. */
    fun clear(context: Context) {
        getStore(context).clear()
    }

    /**
     * Returns the decrypted value for [key] only after successful biometric
     * authentication. Returns `null` when authentication fails or is denied,
     * so protected reads fail closed.
     */
    fun getStringWithBiometric(
        context: Context,
        key: String,
    ): String? {
        return if (authenticator.authenticate(context)) getString(context, key) else null
    }
}
