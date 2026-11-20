package com.chatplayground.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.ProgressBar
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chatplayground.app.utils.CacheClearer
import com.chatplayground.app.utils.UrlProvider

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

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                this@MainActivity.title = title
            }
        }

        // Configure WebViewClient to keep navigation inside the app and
        // retry main-frame load failures with exponential backoff.
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false
                }

                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fail silently if no app can handle the scheme
                }
                return true
            }

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
        val url = UrlProvider.getChatPlaygroundUrl()
        webView.loadUrl(url)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_external_browser -> {
                openInExternalBrowser(webView.url)
                true
            }
            R.id.action_clear_cache -> {
                CacheClearer.clearAll(this, webView)
                Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openInExternalBrowser(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }
}