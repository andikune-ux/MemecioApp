package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AppInfoActivity — halaman Info Aplikasi.
 *
 * Fitur:
 * 1. Tampil versi aplikasi saat ini
 * 2. Tombol "Cek Update" -> cek ke GitHub Releases
 * 3. Kalau ada versi baru -> dialog update muncul otomatis
 * 4. Daftar APK yang sudah didownload + tombol Install & Hapus
 */
class AppInfoActivity : Activity() {

    private lateinit var tvVersion: TextView
    private lateinit var tvCheckStatus: TextView
    private lateinit var btnCheckUpdate: Button
    private lateinit var listApkContainer: LinearLayout
    private lateinit var tvNoApk: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)

        val btnBack = findViewById<ImageButton>(R.id.btnBackAppInfo)
        tvVersion = findViewById(R.id.tvAppInfoVersion)
        tvCheckStatus = findViewById(R.id.tvAppInfoCheckStatus)
        btnCheckUpdate = findViewById(R.id.btnCheckUpdate)
        listApkContainer = findViewById(R.id.listApkContainer)
        tvNoApk = findViewById(R.id.tvNoApk)

        btnBack.setOnClickListener { finish() }

        // Tampil versi saat ini
        val currentVer = UpdateChecker.getCurrentVersion(this)
        tvVersion.text = "V$currentVer"

        // Cek status dari cache
        val lastKnown = UpdateStore.getLastKnownLatest(this)
        if (lastKnown != null && UpdateChecker.isNewerVersion(currentVer, lastKnown)) {
            tvCheckStatus.text = "Versi terbaru tersedia: V$lastKnown"
            tvCheckStatus.setTextColor(0xFF4CAF50.toInt())
        } else {
            tvCheckStatus.text = "Tekan tombol di bawah untuk cek versi terbaru."
            tvCheckStatus.setTextColor(0xFF666666.toInt())
        }

        // Tombol Cek Update
        btnCheckUpdate.setOnClickListener {
            doCheckUpdate()
        }

        // Render daftar APK
        renderApkList()
    }

    override fun onResume() {
        super.onResume()
        renderApkList()
    }

    private fun doCheckUpdate() {
        btnCheckUpdate.isEnabled = false
        btnCheckUpdate.text = "Memeriksa..."
        tvCheckStatus.text = "Menghubungi GitHub..."

        // Reset cache supaya cek betulan
        UpdateStore.clearCheckCache(this)

        UpdateChecker.checkAsync(this, object : UpdateChecker.Callback {
            override fun onResult(info: UpdateChecker.ReleaseInfo?) {
                Handler(Looper.getMainLooper()).post {
                    btnCheckUpdate.isEnabled = true
                    btnCheckUpdate.text = "Cek Update"

                    if (info == null) {
                        tvCheckStatus.text = "Gagal cek update. Periksa koneksi internet."
                        tvCheckStatus.setTextColor(0xFFF44336.toInt())
                        Toast.makeText(
                            this@AppInfoActivity,
                            "Gagal cek update",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@post
                    }

                    UpdateStore.markChecked(this@AppInfoActivity)
                    UpdateStore.setLastKnownLatest(this@AppInfoActivity, info.version)

                    val currentVer = UpdateChecker.getCurrentVersion(this@AppInfoActivity)
                    if (UpdateChecker.isNewerVersion(currentVer, info.version)) {
                        tvCheckStatus.text = "Versi terbaru: V${info.version}"
                        tvCheckStatus.setTextColor(0xFF4CAF50.toInt())

                        // Munculkan dialog update
                        val dialog = UpdateDialog(this@AppInfoActivity, info)
                        dialog.show()
                    } else {
                        tvCheckStatus.text = "Versi kamu sudah paling baru! ✅"
                        tvCheckStatus.setTextColor(0xFF4CAF50.toInt())
                        Toast.makeText(
                            this@AppInfoActivity,
                            "Versi sudah paling baru",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun renderApkList() {
        listApkContainer.removeAllViews()
        val list = UpdateStore.getAllApk(this)

        // Filter file yang masih ada di penyimpanan
        val validList = list.filter { File(it.path).exists() }

        if (validList.isEmpty()) {
            tvNoApk.visibility = View.VISIBLE
            return
        }

        tvNoApk.visibility = View.GONE

        for (entry in validList) {
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_apk_download, listApkContainer, false)

            val tvApkVersion = card.findViewById<TextView>(R.id.tvApkVersion)
            val tvApkInfo = card.findViewById<TextView>(R.id.tvApkInfo)
            val btnInstall = card.findViewById<Button>(R.id.btnInstallApk)
            val btnDelete = card.findViewById<Button>(R.id.btnDeleteApk)

            tvApkVersion.text = "V${entry.version}"

            val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val dateStr = fmt.format(Date(entry.downloadedAt))
            tvApkInfo.text = "${ApkDownloader.formatSize(entry.sizeBytes)} • $dateStr"

            btnInstall.setOnClickListener {
                val file = File(entry.path)
                if (!file.exists()) {
                    Toast.makeText(this, "File sudah tidak ada", Toast.LENGTH_SHORT).show()
                    renderApkList()
                    return@setOnClickListener
                }
                ApkInstaller.installApk(this, entry.path)
            }

            btnDelete.setOnClickListener {
                confirmDelete(entry.version, entry.path)
            }

            listApkContainer.addView(card)
        }
    }

    private fun confirmDelete(version: String, path: String) {
        AlertDialog.Builder(this)
            .setTitle("Hapus APK")
            .setMessage("Hapus file APK versi V$version?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                val ok = ApkDownloader.deleteApk(path)
                if (ok) {
                    UpdateStore.removeApk(this, version)
                    Toast.makeText(this, "APK dihapus", Toast.LENGTH_SHORT).show()
                    renderApkList()
                } else {
                    Toast.makeText(this, "Gagal hapus file", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
