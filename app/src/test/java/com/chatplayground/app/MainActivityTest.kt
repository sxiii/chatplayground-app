package com.chatplayground.app

import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import com.chatplayground.app.utils.UrlProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    @Test
    fun urlProviderReturnsChatPlaygroundUrl() {
        assertEquals("https://chatplayground.ai", UrlProvider.getChatPlaygroundUrl())
    }

    @Test
    fun activityConfiguresWebViewClient() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()

        assertNotNull("WebView should be created in onCreate", webView)
        assertNotNull("WebViewClient should be set", webView.webViewClient)
        assertTrue("WebViewClient should be set", webView.webViewClient != null)
    }

    @Test
    fun activityLoadsChatPlaygroundUrl() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        assertEquals(
            "MainActivity should load the ChatPlayground URL into its WebView",
            "https://chatplayground.ai",
            shadowOf(activity.getWebView()).lastLoadedUrl,
        )
    }

    @Test
    fun activityLoadsOfflinePageOnNetworkError() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val webViewClient = webView.webViewClient

        webViewClient?.onReceivedError(
            webView,
            android.webkit.WebViewClient.ERROR_HOST_LOOKUP,
            "Failed to resolve host",
            "https://chatplayground.ai",
        )

        assertEquals(
            "MainActivity should load the offline fallback page on network error",
            "file:///android_asset/offline.html",
            shadowOf(webView).lastLoadedUrl,
        )
    }

    @Test
    fun activityUpdatesTitleWhenWebViewTitleChanges() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val chromeClient = webView.webChromeClient

        val newTitle = "ChatPlayground - Home"
        chromeClient?.onReceivedTitle(webView, newTitle)

        assertEquals("ChatPlayground - Home", activity.title)
    }

    private fun requestFor(
        url: String,
        hasGesture: Boolean = false,
    ): WebResourceRequest {
        return object : WebResourceRequest {
            override fun getUrl(): android.net.Uri = android.net.Uri.parse(url)

            override fun isForMainFrame(): Boolean = true

            override fun isRedirect(): Boolean = false

            override fun hasGesture(): Boolean = hasGesture

            override fun getMethod(): String = "GET"

            override fun getRequestHeaders(): MutableMap<String, String> = mutableMapOf()
        }
    }

    @Test
    fun webViewInterceptsNonHttpSchemes() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val handled =
            webView.webViewClient?.shouldOverrideUrlLoading(
                webView,
                requestFor("mailto:support@chatplayground.ai"),
            )

        assertTrue("WebViewClient should intercept non-HTTP schemes like mailto:", handled ?: false)
    }

    @Test
    fun webViewAllowsHttpHttpsSchemes() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val webView = activity.getWebView()
        val handled =
            webView.webViewClient?.shouldOverrideUrlLoading(
                webView,
                requestFor("https://chatplayground.ai/about"),
            )

        assertFalse("WebViewClient should NOT intercept HTTP/HTTPS schemes", handled ?: false)
    }

    @Test
    fun externalHttpLinkOpensInCustomTabInsteadOfWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val opened = mutableListOf<android.net.Uri>()
        activity.externalLinkLauncher = { opened.add(it) }
        val webView = activity.getWebView()

        val handled =
            webView.webViewClient?.shouldOverrideUrlLoading(
                webView,
                requestFor("https://github.com/sxiii/chatplayground-app", hasGesture = true),
            )

        assertTrue("Off-domain links must be intercepted by the app", handled ?: false)
        assertEquals(
            "Off-domain links must be handed to the Custom Tab launcher",
            listOf(android.net.Uri.parse("https://github.com/sxiii/chatplayground-app")),
            opened,
        )
    }

    @Test
    fun inAppLinkDoesNotOpenCustomTab() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val opened = mutableListOf<android.net.Uri>()
        activity.externalLinkLauncher = { opened.add(it) }
        val webView = activity.getWebView()

        val handled =
            webView.webViewClient?.shouldOverrideUrlLoading(
                webView,
                requestFor("https://chatplayground.ai/pricing"),
            )

        assertFalse("In-app links must stay in the WebView", handled ?: false)
        assertTrue("In-app links must not open a Custom Tab", opened.isEmpty())
    }

    @Test
    fun inAppLinkClickedByUserStaysInWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val opened = mutableListOf<android.net.Uri>()
        activity.externalLinkLauncher = { opened.add(it) }
        val webView = activity.getWebView()

        val handled =
            webView.webViewClient?.shouldOverrideUrlLoading(
                webView,
                requestFor("https://chatplayground.ai/pricing", hasGesture = true),
            )

        assertFalse("In-app links must stay in the WebView", handled ?: false)
        assertTrue("In-app links must not open a Custom Tab", opened.isEmpty())
    }

    @Test
    fun offDomainRedirectWithoutUserGestureStaysInWebView() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val opened = mutableListOf<android.net.Uri>()
        activity.externalLinkLauncher = { opened.add(it) }
        val webView = activity.getWebView()

        val handled =
            webView.webViewClient?.shouldOverrideUrlLoading(
                webView,
                requestFor("https://accounts.google.com/o/oauth2/auth", hasGesture = false),
            )

        assertFalse(
            "Scripted/redirect navigation (e.g. OAuth) must stay in the WebView",
            handled ?: false,
        )
        assertTrue("Scripted navigation must not launch a Custom Tab", opened.isEmpty())
    }

    @Test
    fun handleUrlLogsDebugForUnsupportedUrls() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        ShadowLog.reset()
        try {
            assertTrue("handleUrl must consume the unsupported URL", activity.handleUrl(""))
            assertTrue(
                "handleUrl should log an unsupported URL at debug level",
                ShadowLog.getLogsForTag("MainActivity").any { it.msg.contains("Unsupported URL") },
            )
        } finally {
            ShadowLog.reset()
        }
    }

    @Test
    fun webViewSavesAndRestoresNavigationState() {
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .create()
                .start()
                .resume()
        val activity = controller.get()
        val webView = activity.getWebView()

        shadowOf(webView).pushEntryToHistory("https://chatplayground.ai/page1")
        shadowOf(webView).pushEntryToHistory("https://chatplayground.ai/page2")

        val bundle = android.os.Bundle()
        controller.saveInstanceState(bundle).pause().stop().destroy()

        val newController =
            Robolectric.buildActivity(MainActivity::class.java)
                .create(bundle)
                .start()
                .restoreInstanceState(bundle)
                .resume()
        val newActivity = newController.get()
        val newWebView = newActivity.getWebView()

        assertEquals(
            "WebView should restore history current URL",
            "https://chatplayground.ai/page2",
            newWebView.url,
        )
        assertTrue("WebView should be able to go back after restore", newWebView.canGoBack())
    }

    @Test
    fun backPressNavigatesWebViewHistoryInsteadOfFinishingActivity() {
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .create()
                .start()
                .resume()
        val activity = controller.get()
        val webView = activity.getWebView()
        val shadowWebView = shadowOf(webView)
        shadowWebView.pushEntryToHistory("https://chatplayground.ai/")
        shadowWebView.pushEntryToHistory("https://chatplayground.ai/pricing")
        assertTrue("Precondition: the WebView must have a back entry", webView.canGoBack())

        activity.onBackPressedDispatcher.onBackPressed()

        assertEquals(
            "Back must navigate the WebView history instead of closing the activity",
            1,
            shadowWebView.goBackInvocations,
        )
        assertEquals(
            "Back must return the WebView to the previous page",
            "https://chatplayground.ai/",
            webView.url,
        )
        assertFalse(
            "The activity must stay open while WebView history remains",
            activity.isFinishing,
        )
    }

    @Test
    fun backPressFinishesActivityWhenWebViewHasNoHistory() {
        val controller =
            Robolectric.buildActivity(MainActivity::class.java)
                .create()
                .start()
                .resume()
        val activity = controller.get()
        val webView = activity.getWebView()
        val shadowWebView = shadowOf(webView)
        shadowWebView.pushEntryToHistory("https://chatplayground.ai/")
        assertFalse("Precondition: a single history entry is not a back entry", webView.canGoBack())

        activity.onBackPressedDispatcher.onBackPressed()

        assertEquals(
            "Back must not navigate when there is no history",
            0,
            shadowWebView.goBackInvocations,
        )
        assertTrue(
            "The activity must finish when the WebView has no back entry",
            activity.isFinishing,
        )
    }

    @Test
    fun aboutMenuItemShowsAboutDialog() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val menu = org.robolectric.fakes.RoboMenu(activity)
        activity.onCreateOptionsMenu(menu)
        val item = menu.findItem(R.id.action_about)

        activity.onOptionsItemSelected(item)

        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        assertNotNull("About dialog should be shown", dialog)
    }

    @Test
    fun clearCacheMenuItemClearsCache() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val menu = org.robolectric.fakes.RoboMenu(activity)
        activity.onCreateOptionsMenu(menu)
        val item = menu.findItem(R.id.action_clear_cache)

        val handled = activity.onOptionsItemSelected(item)
        assertTrue("Clear cache action should be handled", handled)
    }

    @Test
    fun themeSelectionDialogShowsOptions() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        val menu = org.robolectric.fakes.RoboMenu(activity)
        activity.onCreateOptionsMenu(menu)
        val item = menu.findItem(R.id.action_toggle_theme)

        val handled = activity.onOptionsItemSelected(item)
        assertTrue("Toggle theme action should be handled", handled)

        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        assertNotNull("Theme selection dialog should be shown", dialog)
    }
}
