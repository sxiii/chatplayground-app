package com.chatplayground.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecureStorage {
    private const val PREF_NAME = "secure_prefs"
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
}
