package com.chatplayground.app.utils

/** Produces human-readable download start/completion notification text. */
object DownloadNotifier {

    private val FORMATTER = java.text.DecimalFormat("#.##")

    /** Returns `(title, body)` for a download-started notification. */
    fun downloadStarted(fileName: String, totalBytes: Long = -1): Pair<String, String> {
        val title = "Download started"
        val body = buildString {
            append("Downloading ")
            append(fileName)
            if (totalBytes > 0L) {
                append(" (").append(FORMATTER.format(totalBytes / 1024.0 / 1024.0)).append(" MB)")
            }
        }
        return Pair(title, body)
    }

    /** Returns `(title, body)` for a download-completed notification. */
    fun downloadCompleted(fileName: String): Pair<String, String> {
        return Pair("Download complete", "$fileName download complete")
    }
}
