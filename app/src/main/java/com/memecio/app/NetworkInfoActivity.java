package com.memecio.app;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetAddress;

public class NetworkInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_network_info);
        findViewById(R.id.btnBackNetwork).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvNetworkContent)).setText(buildInfo());
    }

    private String buildInfo() {
        StringBuilder sb = new StringBuilder();
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            sb.append("=== KONEKSI ===\n");
            if (cm == null) { sb.append("ConnectivityManager tidak tersedia\n"); return sb.toString(); }

            Network active = cm.getActiveNetwork();
            if (active == null) { sb.append("Tidak ada koneksi aktif\n"); return sb.toString(); }

            NetworkCapabilities nc = cm.getNetworkCapabilities(active);
            if (nc != null) {
                sb.append("WiFi      : ").append(nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "Ya" : "Tidak").append("\n");
                sb.append("Cellular  : ").append(nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "Ya" : "Tidak").append("\n");
                sb.append("Ethernet  : ").append(nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ? "Ya" : "Tidak").append("\n");
                sb.append("VPN       : ").append(nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ? "Ya" : "Tidak").append("\n");
                sb.append("Validated : ").append(nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ? "Ya" : "Tidak").append("\n");
                sb.append("Down      : ").append(nc.getLinkDownstreamBandwidthKbps()).append(" Kbps\n");
                sb.append("Up        : ").append(nc.getLinkUpstreamBandwidthKbps()).append(" Kbps\n\n");
            }

            LinkProperties lp = cm.getLinkProperties(active);
            if (lp != null) {
                sb.append("=== LINK ===\n");
                sb.append("Interface : ").append(lp.getInterfaceName()).append("\n");
                sb.append("DNS       : ").append(lp.getDnsServers()).append("\n");
                sb.append("Domain    : ").append(lp.getDomains()).append("\n");
                sb.append("IP Lokal  :\n");
                for (android.net.LinkAddress la : lp.getLinkAddresses()) {
                    sb.append("  ").append(la.getAddress().getHostAddress()).append("/").append(la.getPrefixLength()).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("\nError: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
