package com.memecio.app

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import java.io.File

/**
 * UpdateDialog — overlay update dengan progress bar + 3 tombol.
 *
 * Alur:
 * 1. Tampil info versi + daftar perubahan
 * 2. User klik Update -> minta izin install (kalau belum) -> download -> auto install
 * 3. Progress bar 0-100%, semua tombol disable saat download
 * 4. Kalau download gagal -> tampil tombol "Coba Lagi"
 */
class UpdateDialog(
    private val activity: Activity,
    private val release: UpdateChecker.ReleaseInfo
) {

    private var dialog: Dialog? = null

    private lateinit var tvCurrentVersion: TextView
    private lateinit var tvNewVersion: TextView
    private lateinit var tvReleaseNotes: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvProgressDetail: TextView
    private lateinit var progressContainer: LinearLayout
    private lateinit var progressDownload: ProgressBar
    private lateinit var btnRetry: Button
    private lateinit var btnSkip: Button
    private lateinit var btnLater: Button
    private lateinit var btnUpdate: Button

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isDownloading = false
    private var lastApkFile: File? = null

    fun show() {
        try {
            val d = Dialog(activity)
            d.requestWindowFeature(Window.FEATURE_NO_TITLE)
            d.setContentView(R.layout.dialog_update)
            d.setCancelable(false)
            d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            d.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog = d

            tvCurrentVersion = d.findViewById(R.id.tvCurrentVersion)
            tvNewVersion = d.findViewById(R.id.tvNewVersion)
            tvReleaseNotes = d.findViewById(R.id.tvReleaseNotes)
            tvProgressPercent = d.findViewById(R.id.tvProgressPercent)
            tvProgressDetail = d.findViewById(R.id.tvProgressDetail)
            progressContainer = d.findViewById(R.id.progressContainer)
            progressDownload = d.findViewById(R.id.progressDownload)
            btnRetry = d.findViewById(R.id.btnRetry)
            btnSkip = d.findViewById(R.id.btnSkip)
            btnLater = d.findViewById(R.id.btnLater)
            btnUpdate = d.findViewById(R.id.btnUpdate)

            // Isi data
            val currentVer = UpdateChecker.getCurrentVersion(activity)
            tvCurrentVersion.text = currentVer
            tvNewVersion.text = release.version
            val notes = if (release.body.isNullOrEmpty()) {
                "• Tidak ada catatan rilis"
            } else {
                formatReleaseNotes(release.body)
            }
            tvReleaseNotes.text = notes

            // Listener tombol
            btnSkip.setOnClickListener {
                UpdateStore.setSkipVersion(activity, release.version)
                Toast.makeText(activity, "Versi ini di-skip", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            btnLater.setOnClickListener {
                dismiss()
            }

            btnUpdate.setOnClickListener {
                startUpdateFlow()
            }

            btnRetry.setOnClickListener {
                btnRetry.visibility = View.GONE
                startUpdateFlow()
            }

            d.show()
        } catch (e: Exception) {
            Toast.makeText(activity, "Gagal menampilkan dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatReleaseNotes(body: String): String {
        // Body dari GitHub biasanya format markdown.
        // Kita sederhanakan jadi poin-poin biar enak dibaca.
        val sb = StringBuilder()
        val lines = body.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            when {
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    sb.append("• ").append(trimmed.substring(2)).append("\n")
                }
                trimmed.startsWith("## ") -> {
                    sb.append("\n").append(trimmed.substring(3)).append("\n")
                }
                trimmed.startsWith("# ") -> {
                    sb.append("\n").append(trimmed.substring(2)).append("\n")
                }
                else -> {
                    sb.append(trimmed).append("\n")
                }
            }
        }
        return sb.toString().trim()
    }

    private fun startUpdateFlow() {
        // 1. Cek izin install
        if (!ApkInstaller.canInstallPackages(activity)) {
            Toast.makeText(
                activity,
                "Izinkan install aplikasi dulu, lalu coba lagi",
                Toast.LENGTH_LONG
            ).show()
            ApkInstaller.requestInstallPermission(activity)
            return
        }

        // 2. Mulai download
        if (isDownloading) return
        if (release.apkUrl.isNullOrEmpty()) {
            Toast.makeText(activity, "URL download tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        isDownloading = true
        showProgressUI()

        Thread {
            ApkDownloader.download(
                activity,
                release.apkUrl,
                release.version,
                object : ApkDownloader.ProgressListener {
                    override fun onProgress(progress: Int, bytesDownloaded: Long, totalBytes: Long) {
                        mainHandler.post {
                            progressDownload.progress = progress
                            tvProgressPercent.text = "$progress%"
                            if (totalBytes > 0) {
                                tvProgressDetail.text =
                                    "${ApkDownloader.formatSize(bytesDownloaded)} / ${ApkDownloader.formatSize(totalBytes)}"
                            } else {
                                tvProgressDetail.text = ApkDownloader.formatSize(bytesDownloaded)
                            }
                        }
                    }

                    override fun onSuccess(file: File) {
                        mainHandler.post {
                            isDownloading = false
                            lastApkFile = file
                            UpdateStore.clearSkip(activity)
                            Toast.makeText(activity, "Download selesai, menginstall...", Toast.LENGTH_SHORT).show()

                            // Auto install
                            val ok = ApkInstaller.installApk(activity, file.absolutePath)
                            if (ok) {
                                dismiss()
                            } else {
                                // Izin belum diberikan, tampil tombol Retry
                                showRetryUI("Izin install belum diberikan")
                            }
                        }
                    }

                    override fun onError(message: String) {
                        mainHandler.post {
                            isDownloading = false
                            showRetryUI("Download gagal: $message")
                        }
                    }

                    override fun onCancelled() {
                        mainHandler.post {
                            isDownloading = false
                            showRetryUI("Download dibatalkan")
                        }
                    }
                }
            )
        }.start()
    }

    private fun showProgressUI() {
        progressContainer.visibility = View.VISIBLE
        progressDownload.progress = 0
        tvProgressPercent.text = "0%"
        tvProgressDetail.text = "Menyiapkan..."

        // Disable semua tombol
        btnSkip.isEnabled = false
        btnLater.isEnabled = false
        btnUpdate.isEnabled = false
        btnSkip.alpha = 0.4f
        btnLater.alpha = 0.4f
        btnUpdate.alpha = 0.4f
        btnRetry.visibility = View.GONE
    }

    private fun showRetryUI(message: String) {
        btnRetry.visibility = View.VISIBLE
        progressContainer.visibility = View.GONE

        // Enable lagi tombol (kecuali Update supaya tidak double)
        btnSkip.isEnabled = true
        btnLater.isEnabled = true
        btnUpdate.isEnabled = true
        btnSkip.alpha = 1f
        btnLater.alpha = 1f
        btnUpdate.alpha = 1f

        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    fun dismiss() {
        try {
            dialog?.dismiss()
        } catch (ignored: Exception) {}
        dialog = null
    }

    companion object {
        /**
         * Cek dan tampilkan dialog update kalau ada versi baru.
         * Panggil dari Activity (onResume / onCreate).
         */
        @JvmStatic
        fun checkAndShow(activity: Activity) {
            // Skip kalau sedang putar video
            if (activity is VideoPlayerActivity) return

            // Cek cache dulu
            if (!UpdateStore.shouldCheck(activity)) {
                // Pakai data terakhir kalau ada
                val lastVersion = UpdateStore.getLastKnownLatest(activity) ?: return
                val currentVer = UpdateChecker.getCurrentVersion(activity)
                if (!UpdateChecker.isNewerVersion(currentVer, lastVersion)) return
                if (UpdateStore.isSkipped(activity, lastVersion)) return
                return
            }

            UpdateChecker.checkAsync(activity, object : UpdateChecker.Callback {
                override fun onResult(info: UpdateChecker.ReleaseInfo?) {
                    if (info == null) return
                    UpdateStore.markChecked(activity)
                    UpdateStore.setLastKnownLatest(activity, info.version)

                    val currentVer = UpdateChecker.getCurrentVersion(activity)
                    if (!UpdateChecker.isNewerVersion(currentVer, info.version)) return
                    if (UpdateStore.isSkipped(activity, info.version)) return

                    Handler(Looper.getMainLooper()).post {
                        try {
                            UpdateDialog(activity, info).show()
                        } catch (ignored: Exception) {}
                    }
                }
            })
        }
    }
}
