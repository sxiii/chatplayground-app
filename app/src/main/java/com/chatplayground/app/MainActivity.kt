package com.chatplayground.app

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    internal lateinit var webView: WebView
    internal val loadRetryPolicy = WebViewLoadRetryPolicy(maxRetries = 3)

    fun getWebView(): WebView = webView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        val progressBar = findViewById<ProgressBar>(R.id.loading_progress)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }

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
                when (errorCode) {
                    ERROR_HOST_LOOKUP, ERROR_CONNECT, ERROR_TIMEOUT, ERROR_FAILED_SSL_HANDSHAKE -> {
                        view?.loadUrl("file:///android_asset/offline.html")
                    }
                    else -> {
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
            }
        }
        // Load URL from UrlProvider
        val url = com.chatplayground.app.utils.UrlProvider.getChatPlaygroundUrl()
        webView.loadUrl(url)
    }
}
