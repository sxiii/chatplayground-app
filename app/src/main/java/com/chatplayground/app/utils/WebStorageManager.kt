package com.chatplayground.app.utils

import android.webkit.ValueCallback
import android.webkit.WebStorage

/**
 * Manages WebStorage (localStorage, IndexedDB, etc.) origins and deletion.
 * Provides seams for testing under Robolectric.
 */
object WebStorageManager {
    /** Seam for getting origins. Defaults to real [WebStorage.getOrigins]. */
    @Volatile
    var originFetcher: (ValueCallback<Map<*, *>?>) -> Unit = { callback ->
        WebStorage.getInstance().getOrigins(callback)
    }

    /** Seam for deleting a specific origin. Defaults to real [WebStorage.deleteOrigin]. */
    @Volatile
    var originDeleter: (String) -> Unit = { origin ->
        WebStorage.getInstance().deleteOrigin(origin)
    }

    /** Seam for deleting all WebStorage. Defaults to real [WebStorage.deleteAllData]. */
    @Volatile
    var allDataDeleter: () -> Unit = {
        WebStorage.getInstance().deleteAllData()
    }

    fun getOrigins(callback: (List<String>) -> Unit) {
        originFetcher { map ->
            if (map == null) {
                callback(emptyList())
                return@originFetcher
            }
            val origins = map.keys.mapNotNull { key ->
                when (key) {
                    is String -> key
                    is WebStorage.Origin -> key.origin
                    else -> key?.toString()
                }
            }
            callback(origins)
        }
    }

    fun deleteOrigin(origin: String) {
        originDeleter(origin)
    }

    fun deleteAllData() {
        allDataDeleter()
    }

    fun resetForTesting() {
        originFetcher = { callback -> WebStorage.getInstance().getOrigins(callback) }
        originDeleter = { origin -> WebStorage.getInstance().deleteOrigin(origin) }
        allDataDeleter = { WebStorage.getInstance().deleteAllData() }
    }
}
