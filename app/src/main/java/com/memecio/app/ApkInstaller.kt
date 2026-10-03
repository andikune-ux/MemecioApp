package com.memecio.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * ApkInstaller — minta izin install + auto install APK.
 *
 * Butuh:
 * 1. Izin REQUEST_INSTALL_PACKAGES di AndroidManifest
 * 2. FileProvider di AndroidManifest (provider android:name="androidx.core.content.FileProvider")
 * 3. res/xml/file_paths.xml
 */
object ApkInstaller {

    private const val REQ_INSTALL_PERMISSION = 5001

    /**
     * Cek apakah aplikasi sudah punya izin install APK.
     * Android 8.0+ (API 26) butuh izin ini.
     */
    @JvmStatic
    fun canInstallPackages(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        return try {
            context.packageManager.canRequestPackageInstalls()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Buka halaman Settings untuk minta izin install.
     * Setelah user memberi izin, kembali ke app -> user bisa klik Install lagi.
     */
    @JvmStatic
    fun requestInstallPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivityForResult(intent, REQ_INSTALL_PERMISSION)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                activity.startActivityForResult(intent, REQ_INSTALL_PERMISSION)
            } catch (e2: Exception) {
                Toast.makeText(
                    activity,
                    "Tidak bisa membuka pengaturan izin install",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Install APK dari path file.
     *
     * @return true kalau proses install berhasil dimulai
     *         false kalau izin belum ada (sudah otomatis buka Settings)
     */
    @JvmStatic
    fun installApk(activity: Activity, apkPath: String): Boolean {
        val file = File(apkPath)
        if (!file.exists()) {
            Toast.makeText(activity, "File APK tidak ditemukan", Toast.LENGTH_SHORT).show()
            return false
        }

        // Cek izin install dulu
        if (!canInstallPackages(activity)) {
            Toast.makeText(
                activity,
                "Izinkan install aplikasi dari Memecio, lalu coba lagi",
                Toast.LENGTH_LONG
            ).show()
            requestInstallPermission(activity)
            return false
        }

        return try {
            val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                "Gagal install: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    /**
     * Dipanggil dari onActivityResult kalau request izin selesai.
     * Cek apakah izin sudah diberikan.
     */
    @JvmStatic
    fun isPermissionRequest(requestCode: Int): Boolean {
        return requestCode == REQ_INSTALL_PERMISSION
    }
}
