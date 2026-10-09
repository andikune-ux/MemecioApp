package com.memecio.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class SecretCodesActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_codes)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val allCodes = SecretCodeRegistry.getAll()
        val countView = findViewById<TextView>(R.id.tvCodeCount)
        countView.text = "${allCodes.size} Kode"

        val listView = findViewById<ListView>(R.id.lvSecretCodes)
        val adapter = SecretCodeAdapter(this, allCodes) { code ->
            runSecretCode(code.code)
        }
        listView.adapter = adapter
    }

    private fun runSecretCode(code: String) {
        try {
            when (code) {
                "140399" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                "000" -> Toast.makeText(this, "Anda sedang berada di halaman Kode Rahasia (000)", Toast.LENGTH_SHORT).show()
                "111" -> startActivity(Intent(this, CrashHistoryActivity::class.java))
                "222" -> startActivity(Intent(this, ChangelogActivity::class.java))
                "333" -> startActivity(Intent(this, SystemInfoActivity::class.java))
                "555" -> startActivity(Intent(this, NetworkInfoActivity::class.java))
                "666" -> startActivity(Intent(this, PermissionInfoActivity::class.java))
                "777" -> startActivity(Intent(this, StorageAnalyzerActivity::class.java))
                "808" -> startActivity(Intent(this, DownloadListActivity::class.java))
                "888" -> startActivity(Intent(this, StatistikActivity::class.java))
                "999" -> ProjectExportHelper.export(this)
                "101" -> DeveloperModeStore.toggle(this)
                "102" -> throw RuntimeException("Force Crash — simulasi melalui Kode Rahasia 102")
                "103" -> TestGestureHelper.showGuide(this)
                "104" -> TestModesHelper.showChoice(this)
                "123" -> CacheResetter.confirmAndReset(this)
                "444" -> TestMediaHelper.showChoice(this)
                "456" -> RepairDatabaseHelper.confirmAndRepair(this)
                "789" -> FactoryResetHelper.confirmAndReset(this)
                "200" -> {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
                "201" -> {
                    try {
                        startActivity(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Tidak didukung di perangkat ini", Toast.LENGTH_SHORT).show()
                    }
                }
                "202" -> {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Tidak didukung di perangkat ini", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> Toast.makeText(this, "Kode $code tidak dikenali", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Aksi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private class SecretCodeAdapter(
        context: Context,
        private val list: List<SecretCodeRegistry.Code>,
        private val onRun: (SecretCodeRegistry.Code) -> Unit
    ) : ArrayAdapter<SecretCodeRegistry.Code>(context, 0, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_secret_code, parent, false)
            val item = list[position]

            val tvPill = view.findViewById<TextView>(R.id.tvCodePill)
            val tvTitle = view.findViewById<TextView>(R.id.tvCodeTitle)
            val tvDesc = view.findViewById<TextView>(R.id.tvCodeDesc)
            val btnRun = view.findViewById<Button>(R.id.btnRunCode)

            tvPill.text = item.code
            tvTitle.text = item.title + if (item.hidden) " [Hidden]" else ""
            tvDesc.text = item.description

            btnRun.setOnClickListener {
                onRun(item)
            }

            return view
        }
    }
}
