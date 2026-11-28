package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WebStorageManagerTest {
    @Rule @JvmField
    val singletonCleanupRule = SingletonCleanupRule()

    @Test
    fun getOriginsParsesOriginsCorrectly() {
        var retrievedOrigins: List<String>? = null
        WebStorageManager.originFetcher = { callback ->
            val mockMap =
                mapOf(
                    "https://chatplayground.ai" to "data1",
                    "https://google.com" to "data2",
                )
            callback.onReceiveValue(mockMap)
        }

        WebStorageManager.getOrigins { origins ->
            retrievedOrigins = origins
        }

        assertEquals(2, retrievedOrigins?.size)
        assertTrue(retrievedOrigins?.contains("https://chatplayground.ai") == true)
        assertTrue(retrievedOrigins?.contains("https://google.com") == true)
    }

    @Test
    fun deleteOriginInvokesSeam() {
        var deletedOrigin: String? = null
        WebStorageManager.originDeleter = { origin ->
            deletedOrigin = origin
        }

        WebStorageManager.deleteOrigin("https://chatplayground.ai")

        assertEquals("https://chatplayground.ai", deletedOrigin)
    }

    @Test
    fun deleteAllDataInvokesSeam() {
        var allDeleted = false
        WebStorageManager.allDataDeleter = {
            allDeleted = true
        }

        WebStorageManager.deleteAllData()

        assertTrue(allDeleted)
    }
}
