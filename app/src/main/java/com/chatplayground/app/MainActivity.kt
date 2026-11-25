package com.chatplayground.app

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
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
import com.chatplayground.app.utils.AssetRequestHandler
import com.chatplayground.app.utils.ContentFilterRequestHandler
import com.chatplayground.app.utils.DataClearer
import com.chatplayground.app.utils.DeepLinkResolver
import com.chatplayground.app.utils.GestureNavigationHandler
import com.chatplayground.app.utils.GestureNavigationTarget
import com.chatplayground.app.utils.LinkRouter
import com.chatplayground.app.utils.NavigationRetryPolicy
import com.chatplayground.app.utils.UrlProvider
import com.chatplayground.app.utils.WebViewConfigurator
import com.chatplayground.app.utils.WebViewDebugging

class MainActivity : AppCompatActivity(), GestureNavigationTarget {
    internal lateinit var webView: WebView
    internal val navigationRetryPolicy = NavigationRetryPolicy(maxRetries = 3)
    internal var gestureNavigationHandler =
        GestureNavigationHandler(
            target = this,
            onSwipeRight = { if (::webView.isInitialized) webView.goBack() },
            onSwipeLeft = { if (::webView.isInitialized) webView.goForward() },
        )

    override fun canGoBack(): Boolean = webView.canGoBack()

    override fun canGoForward(): Boolean = webView.canGoForward()

    override fun canContentScrollHorizontally(): Boolean = false

    private lateinit var gestureDetector: GestureDetector

    private val retryHandler = Handler(Looper.getMainLooper())

    internal lateinit var networkMonitor: NetworkMonitor
    internal var lastTargetUrl: String = ""

    /**
     * Horizontal flings are routed to [gestureNavigationHandler] before the
     * WebView sees the event. Every event is still dispatched normally, so
     * taps, link clicks, text selection and vertical scrolling are unchanged.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (::gestureDetector.isInitialized) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * True when the current main-frame navigation ended in an error. The
     * platform delivers `onPageFinished` for failed navigations too, so the
     * retry budget may only be restored when no error was reported for the
     * attempt that just finished.
     */
    private var currentLoadFailed: Boolean = false

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

        // Right swipe = back, left swipe = forward, like a native browser. The
        // detector only observes; `dispatchTouchEvent` still delivers every
        // event to the WebView so page interaction is untouched.
        gestureDetector =
            GestureDetector(
                this,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onFling(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float,
                    ): Boolean {
                        if (e1 == null) return false
                        return gestureNavigationHandler.handleFling(
                            startX = e1.x,
                            startY = e1.y,
                            endX = e2.x,
                            endY = e2.y,
                            velocityX = velocityX,
                            velocityY = velocityY,
                        )
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
                    request: WebResourceRequest,
                ): Boolean {
                    val url = request.url.toString()
                    // Non-web schemes are always delegated to the system.
                    if (LinkRouter.isExternalScheme(url)) return handleUrl(url)
                    // Keep script/redirect navigations (OAuth, form posts) inside the
                    // WebView; only user-initiated off-domain links leave the app.
                    if (!request.hasGesture()) return false
                    return handleUrl(url)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest,
                ): android.webkit.WebResourceResponse? {
                    val url = request.url.toString()
                    return this@MainActivity.shouldInterceptRequest(view ?: return null, url)
                }

                @Suppress("DEPRECATION")
                override fun shouldInterceptRequest(
                    view: WebView?,
                    url: String?,
                ): android.webkit.WebResourceResponse? {
                    return this@MainActivity.shouldInterceptRequest(view ?: return null, url ?: return null)
                }

                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: Bitmap?,
                ) {
                    super.onPageStarted(view, url, favicon)
                    // A new navigation starts with no error recorded yet; the
                    // flag is set again below if this attempt fails.
                    currentLoadFailed = false
                    progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    // Only a committed load ends the retry episode. The
                    // platform also delivers onPageFinished for a navigation
                    // that failed (after onReceivedError), so reset
                    // unconditionally would re-arm a permanently failing page.
                    if (!currentLoadFailed) {
                        navigationRetryPolicy.reset()
                    }
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
                            override fun getUrl(): Uri = failingUrl?.let { Uri.parse(it) } ?: Uri.EMPTY

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

                    val code = error?.errorCode ?: return
                    val isMainFrame = request?.isForMainFrame() ?: true

                    this@MainActivity.handleWebViewError(view ?: return, code, isMainFrame)
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: android.net.http.SslError?,
                ) {
                    // Secure default policy: cancel the SSL connection on any
                    // certificate error. Proceeding with an insecure connection
                    // would expose the user to MITM attacks, so the handler is
                    // never allowed to continue. The default WebViewClient
                    // implementation proceeds (insecure), so super() is
                    // deliberately NOT called. Log the error so failures are
                    // diagnosable without weakening the security boundary.
                    Log.w(
                        "MainActivity",
                        "SSL error (${error?.primaryError}) for ${error?.url} — cancelling connection",
                    )
                    handler?.cancel()
                }
            }
        // Load the deep link that launched us when it is a ChatPlayground URL,
        // otherwise fall back to the default start page.
        val startUrl =
            DeepLinkResolver.resolve(intent?.data?.toString())
                ?: UrlProvider.getChatPlaygroundUrl()
        lastTargetUrl = startUrl
        webView.loadUrl(startUrl)

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType),
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, "Downloading file...", Toast.LENGTH_SHORT).show()
        }

        networkMonitor = NetworkMonitor(this)
        networkMonitor.observe { isConnected ->
            // The ConnectivityManager.NetworkCallback fires on the connectivity
            // agent thread, so the whole decision (reading webView.url and
            // choosing whether to reload) must run on the UI thread, not just
            // the reload() call. Otherwise webView.url is read off-thread.
            runOnUiThread {
                if (isConnected && webView.url?.contains("offline.html") == true) {
                    if (lastTargetUrl.isNotEmpty()) {
                        webView.loadUrl(lastTargetUrl)
                    } else {
                        webView.reload()
                    }
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
            (webView.parent as? ViewGroup)?.removeView(webView)
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
        navigationRetryPolicy.reset()
        lastTargetUrl = deepLinkUrl
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
            R.id.action_clear_all_data -> {
                DataClearer.clearAllData(this, webView)
                Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_toggle_theme -> {
                showThemeSelectionDialog()
                true
            }
            R.id.action_manage_cookies -> {
                showCookieManagementDialog()
                true
            }
            R.id.action_about -> {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("ChatPlayground Android App\nVersion 1.0")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val mode =
            if (prefs.contains("theme_mode")) {
                prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else if (prefs.contains("dark_mode")) {
                if (prefs.getBoolean("dark_mode", false)) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            } else {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showThemeSelectionDialog() {
        val items = arrayOf("System Default", "Light", "Dark")
        val prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val checkedItem =
            when (currentMode) {
                AppCompatDelegate.MODE_NIGHT_NO -> 1
                AppCompatDelegate.MODE_NIGHT_YES -> 2
                else -> 0
            }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                val newMode =
                    when (which) {
                        1 -> AppCompatDelegate.MODE_NIGHT_NO
                        2 -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                prefs.edit().putInt("theme_mode", newMode).apply()
                AppCompatDelegate.setDefaultNightMode(newMode)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showCookieManagementDialog() {
        val cookieManager = android.webkit.CookieManager.getInstance()
        val hasCookies = cookieManager.hasCookies()
        val message = if (hasCookies) "Cookies exist in storage." else "No cookies currently stored."

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Manage Cookies")
            .setMessage(message)
            .setPositiveButton("Clear All Cookies") { dialog, _ ->
                cookieManager.removeAllCookies(null)
                cookieManager.flush()
                Toast.makeText(this, "All cookies cleared", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
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

        // Remember that this navigation failed so onPageFinished — which the
        // platform also sends for failed loads — does not restore the budget.
        currentLoadFailed = true

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
                if (navigationRetryPolicy.shouldRetry(true)) {
                    retryHandler.postDelayed({
                        view.reload()
                    }, navigationRetryPolicy.backoffDelayMs())
                }
                // A spent budget stays spent until a page commits
                // (onPageFinished resets it). Resetting here would re-arm a
                // permanently failing page and let it retry without limit,
                // defeating maxRetries.
            }
        }
    }

    /**
     * Serves a bundled asset (the offline fallback page and any other
     * whitelisted asset) straight from the APK instead of letting the WebView
     * go through its normal asset/resource loader.
     *
     * Returns `null` for every URL that is not a whitelisted bundled asset —
     * remote URLs and unknown asset names keep loading normally.
     */
     @Suppress("DEPRECATION")
     internal fun shouldInterceptRequest(
         view: WebView?,
         url: String?,
     ): android.webkit.WebResourceResponse? {
         // Block known ad/tracker hosts before anything else so they never
         // reach the network.
         contentFilterResponse(url)?.let { return it }
         // Serve bundled assets first (offline page, etc.).
         val assetPath = AssetRequestHandler.assetPathFor(url) ?: return null
         return try {
             val inputStream = assets.open(assetPath)
             android.webkit.WebResourceResponse(
                 mimeTypeFor(assetPath),
                 "UTF-8",
                 inputStream,
             )
         } catch (e: java.io.IOException) {
             // The asset is whitelisted but missing from the APK — fall back to
             // the WebView's normal loading path rather than crashing.
             Log.w("MainActivity", "Failed to open bundled asset: $assetPath", e)
             null
         }
     }

     /** Blocks requests to known ad/tracker hosts by returning an empty
      * response, so they never reach the network. Returns null when the URL is
      * not blocked and should load normally. */
     private fun contentFilterResponse(url: String?): android.webkit.WebResourceResponse? {
         return if (ContentFilterRequestHandler.shouldIntercept(url)) {
             android.webkit.WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream(emptyByteArray()))
         } else null
     }


    /** Selects the MIME type for a bundled asset by its file extension. */
    private fun mimeTypeFor(assetPath: String): String {
        return when (assetPath.substringAfterLast('.', "").lowercase()) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "svg" -> "image/svg+xml"
            "ico" -> "image/x-icon"
            else -> "*/*"
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

    /** Resolves the default Custom Tabs provider package from the package manager. */
    private fun resolveCustomTabsPackage(): String? = CustomTabsClient.getPackageName(this, null)

    private fun openInExternalBrowser(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }

    internal fun findText(query: String) {
        webView.findAllAsync(query)
    }

    internal fun findNext() {
        webView.findNext(true)
    }
}
