package com.memecio.app

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.util.ArrayList
import java.util.LinkedHashSet

/**
 * MultiSourceStore — simpan semua sumber online (Drive/Streaming/Server) terpisah.
 * Tidak lagi timpa-menimpa. Setiap sumber punya sourceId unik.
 */
object MultiSourceStore {

    private const val PREF_NAME = "memecio_multi_source"
    private const val KEY_SOURCES = "sources_list"
    private const val KEY_PREFIX = "src_"
    private const val SEP_FIELD = "\u0003"
    private const val SEP_ITEM = "\u0002"
    private const val SEP_SOURCE = "\u0001"

    // Format data per source: label|type|item1\u0002item2\u0002...
    // Format item: type\u0003uri\u0003title\u0003thumb\u0003sourceTitle

    @JvmStatic
    fun save(
        ctx: Context,
        sourceId: String?,
        sourceLabel: String?,
        sourceType: String?,
        items: List<MediaItem>?
    ) {
        if (sourceId.isNullOrEmpty() || items == null) return

        val sb = StringBuilder()
        sb.append(sourceLabel ?: "").append(SEP_FIELD)
        sb.append(sourceType ?: "")

        for (m in items) {
            if (m == null) continue
            sb.append(SEP_ITEM) // selalu sebelum tiap item (fix: item pertama hilang)
            sb.append(m.type).append(SEP_FIELD)
                .append(m.uri.toString()).append(SEP_FIELD)
                .append(m.title ?: "").append(SEP_FIELD)
                .append(m.thumbUrl ?: "").append(SEP_FIELD)
                .append(m.sourceTitle ?: "")
        }

        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = LinkedHashSet(prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: LinkedHashSet())
        sources.add(sourceId)
        prefs.edit()
            .putStringSet(KEY_SOURCES, sources)
            .putString(KEY_PREFIX + sourceId, sb.toString())
            .apply()
    }

    @JvmStatic
    fun getAllMerged(ctx: Context): List<MediaItem> {
        val result = ArrayList<MediaItem>()
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: LinkedHashSet()
        for (sourceId in sources) {
            val data = prefs.getString(KEY_PREFIX + sourceId, "")
            if (data.isNullOrEmpty()) continue
            result.addAll(parseData(data))
        }
        return result
    }

    @JvmStatic
    fun getBySource(ctx: Context, sourceId: String?): List<MediaItem> {
        if (sourceId == null) return ArrayList()
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_PREFIX + sourceId, "")
        if (data.isNullOrEmpty()) return ArrayList()
        return parseData(data)
    }

    @JvmStatic
    fun listSourceIds(ctx: Context): List<String> {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: LinkedHashSet()
        return ArrayList(sources)
    }

    @JvmStatic
    fun getLabel(ctx: Context, sourceId: String?): String {
        if (sourceId == null) return ""
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_PREFIX + sourceId, "")
        if (data.isNullOrEmpty()) return ""
        val parts = data.split(SEP_FIELD)
        return if (parts.isNotEmpty()) parts[0] else ""
    }

    @JvmStatic
    fun removeSource(ctx: Context, sourceId: String?) {
        if (sourceId == null) return
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sources = LinkedHashSet(prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: LinkedHashSet())
        sources.remove(sourceId)
        prefs.edit()
            .putStringSet(KEY_SOURCES, sources)
            .remove(KEY_PREFIX + sourceId)
            .apply()
    }

    @JvmStatic
    fun kosongkan(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun parseData(data: String): List<MediaItem> {
        val result = ArrayList<MediaItem>()

        // Format: "label\u0003type\u0002item1\u0002item2..."
        // topParts[0] = "label\u0003type" (header saja), topParts[1..] = items
        val topParts = data.split(SEP_ITEM)
        if (topParts.size < 2) return result

        for (i in 1 until topParts.size) {
            val itemStr = topParts[i]
            if (itemStr.isEmpty()) continue
            val fields = itemStr.split(SEP_FIELD, limit = -1)
            if (fields.size < 2) continue
            try {
                val type = fields[0].toInt()
                val uriStr = fields[1]
                val title = if (fields.size > 2) fields[2] else ""
                val thumb = if (fields.size > 3) fields[3] else ""
                val sourceTitle = if (fields.size > 4) fields[4] else ""

                val m = MediaItem(Uri.parse(uriStr), type)
                m.isLocal = false
                m.title = if (title.isEmpty()) null else title
                m.thumbUrl = if (thumb.isEmpty()) null else thumb
                m.sourceTitle = if (sourceTitle.isEmpty()) null else sourceTitle
                result.add(m)
            } catch (ignored: Exception) {
            }
        }
        return result
    }

    /**
     * Hapus semua source lama (migrated_* dan src_ format lama).
     * Simpan hanya yang format baru (src_ _).
     */
    @JvmStatic
    fun deduplicate(ctx: Context) {
        try {
            val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val sources = LinkedHashSet(prefs.getStringSet(KEY_SOURCES, LinkedHashSet()) ?: LinkedHashSet())
            val keep = LinkedHashSet<String>()
            val ed = prefs.edit()
            for (id in sources) {
                // Format baru: src_ _. Format lama: migrated_* atau src_ 
                val isNewFormat = id.startsWith("src_") && id.matches(Regex("src_\\d+_\\d+"))
                if (isNewFormat) {
                    keep.add(id)
                } else {
                    ed.remove(KEY_PREFIX + id)
                }
            }
            ed.putStringSet(KEY_SOURCES, keep)
            ed.apply()
        } catch (ignored: Exception) {
        }
    }

    /** Hapus SEMUA source (untuk reset manual dari user). */
    @JvmStatic
    fun resetAll(ctx: Context) {
        kosongkan(ctx)
    }

    /** Migrasi data dari ExternalMediaStore kalau ada. */
    @JvmStatic
    fun migrateFromExternal(ctx: Context) {
        try {
            val old = ExternalMediaStore.getAll(ctx)
            if (old.isNullOrEmpty()) return
            var oldLabel = ExternalMediaStore.getSourceLabel(ctx)
            if (oldLabel.isNullOrEmpty()) oldLabel = "Sumber Lama"

            // Cek apakah sudah pernah migrasi
            val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            if (prefs.getBoolean("migrated", false)) return

            // Migrasi pakai sourceId dari label
            val sourceId = "migrated_" + Math.abs(oldLabel.hashCode())
            save(ctx, sourceId, oldLabel, "migrated", old)
            prefs.edit().putBoolean("migrated", true).apply()
        } catch (ignored: Exception) {
        }
    }
}
