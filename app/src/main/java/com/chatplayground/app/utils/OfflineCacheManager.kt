package com.chatplayground.app.utils

import java.io.File
import java.security.MessageDigest

class OfflineCacheManager(private val cacheDir: File) {

    private fun getCacheFile(url: String): File {
        val bytes = MessageDigest.getInstance("MD5").digest(url.toByteArray())
        val hash = bytes.joinToString("") { "%02x".format(it) }
        return File(cacheDir, "cache_$hash.json")
    }

    fun saveCachedResponse(url: String, data: String) {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val file = getCacheFile(url)
        file.writeText(data)
    }

    fun getCachedResponse(url: String): String? {
        val file = getCacheFile(url)
        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }

    fun clearCache() {
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }
}
