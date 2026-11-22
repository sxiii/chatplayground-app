package com.chatplayground.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.chatplayground.app.utils.CacheClearer
import com.chatplayground.app.utils.DeepLinkResolver
import com.chatplayground.app.utils.LinkRouter
import com.chatplayground.app.utils.UrlProvider
import com.chatplayground.app.utils.WebViewConfigurator
import com.chatplayground.app.utils.WebViewDebugging

class MainActivity : AppCompatActivity() {
    internal lateinit var webView: WebView
    internal val loadRetryPolicy = WebViewLoadRetryPolicy(maxRetries = 3)
    internal lateinit var networkMonitor: NetworkMonitor

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

        // Resolve the default Custom Tabs provider package so external links
        // can be stamped onto a warm Custom Tab intent (device only; null under
        // Robolectric where no browser provider is registered).
        customTabsPackage = resolveCustomTabsPackage()

        // System Back walks the WebView history first, like a browser: only the
        // last Back press leaves the app. Without this, one tap on Back closes
        // the activity and discards the in-app navigation stack.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (::webView.isInitialized && webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onProgressChanged(
                    view: WebView?,
                    newProgress: Int,
                ) {
                    super.onProgressChanged(view, newProgress)
                    progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }

                override fun onReceivedTitle(
                    view: WebView?,
                    title: String?,
                ) {
                    super.onReceivedTitle(view, title)
                    this@MainActivity.title = title
                }
            }

        // Configure WebViewClient to keep navigation inside the app and
        // retry main-frame load failures with exponential backoff.
        webView.webViewClient =
            object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest,
                ): Boolean {
                    val url = request.url.toString()
                    // Non-web schemes are always delegated to the system.
                    if (LinkRouter.isExternalScheme(url)) return handleUrl(url)
                    // Keep script/redirect navigations (OAuth, form posts) inside the
                    // WebView; only user-initiated off-domain links leave the app.
                    if (!request.hasGesture()) return false
                    return handleUrl(url)
                }

                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: android.graphics.Bitmap?,
                ) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }

                @Suppress("DEPRECATION")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?,
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    progressBar.visibility = View.GONE
                    // minSdk is 24, so Android dispatches to the WebResourceRequest overload
                    // below; this deprecated branch is kept for compatibility and treats the
                    // error as a main-frame navigation failure.
                    val request =
                        object : WebResourceRequest {
                            override fun getUrl(): android.net.Uri = failingUrl?.let { android.net.Uri.parse(it) } ?: android.net.Uri.EMPTY

                            override fun isForMainFrame(): Boolean = true

                            override fun isRedirect(): Boolean = false

                            override fun hasGesture(): Boolean = false

                            override fun getMethod(): String = "GET"

                            override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
                        }
                    this@MainActivity.handleWebViewError(view ?: return, errorCode, request.isForMainFrame())
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?,
                ) {
                    super.onReceivedError(view, request, error)
                    progressBar.visibility = View.GONE

                    val code = error?.getErrorCode() ?: return
                    val isMainFrame = request?.isForMainFrame() ?: true

                    this@MainActivity.handleWebViewError(view ?: return, code, isMainFrame)
                }
            }
        // Load the deep link that launched us when it is a ChatPlayground URL,
        // otherwise fall back to the default start page.
        val startUrl =
            DeepLinkResolver.resolve(intent?.data?.toString())
                ?: UrlProvider.getChatPlaygroundUrl()
        webView.loadUrl(startUrl)

        networkMonitor = NetworkMonitor(this)
        networkMonitor.observe { isConnected ->
            // The ConnectivityManager.NetworkCallback fires on the connectivity
            // agent thread, so the whole decision (reading webView.url and
            // choosing whether to reload) must run on the UI thread, not just
            // the reload() call. Otherwise webView.url is read off-thread.
            runOnUiThread {
                if (isConnected && webView.url == "file:///android_asset/offline.html") {
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
            if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            },
        )
    }

    private fun toggleTheme() {
        val current = AppCompatDelegate.getDefaultNightMode()
        val newMode =
            if (current == AppCompatDelegate.MODE_NIGHT_YES) {
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

        // Blank or malformed URLs match no rule — log at debug level so
        // unsupported schemes are diagnosable instead of silently dropped.
        Log.d("MainActivity", "Unsupported URL dropped: $url")
        return true
    }

    /**
     * Handles a WebView load error. A failed sub-resource (an image, analytics
     * beacon, iframe, …) must not replace the whole page with the offline
     * fallback and must not show a network-error toast; only main-frame
     * navigations fall back. Extracted from the anonymous `WebViewClient` so it
     * can be exercised directly by unit tests.
     */
    internal fun handleWebViewError(
        view: WebView,
        code: Int,
        isMainFrame: Boolean,
    ) {
        if (!isMainFrame) return

        when (code) {
            WebViewClient.ERROR_HOST_LOOKUP,
            WebViewClient.ERROR_CONNECT,
            WebViewClient.ERROR_TIMEOUT,
            WebViewClient.ERROR_FAILED_SSL_HANDSHAKE,
            -> {
                Toast.makeText(this, "Network error occurred. Showing offline page.", Toast.LENGTH_SHORT).show()
                view.loadUrl("file:///android_asset/offline.html")
            }
            else -> {
                if (loadRetryPolicy.shouldRetry(true)) {
                    loadRetryPolicy.scheduleRetryWithBackoff {
                        view.reload()
                    }
                } else {
                    loadRetryPolicy.reset()
                }
            }
        }
    }

    /** Opens [uri] in a Custom Tab, falling back to a plain browser Intent. */
    private fun launchInCustomTab(uri: Uri) {
        try {
            val builder = CustomTabsIntent.Builder()
            // Theme the Custom Tab toolbar to match the app's theme colour so the
            // external chrome looks native rather than a default unthemed browser.
            builder.setToolbarColor(themePrimaryColor())
            val intent = builder.build()
            // Stamp the resolved provider package so the Intent targets a warm,
            // already-bound Custom Tabs client instead of a cold bind at click time.
            customTabsPackage?.let { intent.intent.setPackage(it) }
            launchCustomTabsIntent(intent, uri)
        } catch (e: Exception) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (ignored: Exception) {
                Toast.makeText(this, "No app can open this link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Resolves `colorPrimary` from the current theme (light or dark). */
    internal fun themePrimaryColor(): Int {
        val typedValue = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            typedValue.data
        } else {
            ContextCompat.getColor(this, R.color.primary)
        }
    }

    internal var externalLinkLauncher: (Uri) -> Unit = { launchInCustomTab(it) }

    /** Injectable seam: launches a built [CustomTabsIntent]. Defaults to a real launch. */
    internal var launchCustomTabsIntent: (CustomTabsIntent, Uri) -> Unit = { intent, uri ->
        intent.launchUrl(this@MainActivity, uri)
    }

    /** Package of the default Custom Tabs provider, resolved at runtime. */
    internal var customTabsPackage: String? = null

    /** The warm Custom Tabs client bound in `onStart`; released in `onStop`. */
    internal var customTabsClient: CustomTabsClient? = null

    /** True while the Custom Tabs service connection is bound (set in `onStart`). */
    internal var isCustomTabsBound: Boolean = false

    /** Resolves the default Custom Tabs provider package from the package manager. */
    private fun resolveCustomTabsPackage(): String? = CustomTabsClient.getPackageName(this, null)

    private fun openInExternalBrowser(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }
}
