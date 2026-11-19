package com.chatplayground.app

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    internal lateinit var webView: WebView
    internal val loadRetryPolicy = WebViewLoadRetryPolicy(maxRetries = 3)

    fun getWebView(): WebView = webView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        // Configure WebViewClient to keep navigation inside the app and
        // retry main-frame load failures with exponential backoff.
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if (loadRetryPolicy.shouldRetry(true)) {
                    loadRetryPolicy.recordRetry()
                    loadRetryPolicy.scheduleRetryWithBackoff {
                        view?.reload()
                    }
                } else {
                    loadRetryPolicy.reset()
                }
            }
        }
        // Load URL from UrlProvider
        val url = com.chatplayground.app.utils.UrlProvider.getChatPlaygroundUrl()
        webView.loadUrl(url)
        setContentView(webView)
    }
}
