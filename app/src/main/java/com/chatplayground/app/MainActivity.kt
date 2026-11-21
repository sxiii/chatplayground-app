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
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.chatplayground.app.utils.CacheClearer
import com.chatplayground.app.utils.UrlProvider

class MainActivity : AppCompatActivity() {
    internal lateinit var webView: WebView
    internal val loadRetryPolicy = WebViewLoadRetryPolicy(maxRetries = 3)
    private lateinit var networkMonitor: NetworkMonitor

    fun getWebView(): WebView = webView

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
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
                        Toast.makeText(this@MainActivity, "Network error occurred. Showing offline page.", Toast.LENGTH_SHORT).show()
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

        networkMonitor = NetworkMonitor(this)
        networkMonitor.observe { isConnected ->
            if (isConnected && webView.url == "file:///android_asset/offline.html") {
                runOnUiThread {
                    webView.reload()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::networkMonitor.isInitialized) {
            networkMonitor.stopObserving()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        // Add theme toggle button to the action bar
        addThemeToggle(menu)
        return true
    }

    private fun addThemeToggle(menu: Menu) {
        val toggleItem = menu.findItem(R.id.action_toggle_theme)
        if (toggleItem != null) {
            val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            toggleItem.setIcon(if (isDark) R.drawable.ic_sun else R.drawable.ic_moon)
        }
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
            R.id.action_toggle_theme -> {
                toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun toggleTheme() {
        val current = AppCompatDelegate.getDefaultNightMode()
        val newMode = if (current == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(newMode)
        
        // Save preference
        val prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", newMode == AppCompatDelegate.MODE_NIGHT_YES).apply()
        
        // Recreate activity to apply theme
        recreate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    private fun openInExternalBrowser(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }
}