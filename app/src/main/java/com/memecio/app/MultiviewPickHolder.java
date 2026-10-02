package com.memecio.app;

public class MultiviewPickHolder {
    public static int pickSlot = -1;
    public static int deliveredSlot = -1;
    public static String deliveredUrl = null;
    public static String deliveredType = null; // "image" atau "video"

    // Simpan URL + tipe tiap slot (persistent antar instance)
    public static String[] slotUrls = new String[]{null, null, null, null};
    public static String[] slotTypes = new String[]{null, null, null, null};

    public static void startPick(int slot) { pickSlot = slot; }
    public static void cancel() { pickSlot = -1; }
    public static boolean isPicking() { return pickSlot >= 0; }

    public static void deliver(int slot, String url, String type) {
        deliveredSlot = slot;
        deliveredUrl = url;
        deliveredType = type;
        if (slot >= 0 && slot < 4) {
            slotUrls[slot] = url;
            slotTypes[slot] = type;
        }
        pickSlot = -1;
    }

    public static void consume() {
        deliveredSlot = -1;
        deliveredUrl = null;
        deliveredType = null;
    }

    public static void saveSlot(int slot, String url, String type) {
        if (slot >= 0 && slot < 4) {
            slotUrls[slot] = url;
            slotTypes[slot] = type;
        }
    }

    public static void clearSlot(int slot) {
        if (slot >= 0 && slot < 4) {
            slotUrls[slot] = null;
            slotTypes[slot] = null;
        }
    }
}
