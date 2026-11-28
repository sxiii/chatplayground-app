package com.chatplayground.app.utils

import android.app.Activity
import android.os.Looper
import android.webkit.ValueCallback
import android.webkit.WebStorage
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

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
            val origins =
                map.keys.mapNotNull { key ->
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

    /**
     * Shows a dialog listing WebStorage origins. Selecting an origin clears that
     * origin; "Clear All" clears every origin. Empty storage shows a message only.
     */
    fun showSiteDataManagementDialog(activity: Activity) {
        getOrigins { origins ->
            val present = Runnable { presentSiteDataDialog(activity, origins) }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                present.run()
            } else {
                activity.runOnUiThread(present)
            }
        }
    }

    private fun presentSiteDataDialog(
        activity: Activity,
        origins: List<String>,
    ) {
        if (origins.isEmpty()) {
            AlertDialog.Builder(activity)
                .setTitle("Site Data")
                .setMessage("No site data currently stored.")
                .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val items = origins.toTypedArray()
        AlertDialog.Builder(activity)
            .setTitle("Site Data")
            .setItems(items) { dialog, which ->
                val origin = items[which]
                deleteOrigin(origin)
                Toast.makeText(activity, "Cleared $origin", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Clear All") { dialog, _ ->
                deleteAllData()
                Toast.makeText(activity, "All site data cleared", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun resetForTesting() {
        originFetcher = { callback -> WebStorage.getInstance().getOrigins(callback) }
        originDeleter = { origin -> WebStorage.getInstance().deleteOrigin(origin) }
        allDataDeleter = { WebStorage.getInstance().deleteAllData() }
    }
}
