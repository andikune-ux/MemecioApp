package com.memecio.app

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import java.net.Inet4Address
import java.net.NetworkInterface

class NetworkInfoActivity : Activity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_info)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnRefreshNetwork).setOnClickListener {
            populateInfo()
        }

        container = findViewById(R.id.llNetworkInfoContainer)
        populateInfo()
    }

    private fun populateInfo() {
        container.removeAllViews()

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNet = cm.activeNetwork
        val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null

        val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        val netType = when {
            isWifi -> "Wi-Fi (Wireless LAN)"
            isCellular -> "Jaringan Seluler (Mobile Data)"
            isVpn -> "Jaringan VPN Terproteksi"
            isConnected -> "Koneksi Jaringan Lain"
            else -> "Tidak Terhubung (Offline)"
        }

        val ipAddress = getLocalIpAddress()

        val items = listOf(
            "Status Internet" to if (isConnected) "🟢 Terhubung ke Internet" else "🔴 Tidak Terhubung",
            "Tipe Jaringan Aktif" to netType,
            "Alamat IP Lokal (IPv4)" to (ipAddress ?: "Tidak diketahui"),
            "Bandwidth Perkiraan (Downlink)" to if (caps != null) "${caps.linkDownstreamBandwidthKbps / 1000} Mbps" else "-",
            "Bandwidth Perkiraan (Uplink)" to if (caps != null) "${caps.linkUpstreamBandwidthKbps / 1000} Mbps" else "-",
            "Jaringan Metered (Hemat Data)" to if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true) "Bebas Kuota (Unmetered)" else "Berbayar / Berkuota (Metered)"
        )

        for ((title, desc) in items) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_glass_card)
                setPadding(32, 24, 32, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                layoutParams = params
            }

            val tvTitle = TextView(this).apply {
                text = title
                textSize = 14f
                setTextColor(0xFF3F51B5.toInt())
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val tvDesc = TextView(this).apply {
                text = desc
                textSize = 13f
                setTextColor(0xFF222222.toInt())
                setPadding(0, 6, 0, 0)
            }

            card.addView(tvTitle)
            card.addView(tvDesc)
            container.addView(card)
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ignored: Exception) {}
        return null
    }
}
