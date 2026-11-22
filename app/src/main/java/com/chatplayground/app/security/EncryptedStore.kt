package com.chatplayground.app.security

/**
 * Abstraction over an encrypted key-value store so [SecureStorage] stays unit
 * testable without a real Android Keystore (Robolectric does not provide one).
 *
 * The production implementation ([EncryptedSharedPreferencesStore]) wraps
 * AndroidX `EncryptedSharedPreferences`; tests inject an in-memory fake via
 * [SecureStorage.storeFactory].
 */
interface EncryptedStore {
    fun putString(key: String, value: String)
    fun getString(key: String, default: String?): String?
    fun remove(key: String)
    fun contains(key: String): Boolean
    fun clear()
}