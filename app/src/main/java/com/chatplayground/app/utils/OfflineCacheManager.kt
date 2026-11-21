package com.chatplayground.app.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
        FileOutputStream(file).use { it.write(data.toByteArray(Charsets.UTF_8)) }
    }

    fun getCachedResponse(url: String): String? {
        val file = getCacheFile(url)
        return if (file.exists()) {
            FileInputStream(file).use { it.readBytes().toString(Charsets.UTF_8) }
        } else {
            null
        }
    }

    fun clearCache() {
        if (cacheDir.exists()) {
            val files = cacheDir.listFiles() ?: return
            for (file in files) {
                file.delete()
            }
        }
    }
}
