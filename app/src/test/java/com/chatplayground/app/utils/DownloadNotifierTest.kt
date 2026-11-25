package com.chatplayground.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadNotifierTest {
    @Test
    fun downloadStartedReturnsStartNotification() {
        val notifier = DownloadNotifier()
        val (title, body) = notifier.downloadStarted("report.pdf")

        assertEquals("Download started", title)
        assertTrue("Body should name the file", body.contains("report.pdf"))
        assertTrue("Body should indicate the download is in progress", body.contains("Downloading"))
    }

    @Test
    fun downloadCompletedReturnsCompletionNotification() {
        val notifier = DownloadNotifier()
        val (title, body) = notifier.downloadCompleted("report.pdf")

        assertEquals("Download complete", title)
        assertTrue("Body should name the file", body.contains("report.pdf"))
        assertTrue("Body should indicate completion", body.contains("complete"))
    }

    @Test
    fun downloadStartedBodyIncludesFileSizeWhenKnown() {
        val notifier = DownloadNotifier()
        val (_, body) = notifier.downloadStarted("image.png", totalBytes = 1_048_576)

        assertTrue("Body should include file size when known", body.contains("1 MB"))
    }

    @Test
    fun downloadStartedOmitsFileSizeWhenUnknown() {
        val notifier = DownloadNotifier()
        val (_, body) = notifier.downloadStarted("file.bin")

        assertTrue("Body should not contain a size placeholder when unknown", !body.contains("MB"))
    }
}
