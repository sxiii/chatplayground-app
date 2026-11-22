package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Files

class OfflineCacheManagerTest {
    @Test
    fun testSaveAndRetrieveCache() {
        val tempDir = Files.createTempDirectory("cache_test").toFile()
        val cacheManager = OfflineCacheManager(tempDir)

        val url = "https://chatplayground.ai/api/chat"
        val content = "{\"status\":\"ok\"}"

        cacheManager.saveCachedResponse(url, content)
        val retrieved = cacheManager.getCachedResponse(url)

        assertEquals(content, retrieved)
        tempDir.deleteRecursively()
    }

    @Test
    fun testCacheMiss() {
        val tempDir = Files.createTempDirectory("cache_test").toFile()
        val cacheManager = OfflineCacheManager(tempDir)

        val retrieved = cacheManager.getCachedResponse("https://chatplayground.ai/nonexistent")
        assertNull(retrieved)

        tempDir.deleteRecursively()
    }
}
