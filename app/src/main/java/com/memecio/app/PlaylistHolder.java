package com.memecio.app;

import java.util.ArrayList;

public class PlaylistHolder {
    private static ArrayList<String> playlist = new ArrayList<>();

    public static void set(ArrayList<String> list) {
        playlist = list;
    }

    public static ArrayList<String> get() {
        return playlist;
    }
}

