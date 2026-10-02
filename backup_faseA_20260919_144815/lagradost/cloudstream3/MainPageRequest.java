package com.lagradost.cloudstream3;

/** Stub CloudStream - MainPageRequest (C-2a). */
public class MainPageRequest {
    public String name;
    public String data;
    public boolean horizontalImages;
    public boolean addRedirect;

    public MainPageRequest(String name, String data) {
        this.name = name;
        this.data = data;
        this.horizontalImages = false;
        this.addRedirect = true;
    }

    public MainPageRequest(String name, String data, boolean horizontalImages) {
        this.name = name;
        this.data = data;
        this.horizontalImages = horizontalImages;
        this.addRedirect = true;
    }
}
