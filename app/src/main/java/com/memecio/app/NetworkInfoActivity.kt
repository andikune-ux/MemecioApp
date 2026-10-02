package com.memecio.app

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.TextView

class NetworkInfoActivity : Activity() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_network_info)
        findViewById<View>(R.id.btnBackNetwork).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvNetworkContent).text = buildInfo()
    }

    private fun buildInfo(): String {
        val sb = StringBuilder()
        try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            sb.append("=== KONEKSI ===\n")
            val active = cm.activeNetwork
            if (active == null) {
                sb.append("Tidak ada koneksi aktif\n")
                return sb.toString()
            }
            val nc = cm.getNetworkCapabilities(active)
            if (nc != null) {
                sb.append("WiFi : ").append(if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) "Ya" else "Tidak").append("\n")
                sb.append("Cellular : ").append(if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) "Ya" else "Tidak").append("\n")
                sb.append("Ethernet : ").append(if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) "Ya" else "Tidak").append("\n")
                sb.append("VPN : ").append(if (nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) "Ya" else "Tidak").append("\n")
                sb.append("Validated : ").append(if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) "Ya" else "Tidak").append("\n")
                sb.append("Down : ").append(nc.linkDownstreamBandwidthKbps).append(" Kbps\n")
                sb.append("Up : ").append(nc.linkUpstreamBandwidthKbps).append(" Kbps\n\n")
            }
            val lp = cm.getLinkProperties(active)
            if (lp != null) {
                sb.append("=== LINK ===\n")
                sb.append("Interface : ").append(lp.interfaceName).append("\n")
                sb.append("DNS : ").append(lp.dnsServers).append("\n")
                sb.append("Domain : ").append(lp.domains).append("\n")
                sb.append("IP Lokal :\n")
                for (la in lp.linkAddresses) {
                    sb.append("  ").append(la.address.hostAddress).append("/").append(la.prefixLength).append("\n")
                }
            }
        } catch (e: Exception) {
            sb.append("\nError: ").append(e.message)
        }
        return sb.toString()
    }
}
