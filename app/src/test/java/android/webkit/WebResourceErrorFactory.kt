package android.webkit

/**
 * [WebResourceError] is abstract with an inaccessible no-arg constructor from
 * outside the `android.webkit` package, so tests in `com.chatplayground.app`
 * cannot construct one directly. This same-package helper exposes a concrete
 * subclass and a factory so unit tests can drive the
 * `WebViewClient.onReceivedError(WebView, WebResourceRequest, WebResourceError)`
 * overload end to end.
 */
class TestWebResourceError(
    private val errorCode: Int,
    private val description: String,
) : WebResourceError() {
    override fun getErrorCode(): Int = errorCode

    override fun getDescription(): CharSequence = description
}

fun webResourceError(
    errorCode: Int,
    description: String,
): WebResourceError =
    TestWebResourceError(errorCode, description)
