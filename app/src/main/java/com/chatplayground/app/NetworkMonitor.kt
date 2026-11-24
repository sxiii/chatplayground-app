package com.chatplayground.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

/** Monitor网络连接状态，并提供实时回调。 */
class NetworkMonitor(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // 简单的连接状态存储
    @Volatile var isConnected: Boolean = checkConnectivity()

    /** Checks if the device is currently online. */
    fun isOnline(): Boolean = checkConnectivity()

    private var callbackObj: ConnectivityManager.NetworkCallback? = null

    /** Checks if the device is currently online. */
    fun checkConnectivity(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = cm.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(nw)
            if (actNw == null) return false
            actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }

    /** Register a callback to listen for online/offline transitions. */
    fun observe(callback: (Boolean) -> Unit) {
        // Unregister any previously registered callback first so repeated
        // calls (e.g. activity recreation) do not leak ConnectivityManager
        // NetworkCallback registrations that can no longer be unregistered.
        stopObserving()
        val callbackObj =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    publish(true)
                }

                override fun onLost(network: Network) {
                    publish(false)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    // Capabilities can change while the network stays up: a captive
                    // portal loses NET_CAPABILITY_VALIDATED without any onLost(), so
                    // re-read live state instead of trusting the cached value.
                    publish(checkConnectivity())
                }

                override fun onUnavailable() {
                    // registerDefaultNetworkCallback reports onUnavailable() when no
                    // default network can satisfy the request; without this override
                    // the monitor would keep reporting its last known state.
                    publish(false)
                }

                /**
                 * Stores [connected] and notifies the observer only when the value
                 * actually changed, so capability churn cannot spam the consumer.
                 */
                private fun publish(connected: Boolean) {
                    if (isConnected == connected) return
                    isConnected = connected
                    callback(connected)
                }
            }
        this.callbackObj = callbackObj
        cm.registerDefaultNetworkCallback(callbackObj)
    }

    /** Cancel a previous observation registration, releasing ConnectivityManager resources. */
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
