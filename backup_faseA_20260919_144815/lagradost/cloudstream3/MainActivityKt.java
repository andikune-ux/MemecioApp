package com.lagradost.cloudstream3;

import com.lagradost.nicehttp.Requests;

/**
 * Stub CloudStream - MainActivityKt (C-2b-4 fix).
 * getApp() return singleton Requests.
 */
public class MainActivityKt {

    public MainActivityKt() {}

    private static final Requests APP = new Requests();

    /** Global HTTP client — sama dengan `app` di CloudStream */
    public static Requests getApp() {
        return APP;
    }
}
