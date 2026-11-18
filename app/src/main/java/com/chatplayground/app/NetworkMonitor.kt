package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Monitor网络连接状态，并提供实时回调。
 */
class NetworkMonitor(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // 简单的连接状态存储
    @Volatile var isConnected: Boolean = checkConnectivity()

    private var callbackObj: ConnectivityManager.NetworkCallback? = null

    fun checkConnectivity(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = cm.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(nw) ?: return false
            actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }

    /**
     * 注册一个回调以监听在线/离线转换。
     */
    fun observe(callback: (Boolean) -> Unit) {
        val callbackObj = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
                callback(true)
            }

            override fun onLost(network: Network) {
                isConnected = false
                callback(false)
            }
        }
        this.callbackObj = callbackObj
        cm.registerDefaultNetworkCallback(callbackObj)
    }

    /**
     * 注销之前注册的回调，释放 ConnectivityManager 资源。
     * 即使没有注册过的回调也不会抛出异常。
     */
    fun stopObserving() {
        val cb = callbackObj ?: return
        callbackObj = null
        try {
            cm.unregisterNetworkCallback(cb)
        } catch (e: Exception) {
            // Best-effort cleanup: some platform versions throw if the
            // callback was already unregistered.
        }
    }
}
