package com.memecio.app;

public class MultiviewPickHolder {
    public static int pickSlot = -1;      // slot yang sedang dipilih, -1 = tidak picking
    public static int deliveredSlot = -1; // slot hasil pick
    public static String deliveredUrl = null; // url hasil pick

    public static void startPick(int slot) { pickSlot = slot; }
    public static void cancel() { pickSlot = -1; }
    public static boolean isPicking() { return pickSlot >= 0; }

    public static void deliver(int slot, String url) {
        deliveredSlot = slot;
        deliveredUrl = url;
        pickSlot = -1;
    }

    public static void consume() {
        deliveredSlot = -1;
        deliveredUrl = null;
    }
}
