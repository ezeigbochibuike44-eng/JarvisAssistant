package com.jarvis.assistant.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

data class NetworkStatus(
    val isConnected: Boolean,
    val transport: String,
)

class NetworkMonitor(private val context: Context) {

    fun currentStatus(): NetworkStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return NetworkStatus(false, "none")
        val capabilities = cm.getNetworkCapabilities(network) ?: return NetworkStatus(false, "none")

        val transport = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }
        val connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return NetworkStatus(connected, transport)
    }
}
