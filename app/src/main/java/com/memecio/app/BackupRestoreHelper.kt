package com.memecio.app

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreHelper {

    private fun getBackupDir(): File {
        val dir = File(Environment.getExternalStorageDirectory(), "Termux/Ekspor File")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    @JvmStatic
    fun exportPlaylists(context: Context) {
        try {
            val root = JSONObject()

            // 1. Playlist manual
            val manuals = JSONArray()
            val playlists = CustomPlaylistStore.getAll(context)
            for (pl in playlists) {
                val obj = JSONObject()
                obj.put("name", pl.name)
                val itemsArr = JSONArray()
                for (item in pl.items) {
                    val itemObj = JSONObject()
                    itemObj.put("uri", item.uri.toString())
                    itemObj.put("type", item.type)
                    itemObj.put("title", item.title ?: "")
                    itemsArr.put(itemObj)
                }
                obj.put("items", itemsArr)
                manuals.put(obj)
            }
            root.put("playlist_manual", manuals)

            // 2. Playlist URL / streaming (SavedLinksStore)
            val savedLinks = JSONArray()
            try {
                val saved = SavedLinksStore.getAll(context)
                for (m in saved) {
                    val obj = JSONObject()
                    obj.put("uri", m.uri.toString())
                    obj.put("title", m.title ?: "")
                    savedLinks.put(obj)
                }
            } catch (ignored: Exception) {
            }
            root.put("playlist_url", savedLinks)

            // 3. Riwayat link
            val riwayat = JSONArray()
            try {
                val hist = RiwayatStore.getAll(context)
                for (h in hist) {
                    riwayat.put(h)
                }
            } catch (ignored: Exception) {
            }
            root.put("riwayat_link", riwayat)

            // 4. Media eksternal aktif
            val eksternal = JSONArray()
            try {
                val ext = ExternalMediaStore.getAll(context)
                for (m in ext) {
                    val obj = JSONObject()
                    obj.put("uri", m.uri.toString())
                    obj.put("type", m.type)
                    obj.put("title", m.title ?: "")
                    eksternal.put(obj)
                }
            } catch (ignored: Exception) {
            }
            root.put("media_eksternal", eksternal)

            // 5. Info header
            root.put(
                "export_date",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )

            // Simpan file dengan timestamp
            val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val file = File(getBackupDir(), "data_memecio_$ts.json")
            val out = FileOutputStream(file)
            out.write(root.toString(2).toByteArray(StandardCharsets.UTF_8))
            out.close()

            Toast.makeText(context, "Ekspor berhasil:\n" + file.absolutePath, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal ekspor: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    fun importPlaylists(context: Context) {
        try {
            // Cari file JSON terbaru di folder
            val dir = getBackupDir()
            val files = dir.listFiles { _, name ->
                name.startsWith("data_memecio_") && name.endsWith(".json")
            }
            if (files == null || files.isEmpty()) {
                Toast.makeText(context, "Tidak ada file backup di " + dir.absolutePath, Toast.LENGTH_LONG).show()
                return
            }

            // Ambil file terbaru
            var latest = files[0]
            for (f in files) {
                if (f.lastModified() > latest.lastModified()) latest = f
            }

            val input = FileInputStream(latest)
            val buf = ByteArray(latest.length().toInt())
            input.read(buf)
            input.close()
            val json = String(buf, StandardCharsets.UTF_8)
            val root = JSONObject(json)
            var imported = 0

            // 1. Import playlist manual (tambah ke existing)
            if (root.has("playlist_manual")) {
                val manuals = root.getJSONArray("playlist_manual")
                val existing = CustomPlaylistStore.getAll(context)
                for (i in 0 until manuals.length()) {
                    val obj = manuals.getJSONObject(i)
                    val namaBaru = obj.getString("name")

                    // Skip kalau nama sudah ada
                    var skip = false
                    for (ex in existing) {
                        if (ex.name == namaBaru) {
                            skip = true
                            break
                        }
                    }
                    if (skip) continue

                    val pl = CustomPlaylistStore.Playlist()
                    pl.name = namaBaru
                    val itemsArr = obj.getJSONArray("items")
                    for (j in 0 until itemsArr.length()) {
                        val itemObj = itemsArr.getJSONObject(j)
                        val item = MediaItem(Uri.parse(itemObj.getString("uri")), itemObj.getInt("type"))
                        item.isLocal = false
                        val t = itemObj.optString("title", "")
                        item.title = if (t.isEmpty()) null else t
                        pl.items.add(item)
                    }
                    existing.add(pl)
                    imported++
                }
                CustomPlaylistStore.saveAll(context, existing)
            }

            // 2. Import playlist URL (tambah ke existing)
            if (root.has("playlist_url")) {
                val savedArr = root.getJSONArray("playlist_url")
                val existingSaved = SavedLinksStore.getAll(context)
                for (i in 0 until savedArr.length()) {
                    val obj = savedArr.getJSONObject(i)
                    val uri = obj.getString("uri")

                    // Skip duplikat
                    var skip = false
                    for (ex in existingSaved) {
                        if (ex.uri.toString() == uri) {
                            skip = true
                            break
                        }
                    }
                    if (skip) continue

                    val t = obj.optString("title", "")
                    SavedLinksStore.tambah(context, if (t.isEmpty()) uri else t, uri)
                    imported++
                }
            }

            // 3. Import riwayat link
            if (root.has("riwayat_link")) {
                val riwArr = root.getJSONArray("riwayat_link")
                val existingRiw = RiwayatStore.getAll(context)
                for (i in 0 until riwArr.length()) {
                    val url = riwArr.getString(i)
                    if (!existingRiw.contains(url)) {
                        RiwayatStore.tambah(context, url)
                        imported++
                    }
                }
            }

            Toast.makeText(
                context,
                "Impor dari: " + latest.name + "\n" + imported + " item ditambahkan",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal impor: " + e.message, Toast.LENGTH_LONG).show()
        }
    }
}
