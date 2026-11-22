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
import androidx.activity.OnBackPressedCallback
import androidx.browser.customtabs.CustomTabsIntent
import com.chatplayground.app.utils.CacheClearer
import com.chatplayground.app.utils.DeepLinkResolver
import com.chatplayground.app.utils.LinkRouter
import com.chatplayground.app.utils.UrlProvider
import com.chatplayground.app.utils.WebViewConfigurator
import com.chatplayground.app.utils.WebViewDebugging

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

        // Apply the app-wide WebView settings policy before any page loads:
        // JavaScript + DOM storage (the ChatPlayground SPA needs both),
        // mixed content never allowed, local file/content access disabled.
        WebViewConfigurator.configure(webView)

        // Expose chrome://inspect to debug builds only; a release build never
        // exposes its renderer or page state to an attached debugger.
        WebViewDebugging.configure(BuildConfig.DEBUG)

        // System Back walks the WebView history first, like a browser: only the
        // last Back press leaves the app. Without this, one tap on Back closes
        // the activity and discards the in-app navigation stack.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::webView.isInitialized && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

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
                // Non-web schemes are always delegated to the system.
                if (LinkRouter.isExternalScheme(url)) return handleUrl(url)
                // Keep script/redirect navigations (OAuth, form posts) inside the
                // WebView; only user-initiated off-domain links leave the app.
                if (!request.hasGesture()) return false
                return handleUrl(url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                progressBar.visibility = View.GONE
                when (errorCode) {
                    ERROR_HOST_LOOKUP, ERROR_CONNECT, ERROR_TIMEOUT, ERROR_FAILED_SSL_HANDSHAKE -> {
                        Toast.makeText(this@MainActivity, "Network error occurred. Showing offline page.", Toast.LENGTH_SHORT).show()
                        view?.loadUrl("file:///android_asset/offline.html")
                    }
                    else -> {
                        if (loadRetryPolicy.shouldRetry(true)) {
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
        // Load the deep link that launched us when it is a ChatPlayground URL,
        // otherwise fall back to the default start page.
        val startUrl = DeepLinkResolver.resolve(intent?.data?.toString())
            ?: UrlProvider.getChatPlaygroundUrl()
        webView.loadUrl(startUrl)

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
        // An Android WebView does not die with its activity: unless destroy() is called it
        // keeps the activity context and its renderer binding alive past the activity's life.
        // Detach first (a WebView destroyed while still attached is removed on the next
        // layout pass, which can touch an already-freed renderer), then destroy.
        if (::webView.isInitialized) {
            (webView.parent as? android.view.ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        super.onDestroy()
        if (::networkMonitor.isInitialized) {
            networkMonitor.stopObserving()
        }
    }

    /**
     * A deep link arrived while this activity was already running (the manifest
     * declares `launchMode="singleTop"`), so navigate the existing WebView
     * instead of stacking a second activity.
     */
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLinkUrl = DeepLinkResolver.resolve(intent.data?.toString()) ?: return
        if (!::webView.isInitialized) return
        loadRetryPolicy.reset()
        webView.loadUrl(deepLinkUrl)
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

    /**
     * Routes a WebView navigation. Returns `true` when the app consumed the URL
     * (Custom Tab or external app) and the WebView must not load it, `false`
     * when the WebView should navigate normally.
     */
    internal fun handleUrl(url: String): Boolean {
        if (LinkRouter.isInAppUrl(url)) return false

        if (LinkRouter.isExternalWebUrl(url)) {
            externalLinkLauncher(Uri.parse(url))
            return true
        }

        if (LinkRouter.isExternalScheme(url)) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                // No app can handle the scheme; swallow so the WebView does not crash.
            }
        }
        return true
    }

    /** Opens [uri] in a Custom Tab, falling back to a plain browser Intent. */
    private fun launchInCustomTab(uri: Uri) {
        try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(this, uri)
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (ignored: Exception) {
                Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    internal var externalLinkLauncher: (Uri) -> Unit = { launchInCustomTab(it) }

    private fun openInExternalBrowser(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }
}