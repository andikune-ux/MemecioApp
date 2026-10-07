package com.memecio.app

object MultiviewPickHolder {
    @JvmField var pickSlot: Int = -1
    @JvmField var deliveredSlot: Int = -1
    @JvmField var deliveredUrl: String? = null
    @JvmField var deliveredType: String? = null // "image" atau "video"

    // Simpan URL + tipe tiap slot (persistent antar instance)
    @JvmField var slotUrls: Array<String?> = arrayOfNulls(4)
    @JvmField var slotTypes: Array<String?> = arrayOfNulls(4)

    @JvmStatic fun startPick(slot: Int) { pickSlot = slot }
    @JvmStatic fun cancel() { pickSlot = -1 }
    @JvmStatic fun isPicking(): Boolean = pickSlot >= 0

    @JvmStatic
    fun deliver(slot: Int, url: String?, type: String?) {
        deliveredSlot = slot
        deliveredUrl = url
        deliveredType = type
        if (slot in 0..3) {
            slotUrls[slot] = url
            slotTypes[slot] = type
        }
        pickSlot = -1
    }

    @JvmStatic
    fun consume() {
        deliveredSlot = -1
        deliveredUrl = null
        deliveredType = null
    }

    @JvmStatic
    fun saveSlot(slot: Int, url: String?, type: String?) {
        if (slot in 0..3) {
            slotUrls[slot] = url
            slotTypes[slot] = type
        }
    }

    @JvmStatic
    fun clearSlot(slot: Int) {
        if (slot in 0..3) {
            slotUrls[slot] = null
            slotTypes[slot] = null
        }
    }

    @JvmStatic fun getUrl(slot: Int): String? = if (slot in 0..3) slotUrls[slot] else null
    @JvmStatic fun getType(slot: Int): String? = if (slot in 0..3) slotTypes[slot] else null
    @JvmStatic fun setPickSlot(slot: Int) { pickSlot = slot }
    @JvmStatic fun clear(slot: Int) { clearSlot(slot) }
    @JvmStatic fun swap(src: Int, dst: Int) {
        if (src in 0..3 && dst in 0..3) {
            val u = slotUrls[src]
            slotUrls[src] = slotUrls[dst]
            slotUrls[dst] = u

            val t = slotTypes[src]
            slotTypes[src] = slotTypes[dst]
            slotTypes[dst] = t
        }
    }
}
