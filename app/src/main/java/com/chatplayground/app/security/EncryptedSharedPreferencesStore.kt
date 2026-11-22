package com.chatplayground.app.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Production [EncryptedStore] backed by AndroidX `EncryptedSharedPreferences`
 * and the Android Keystore.
 *
 * Keystore access requires a real device/emulator, so this implementation is
 * not exercised by Robolectric unit tests; see [EncryptedStore].
 */
class EncryptedSharedPreferencesStore(private val context: Context) : EncryptedStore {
    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            SecureStorage.PREF_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getString(
        key: String,
        default: String?,
    ): String? {
        return prefs.getString(key, default)
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
