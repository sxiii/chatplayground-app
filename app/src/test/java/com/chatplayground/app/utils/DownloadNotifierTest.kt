package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadNotifierTest {
    @Test
    fun downloadStartedReturnsStartNotification() {
        val (title, body) = DownloadNotifier.downloadStarted("report.pdf")

        assertEquals("Download started", title)
        assertTrue("Body should name the file", body.contains("report.pdf"))
        assertTrue("Body should indicate the download is in progress", body.contains("Downloading"))
    }

    @Test
    fun downloadCompletedReturnsCompletionNotification() {
        val (title, body) = DownloadNotifier.downloadCompleted("report.pdf")

        assertEquals("Download complete", title)
        assertTrue("Body should name the file", body.contains("report.pdf"))
        assertTrue("Body should indicate completion", body.contains("complete"))
    }

    @Test
    fun downloadStartedBodyIncludesFileSizeWhenKnown() {
        val (_, body) = DownloadNotifier.downloadStarted("image.png", totalBytes = 1_048_576)

        assertTrue("Body should include file size when known", body.contains("1 MB"))
    }

    @Test
    fun downloadStartedOmitsFileSizeWhenUnknown() {
        val (_, body) = DownloadNotifier.downloadStarted("file.bin")

        assertTrue("Body should not contain a size placeholder when unknown", !body.contains("MB"))
    }
}
