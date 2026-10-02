package com.memecio.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.memecio.app.js.JsRuntime

class CalculatorActivity : Activity() {

    private var tvDisplay: TextView? = null
    private var tvDisplayScientific: TextView? = null
    private var rawInput = ""
    private var delStreak = 0
    private var delStreakStart = ""
    private var isScientific = false
    private var lastAppliedMode: String? = null

    companion object {
        private const val SECRET_CODE = "140399"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyModeAndLayout()
        CrashCheckHelper.checkAndShow(this)
        setupButtons()
    }

    private fun applyModeAndLayout() {
        val mode = DisplayModeStore.getEffectiveMode(this)
        lastAppliedMode = mode
        if (mode == "tv") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            setContentView(R.layout.activity_calculator_scientific)
            tvDisplayScientific = findViewById(R.id.tvDisplayScientific)
            isScientific = true
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_calculator_scientific)
                tvDisplayScientific = findViewById(R.id.tvDisplayScientific)
                isScientific = true
            } else {
                setContentView(R.layout.activity_calculator)
                tvDisplay = findViewById(R.id.tvDisplay)
                isScientific = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentMode = DisplayModeStore.getEffectiveMode(this)
        if (lastAppliedMode != null && lastAppliedMode != currentMode) {
            recreate()
        }
    }

    private fun testJsRhino() {
        val sb = StringBuilder()
        sb.append("1+1 = ").append(JsRuntime.evalToString("1+1")).append("\n")
        sb.append("2*3+4 = ").append(JsRuntime.evalToString("2*3+4")).append("\n")
        sb.append("'hello'+' world' = ").append(JsRuntime.evalToString("'hello' + ' world'")).append("\n")
        sb.append("Math.max(1,5,3) = ").append(JsRuntime.evalToString("Math.max(1,5,3)")).append("\n")
        sb.append("[1,2,3].map(x=>x*2).join(',') = ").append(JsRuntime.evalToString("[1,2,3].map(x=>x*2).join(',')")).append("\n")
        sb.append("JSON.parse test = ").append(JsRuntime.evalToString("JSON.parse('{\"a\":1}').a")).append("\n")
        val result = sb.toString()
        try {
            AlertDialog.Builder(this)
                .setTitle("JS Runtime OK")
                .setMessage(result)
                .setPositiveButton("OK", null)
                .setNeutralButton("Salin") { _, _ ->
                    try {
                        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("js", result))
                        Toast.makeText(this, "Disalin", Toast.LENGTH_SHORT).show()
                    } catch (ignored: Throwable) {
                    }
                }
                .show()
        } catch (t: Throwable) {
            AlertDialog.Builder(this)
                .setTitle("Dialog error")
                .setMessage(t.message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun testJsHttp() {
        val sb = StringBuilder()
        try {
            JsRuntime.reset()
        } catch (ignored: Throwable) {
        }
        val sep = System.lineSeparator()
        try {
            sb.append("bridgeReady=").append(JsRuntime.evalToString("typeof __jsBridgeReady")).append(sep)
            sb.append("http=").append(JsRuntime.evalToString("typeof http")).append(sep)
            sb.append("console=").append(JsRuntime.evalToString("typeof console")).append(sep)
            sb.append("get=").append(JsRuntime.evalToString("var r = http.get('https://example.com'); r.code + ':' + r.text.length")).append(sep)
            sb.append("title=").append(JsRuntime.evalToString("var r2 = http.get('https://example.com'); r2.document().title()"))
        } catch (t: Throwable) {
            sb.append("ERROR: ").append(t.toString())
        }
        val result = sb.toString()
        try {
            AlertDialog.Builder(this)
                .setTitle("HTTP Test")
                .setMessage(result)
                .setPositiveButton("OK", null)
                .setNeutralButton("Salin") { _, _ ->
                    try {
                        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("http", result))
                        Toast.makeText(this, "Disalin", Toast.LENGTH_SHORT).show()
                    } catch (ignored: Throwable) {
                    }
                }
                .show()
        } catch (t: Throwable) {
        }
    }

    private fun setupButtons() {
        if (isScientific) {
            bindScienceButtons()
        } else {
            bindPortraitButtons()
        }
    }

    private fun bindPortraitButtons() {
        val digitIds = intArrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn00
        )
        for (id in digitIds) {
            val btn = findViewById<TextView>(id)
            btn.setOnClickListener { v ->
                rawInput += (v as TextView).text.toString()
                updateDisplay()
            }
        }
        findViewById<View>(R.id.btnDot).setOnClickListener {
            rawInput += "."
            updateDisplay()
        }
        findViewById<View>(R.id.btnPlus).setOnClickListener {
            rawInput += "+"
            updateDisplay()
        }
        findViewById<View>(R.id.btnMinus).setOnClickListener {
            rawInput += "-"
            updateDisplay()
        }
        findViewById<View>(R.id.btnMultiply).setOnClickListener {
            rawInput += "*"
            updateDisplay()
        }
        findViewById<View>(R.id.btnDivide).setOnClickListener {
            rawInput += "/"
            updateDisplay()
        }
        findViewById<View>(R.id.btnClear).setOnClickListener {
            rawInput = ""
            delStreak = 0
            delStreakStart = ""
            updateDisplay()
        }
        findViewById<View>(R.id.btnDel).setOnClickListener {
            if (delStreak == 0) delStreakStart = rawInput
            delStreak++
            if (rawInput.isNotEmpty()) rawInput = rawInput.substring(0, rawInput.length - 1)
            updateDisplay()
        }
        findViewById<View>(R.id.btnPercent).setOnClickListener {
            try {
                val v = rawInput.toDouble()
                rawInput = formatResult(v / 100.0)
            } catch (ignored: Exception) {
            }
            updateDisplay()
        }
        findViewById<View>(R.id.btnEquals).setOnClickListener {
            if (delStreak == 3 && "0000" == delStreakStart) {
                val newState = PrivacyStore.toggle()
                Toast.makeText(this, "FLAG SECURE " + if (newState) "ON" else "OFF", Toast.LENGTH_SHORT).show()
                rawInput = ""
                delStreak = 0
                delStreakStart = ""
                updateDisplay()
                return@setOnClickListener
            }
            delStreak = 0
            delStreakStart = ""
            onEquals()
        }
    }

    private fun bindScienceButtons() {
        val digitIds = intArrayOf(
            R.id.btn0Sci, R.id.btn1Sci, R.id.btn2Sci, R.id.btn3Sci, R.id.btn4Sci,
            R.id.btn5Sci, R.id.btn6Sci, R.id.btn7Sci, R.id.btn8Sci, R.id.btn9Sci, R.id.btn00Sci
        )
        for (id in digitIds) {
            val btn = findViewById<TextView>(id)
            btn.setOnClickListener { v ->
                rawInput += (v as TextView).text.toString()
                updateDisplayScientific()
            }
        }
        findViewById<View>(R.id.btnDotSci).setOnClickListener {
            rawInput += "."
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnPlusSci).setOnClickListener {
            rawInput += "+"
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnMinusSci).setOnClickListener {
            rawInput += "-"
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnMultiplySci).setOnClickListener {
            rawInput += "*"
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnDivideSci).setOnClickListener {
            rawInput += "/"
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnPower).setOnClickListener {
            rawInput += "^"
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnClearSci).setOnClickListener {
            rawInput = ""
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnDelSci).setOnClickListener {
            if (rawInput.isNotEmpty()) rawInput = rawInput.substring(0, rawInput.length - 1)
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnPercentSci).setOnClickListener {
            try {
                val v = rawInput.toDouble()
                rawInput = formatResult(v / 100.0)
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnEqualsSci).setOnClickListener { onEquals() }
        findViewById<View>(R.id.btnLeftParen).setOnClickListener {
            rawInput += "("
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnRightParen).setOnClickListener {
            rawInput += ")"
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnPi).setOnClickListener {
            rawInput += Math.PI.toString()
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnSin).setOnClickListener {
            try {
                rawInput = formatResult(Math.sin(rawInput.toDouble()))
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnCos).setOnClickListener {
            try {
                rawInput = formatResult(Math.cos(rawInput.toDouble()))
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnTan).setOnClickListener {
            try {
                rawInput = formatResult(Math.tan(rawInput.toDouble()))
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnLog).setOnClickListener {
            try {
                rawInput = formatResult(Math.log10(rawInput.toDouble()))
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnLn).setOnClickListener {
            try {
                rawInput = formatResult(Math.log(rawInput.toDouble()))
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnSqrt).setOnClickListener {
            try {
                rawInput = formatResult(Math.sqrt(rawInput.toDouble()))
            } catch (ignored: Exception) {
            }
            updateDisplayScientific()
        }
    }

    private fun onEquals() {
        val input = rawInput.trim()
        when (input) {
            SECRET_CODE -> {
                openMainApp()
                return
            }
            "000" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, SecretCodesActivity::class.java))
                return
            }
            "111" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, CrashHistoryActivity::class.java))
                return
            }
            "222" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, ChangelogActivity::class.java))
                return
            }
            "808" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, DownloadListActivity::class.java))
                return
            }
            "888" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, StatistikActivity::class.java))
                return
            }
            "102" -> {
                rawInput = ""
                throw RuntimeException("Force Crash - User triggered via secret code 102")
            }
            "103" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                TestGestureHelper.showGuide(this)
                return
            }
            "104" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                TestModesHelper.showChoice(this)
                return
            }
            "101" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                DeveloperModeStore.toggle(this)
                return
            }
            "123" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                CacheResetter.confirmAndReset(this)
                return
            }
            "456" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                RepairDatabaseHelper.confirmAndRepair(this)
                return
            }
            "444" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                TestMediaHelper.showChoice(this)
                return
            }
            "789" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                FactoryResetHelper.confirmAndReset(this)
                return
            }
            "777" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, StorageAnalyzerActivity::class.java))
                return
            }
            "999" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                ProjectExportHelper.export(this)
                return
            }
            "333" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, SystemInfoActivity::class.java))
                return
            }
            "555" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, NetworkInfoActivity::class.java))
                return
            }
            "666" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                startActivity(Intent(this, PermissionInfoActivity::class.java))
                return
            }
            "200" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
                return
            }
            "201" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Tidak didukung di HP ini", Toast.LENGTH_SHORT).show()
                }
                return
            }
            "8889" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                testJsHttp()
                return
            }
            "8888" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                testJsRhino()
                return
            }
            "202" -> {
                rawInput = ""
                updateDisplay()
                updateDisplayScientific()
                try {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Tidak didukung di HP ini", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

        try {
            val result = evaluate(rawInput)
            val resultStr = formatResult(result)
            rawInput = resultStr
            updateDisplay()
            updateDisplayScientific()
        } catch (e: Exception) {
            rawInput = ""
            tvDisplay?.text = "Error"
            tvDisplayScientific?.text = "Error"
        }
    }

    private fun openMainApp() {
        rawInput = ""
        updateDisplay()
        updateDisplayScientific()
        try {
            SessionManager.markAuthPassed()
        } catch (ignored: Exception) {
        }
        try {
            val lastClass = SessionState.lastActivityClass
            if (lastClass != null && lastClass.contains("VideoPlayerActivity")) {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtra("index", SessionState.lastVideoIndex)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                SessionState.clear()
                startActivity(intent)
                return
            }
        } catch (ignored: Exception) {
        }
        SessionState.clear()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun updateDisplay() {
        tvDisplay?.text = if (rawInput.isEmpty()) "0" else rawInput
    }

    private fun updateDisplayScientific() {
        tvDisplayScientific?.text = if (rawInput.isEmpty()) "0" else rawInput
    }

    private fun formatResult(value: Double): String {
        return if (value == Math.floor(value) && !value.isInfinite()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private fun evaluate(expr: String): Double {
        return ExprParser(expr).parse()
    }

    private class ExprParser(private val s: String) {
        private var pos = 0

        fun parse(): Double {
            var result = parseTerm()
            while (pos < s.length) {
                val c = s[pos]
                if (c == '+') {
                    pos++
                    result += parseTerm()
                } else if (c == '-') {
                    pos++
                    result -= parseTerm()
                } else {
                    break
                }
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parsePower()
            while (pos < s.length) {
                val c = s[pos]
                if (c == '*') {
                    pos++
                    result *= parsePower()
                } else if (c == '/') {
                    pos++
                    result /= parsePower()
                } else {
                    break
                }
            }
            return result
        }

        private fun parsePower(): Double {
            var result = parseFactor()
            while (pos < s.length) {
                val c = s[pos]
                if (c == '^') {
                    pos++
                    result = Math.pow(result, parseFactor())
                } else {
                    break
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            val start = pos
            if (pos < s.length && s[pos] == '-') {
                pos++
                return -parseFactor()
            }
            while (pos < s.length && (Character.isDigit(s[pos]) || s[pos] == '.')) {
                pos++
            }
            if (start == pos) throw RuntimeException("Invalid expression")
            return s.substring(start, pos).toDouble()
        }
    }
}
