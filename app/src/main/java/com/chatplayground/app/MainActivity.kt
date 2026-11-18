package com.chatplayground.app

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    internal lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        // Configure WebViewClient to keep navigation inside the app
        webView.webViewClient = android.webkit.WebViewClient()
        // Load URL from UrlProvider
        val url = com.chatplayground.app.utils.UrlProvider.getChatPlaygroundUrl()
        webView.loadUrl(url)
        setContentView(webView)
    }
}
