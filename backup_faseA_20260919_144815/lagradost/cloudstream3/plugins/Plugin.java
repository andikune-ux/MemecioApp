package com.lagradost.cloudstream3.plugins;

import android.content.Context;
import com.lagradost.cloudstream3.MainAPI;

/**
 * Stub CloudStream - Plugin (C-2b-2 fix).
 *
 * Field PUBLIC supaya plugin bisa akses langsung (pola umum CloudStream).
 * 3 nama field alternatif: api, mainApi, mainPlugin.
 */
public class Plugin {

    // Public fields — plugin bisa langsung akses
    public MainAPI api;
    public MainAPI mainApi;
    public MainAPI mainPlugin;

    // Getters
    public MainAPI getApi() { return firstNonNull(); }
    public MainAPI getMainApi() { return firstNonNull(); }
    public MainAPI getMainPlugin() { return firstNonNull(); }

    // Setters
    public void setApi(MainAPI a) { this.api = a; }
    public void setMainApi(MainAPI a) { this.mainApi = a; }
    public void setMainPlugin(MainAPI a) { this.mainPlugin = a; }

    public void load(Context context) {}
    public void load() {}

    /** Return field yang paling tidak null */
    public MainAPI firstNonNull() {
        if (api != null) return api;
        if (mainApi != null) return mainApi;
        if (mainPlugin != null) return mainPlugin;
        return null;
    }
}
