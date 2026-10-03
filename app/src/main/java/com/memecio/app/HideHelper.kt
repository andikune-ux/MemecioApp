package com.memecio.app

import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

object HideHelper {

    @JvmStatic
    fun sembunyikan(context: Context, item: MediaItem): Boolean {
        try {
            HiddenMediaStore.ensureNoMedia(context)
            val uri = item.uri

            var displayName = getDisplayName(context, uri)
            if (displayName.isNullOrEmpty()) {
                displayName = "media_${System.currentTimeMillis()}"
            }

            val folder = HiddenMediaStore.getFolder()
            val destFile = File(folder, displayName)

            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return false
            val outputStream = FileOutputStream(destFile)
            val buffer = ByteArray(8192)
            var len: Int
            while (inputStream.read(buffer).also { len = it } > 0) {
                outputStream.write(buffer, 0, len)
            }
            outputStream.close()
            inputStream.close()

            try {
                context.contentResolver.delete(uri, null, null)
            } catch (_: Exception) {
            }

            return true
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal menyembunyikan: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    @JvmStatic
    fun kembalikan(context: Context, item: MediaItem): Boolean {
        try {
            val uri = item.uri
            if (uri.scheme != "file") return false
            val path = uri.path ?: return false
            val srcFile = File(path)
            if (!srcFile.exists()) return false

            val name = srcFile.name

            var destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (item.type == MediaItem.TYPE_VIDEO) {
                destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            } else if (item.type == MediaItem.TYPE_AUDIO) {
                destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            }
            if (!destDir.exists()) destDir.mkdirs()

            var destFile = File(destDir, name)
            var counter = 1
            while (destFile.exists()) {
                val base = if (name.contains(".")) name.substringBeforeLast('.') else name
                val ext = if (name.contains(".")) "." + name.substringAfterLast('.') else ""
                destFile = File(destDir, "${base}_$counter$ext")
                counter++
            }

            copyFile(srcFile, destFile)
            srcFile.delete()

            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(destFile.absolutePath),
                    null,
                    null
                )
            } catch (_: Exception) {}

            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun getDisplayName(context: Context, uri: Uri): String? {
        try {
            if (uri.scheme == "file") {
                return File(uri.path ?: "").name
            }
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && it.moveToFirst()) {
                    return it.getString(idx)
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun copyFile(src: File, dest: File) {
        FileInputStream(src).use { `in` ->
            FileOutputStream(dest).use { out ->
                val buf = ByteArray(8192)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }
}
