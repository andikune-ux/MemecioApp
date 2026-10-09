package com.memecio.app

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlin.math.*

class CalculatorActivity : Activity() {

    private var tvDisplay: TextView? = null
    private var tvDisplayScientific: TextView? = null
    private var rawInput: String = ""
    private var isScientific: Boolean = false
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
            findViewById<TextView>(id)?.setOnClickListener { v ->
                rawInput += (v as TextView).text.toString()
                updateDisplay()
            }
        }

        findViewById<View>(R.id.btnDot)?.setOnClickListener { rawInput += "."; updateDisplay() }
        findViewById<View>(R.id.btnPlus)?.setOnClickListener { rawInput += "+"; updateDisplay() }
        findViewById<View>(R.id.btnMinus)?.setOnClickListener { rawInput += "-"; updateDisplay() }
        findViewById<View>(R.id.btnMultiply)?.setOnClickListener { rawInput += "*"; updateDisplay() }
        findViewById<View>(R.id.btnDivide)?.setOnClickListener { rawInput += "/"; updateDisplay() }

        findViewById<View>(R.id.btnClear)?.setOnClickListener { rawInput = ""; updateDisplay() }
        findViewById<View>(R.id.btnDel)?.setOnClickListener {
            if (rawInput.isNotEmpty()) {
                rawInput = rawInput.substring(0, rawInput.length - 1)
            }
            updateDisplay()
        }
        findViewById<View>(R.id.btnPercent)?.setOnClickListener {
            try {
                val v = rawInput.toDouble()
                rawInput = formatResult(v / 100.0)
            } catch (ignored: Exception) {}
            updateDisplay()
        }
        findViewById<View>(R.id.btnEquals)?.setOnClickListener { onEquals() }
    }

    private fun bindScienceButtons() {
        val digitIds = intArrayOf(
            R.id.btn0Sci, R.id.btn1Sci, R.id.btn2Sci, R.id.btn3Sci, R.id.btn4Sci,
            R.id.btn5Sci, R.id.btn6Sci, R.id.btn7Sci, R.id.btn8Sci, R.id.btn9Sci, R.id.btn00Sci
        )
        for (id in digitIds) {
            findViewById<TextView>(id)?.setOnClickListener { v ->
                rawInput += (v as TextView).text.toString()
                updateDisplayScientific()
            }
        }

        findViewById<View>(R.id.btnDotSci)?.setOnClickListener { rawInput += "."; updateDisplayScientific() }
        findViewById<View>(R.id.btnPlusSci)?.setOnClickListener { rawInput += "+"; updateDisplayScientific() }
        findViewById<View>(R.id.btnMinusSci)?.setOnClickListener { rawInput += "-"; updateDisplayScientific() }
        findViewById<View>(R.id.btnMultiplySci)?.setOnClickListener { rawInput += "*"; updateDisplayScientific() }
        findViewById<View>(R.id.btnDivideSci)?.setOnClickListener { rawInput += "/"; updateDisplayScientific() }
        findViewById<View>(R.id.btnPower)?.setOnClickListener { rawInput += "^"; updateDisplayScientific() }

        findViewById<View>(R.id.btnClearSci)?.setOnClickListener { rawInput = ""; updateDisplayScientific() }
        findViewById<View>(R.id.btnDelSci)?.setOnClickListener {
            if (rawInput.isNotEmpty()) {
                rawInput = rawInput.substring(0, rawInput.length - 1)
            }
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnPercentSci)?.setOnClickListener {
            try {
                val v = rawInput.toDouble()
                rawInput = formatResult(v / 100.0)
            } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnEqualsSci)?.setOnClickListener { onEquals() }

        findViewById<View>(R.id.btnLeftParen)?.setOnClickListener { rawInput += "("; updateDisplayScientific() }
        findViewById<View>(R.id.btnRightParen)?.setOnClickListener { rawInput += ")"; updateDisplayScientific() }
        findViewById<View>(R.id.btnPi)?.setOnClickListener { rawInput += PI.toString(); updateDisplayScientific() }
        findViewById<View>(R.id.btnSin)?.setOnClickListener {
            try { rawInput = formatResult(sin(rawInput.toDouble())) } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnCos)?.setOnClickListener {
            try { rawInput = formatResult(cos(rawInput.toDouble())) } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnTan)?.setOnClickListener {
            try { rawInput = formatResult(tan(rawInput.toDouble())) } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnLog)?.setOnClickListener {
            try { rawInput = formatResult(log10(rawInput.toDouble())) } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnLn)?.setOnClickListener {
            try { rawInput = formatResult(ln(rawInput.toDouble())) } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
        findViewById<View>(R.id.btnSqrt)?.setOnClickListener {
            try { rawInput = formatResult(sqrt(rawInput.toDouble())) } catch (ignored: Exception) {}
            updateDisplayScientific()
        }
    }

    private fun onEquals() {
        val input = rawInput.trim()
        if (input == SECRET_CODE) {
            openMainApp()
            return
        }

        // Kode-kode rahasia
        when (input) {
            "000" -> {
                clearInput()
                startActivity(Intent(this, SecretCodesActivity::class.java))
                return
            }
            "111" -> {
                clearInput()
                startActivity(Intent(this, CrashHistoryActivity::class.java))
                return
            }
            "222" -> {
                clearInput()
                startActivity(Intent(this, ChangelogActivity::class.java))
                return
            }
            "808" -> {
                clearInput()
                startActivity(Intent(this, DownloadListActivity::class.java))
                return
            }
            "888" -> {
                clearInput()
                startActivity(Intent(this, StatistikActivity::class.java))
                return
            }
            "102" -> {
                clearInput()
                throw RuntimeException("Force Crash — User triggered via secret code 102")
            }
            "103" -> {
                clearInput()
                TestGestureHelper.showGuide(this)
                return
            }
            "104" -> {
                clearInput()
                TestModesHelper.showChoice(this)
                return
            }
            "101" -> {
                clearInput()
                DeveloperModeStore.toggle(this)
                return
            }
            "123" -> {
                clearInput()
                CacheResetter.confirmAndReset(this)
                return
            }
            "456" -> {
                clearInput()
                RepairDatabaseHelper.confirmAndRepair(this)
                return
            }
            "444" -> {
                clearInput()
                TestMediaHelper.showChoice(this)
                return
            }
            "789" -> {
                clearInput()
                FactoryResetHelper.confirmAndReset(this)
                return
            }
            "777" -> {
                clearInput()
                startActivity(Intent(this, StorageAnalyzerActivity::class.java))
                return
            }
            "999" -> {
                clearInput()
                ProjectExportHelper.export(this)
                return
            }
            "333" -> {
                clearInput()
                startActivity(Intent(this, SystemInfoActivity::class.java))
                return
            }
            "555" -> {
                clearInput()
                startActivity(Intent(this, NetworkInfoActivity::class.java))
                return
            }
            "666" -> {
                clearInput()
                startActivity(Intent(this, PermissionInfoActivity::class.java))
                return
            }
            "200" -> {
                clearInput()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return
            }
            "201" -> {
                clearInput()
                try {
                    startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(this, "Tidak didukung di HP ini", Toast.LENGTH_SHORT).show()
                }
                return
            }
            "202" -> {
                clearInput()
                try {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Tidak didukung di HP ini", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

        // Kalkulasi matematika normal
        try {
            val result = evaluate(rawInput)
            rawInput = formatResult(result)
            updateDisplay()
            updateDisplayScientific()
        } catch (e: Exception) {
            rawInput = ""
            tvDisplay?.text = "Error"
            tvDisplayScientific?.text = "Error"
        }
    }

    private fun clearInput() {
        rawInput = ""
        updateDisplay()
        updateDisplayScientific()
    }

    private fun openMainApp() {
        clearInput()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun updateDisplay() {
        tvDisplay?.text = if (rawInput.isEmpty()) "0" else rawInput
    }

    private fun updateDisplayScientific() {
        tvDisplayScientific?.text = if (rawInput.isEmpty()) "0" else rawInput
    }

    private fun formatResult(v: Double): String {
        return if (v == floor(v) && !v.isInfinite() && !v.isNaN()) {
            v.toLong().toString()
        } else {
            v.toString()
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
                if (c == '+') { pos++; result += parseTerm() }
                else if (c == '-') { pos++; result -= parseTerm() }
                else break
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parsePower()
            while (pos < s.length) {
                val c = s[pos]
                if (c == '*') { pos++; result *= parsePower() }
                else if (c == '/') { pos++; result /= parsePower() }
                else break
            }
            return result
        }

        private fun parsePower(): Double {
            var result = parseFactor()
            while (pos < s.length) {
                val c = s[pos]
                if (c == '^') { pos++; result = result.pow(parseFactor()) }
                else break
            }
            return result
        }

        private fun parseFactor(): Double {
            if (pos >= s.length) throw RuntimeException("Unexpected end of expression")
            if (s[pos] == '(') {
                pos++
                val res = parse()
                if (pos < s.length && s[pos] == ')') pos++
                return res
            }
            val start = pos
            if (s[pos] == '-') { pos++; return -parseFactor() }
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) {
                pos++
            }
            if (start == pos) throw RuntimeException("Invalid expression at pos $pos")
            return s.substring(start, pos).toDouble()
        }
    }
}
