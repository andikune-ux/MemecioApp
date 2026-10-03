package com.memecio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memecio.app.ui.theme.*

@Composable
fun CalculatorScreen(
    currentPin: String,
    onUnlock: () -> Unit,
    onSecretCode: (String) -> Unit
) {
    var rawInput by remember { mutableStateOf("") }
    var isScientific by remember { mutableStateOf(false) }

    fun handleKey(char: String) {
        rawInput += char
    }

    fun handleClear() {
        rawInput = ""
    }

    fun handleDel() {
        if (rawInput.isNotEmpty()) {
            rawInput = rawInput.substring(0, rawInput.length - 1)
        }
    }

    fun handleEquals() {
        val trimmed = rawInput.trim()
        if (trimmed == currentPin) {
            rawInput = ""
            onUnlock()
            return
        }

        // Check secret codes
        val knownCodes = setOf("000", "111", "222", "333", "444", "555", "666", "777", "888", "999", "123", "456", "789", "101", "103", "104", "808")
        if (knownCodes.contains(trimmed)) {
            val code = trimmed
            rawInput = ""
            onSecretCode(code)
            return
        }

        // Normal calculator evaluation
        try {
            val result = evaluateExpression(rawInput)
            rawInput = formatResult(result)
        } catch (_: Exception) {
            rawInput = "Error"
        }
    }

    Scaffold(
        containerColor = Color(0xFF0C0A14),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculator",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isScientific = !isScientific },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isScientific) Icons.Default.Calculate else Icons.Default.Science,
                            contentDescription = "Toggle Scientific",
                            tint = if (isScientific) NeonCyan else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onSecretCode("000") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Codes",
                            tint = TextMuted
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Display area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF141124), RoundedCornerShape(18.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (rawInput.isEmpty()) "0" else rawInput,
                        color = Color.White,
                        fontSize = if (rawInput.length > 12) 32.sp else 46.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        modifier = Modifier.testTag("calculator_display")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad grid
            if (isScientific) {
                ScientificKeypad(
                    onKey = { handleKey(it) },
                    onClear = { handleClear() },
                    onDel = { handleDel() },
                    onEquals = { handleEquals() }
                )
            } else {
                StandardKeypad(
                    onKey = { handleKey(it) },
                    onClear = { handleClear() },
                    onDel = { handleDel() },
                    onEquals = { handleEquals() }
                )
            }
        }
    }
}

@Composable
private fun StandardKeypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onDel: () -> Unit,
    onEquals: () -> Unit
) {
    val rows = listOf(
        listOf("C", "DEL", "%", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { btn ->
                    CalcButton(
                        text = btn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (btn) {
                                "C" -> onClear()
                                "DEL" -> onDel()
                                "=" -> onEquals()
                                else -> onKey(btn)
                            }
                        },
                        isSpecial = btn in listOf("C", "DEL", "%"),
                        isOperator = btn in listOf("/", "*", "-", "+"),
                        isEquals = btn == "="
                    )
                }
            }
        }
    }
}

@Composable
private fun ScientificKeypad(
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onDel: () -> Unit,
    onEquals: () -> Unit
) {
    val rows = listOf(
        listOf("C", "DEL", "(", ")", "^"),
        listOf("sin", "cos", "tan", "sqrt", "/"),
        listOf("7", "8", "9", "log", "*"),
        listOf("4", "5", "6", "ln", "-"),
        listOf("1", "2", "3", "pi", "+"),
        listOf("00", "0", ".", "%", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { btn ->
                    CalcButton(
                        text = btn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (btn) {
                                "C" -> onClear()
                                "DEL" -> onDel()
                                "=" -> onEquals()
                                "pi" -> onKey("3.14159")
                                else -> onKey(btn)
                            }
                        },
                        isSpecial = btn in listOf("C", "DEL", "%", "(", ")", "pi"),
                        isOperator = btn in listOf("/", "*", "-", "+", "^", "sin", "cos", "tan", "sqrt", "log", "ln"),
                        isEquals = btn == "=",
                        isSmallFont = btn.length > 2
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isSpecial: Boolean = false,
    isOperator: Boolean = false,
    isEquals: Boolean = false,
    isSmallFont: Boolean = false
) {
    val bgColor = when {
        isEquals -> NeonPink
        isOperator -> NeonViolet.copy(alpha = 0.85f)
        isSpecial -> Color(0xFF2C2448)
        else -> DarkCard
    }

    val textColor = when {
        isEquals -> Color.White
        isOperator -> Color.White
        isSpecial -> NeonCyan
        else -> TextPrimary
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(if (isSmallFont) 1.25f else 1.15f)
            .testTag("calc_btn_$text"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (isSmallFont) 14.sp else 22.sp,
            fontWeight = if (isOperator || isEquals) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun formatResult(value: Double): String {
    return if (value == Math.floor(value) && !value.isInfinite()) {
        value.toLong().toString()
    } else {
        String.format("%.4f", value).trimEnd('0').trimEnd('.')
    }
}

private class ExpressionParser(private val clean: String) {
    private var pos = 0

    fun parse(): Double {
        return parseTerm()
    }

    private fun parseFactor(): Double {
        if (pos >= clean.length) return 0.0
        if (clean[pos] == '-') {
            pos++
            return -parseFactor()
        }
        if (clean[pos] == '+') {
            pos++
            return parseFactor()
        }
        if (clean[pos] == '(') {
            pos++
            val res = parseTerm()
            if (pos < clean.length && clean[pos] == ')') pos++
            return res
        }
        for (fn in listOf("sin", "cos", "tan", "sqrt", "log", "ln")) {
            if (clean.startsWith(fn, pos)) {
                pos += fn.length
                val arg = parseFactor()
                return when (fn) {
                    "sin" -> Math.sin(Math.toRadians(arg))
                    "cos" -> Math.cos(Math.toRadians(arg))
                    "tan" -> Math.tan(Math.toRadians(arg))
                    "sqrt" -> Math.sqrt(arg)
                    "log" -> Math.log10(arg)
                    "ln" -> Math.log(arg)
                    else -> arg
                }
            }
        }
        val start = pos
        while (pos < clean.length && (clean[pos].isDigit() || clean[pos] == '.')) {
            pos++
        }
        if (start == pos) return 0.0
        return clean.substring(start, pos).toDoubleOrNull() ?: 0.0
    }

    private fun parsePower(): Double {
        var x = parseFactor()
        while (pos < clean.length && clean[pos] == '^') {
            pos++
            val y = parseFactor()
            x = Math.pow(x, y)
        }
        return x
    }

    private fun parseTerm(): Double {
        var x = parsePower()
        while (pos < clean.length) {
            val op = clean[pos]
            if (op == '*' || op == '/' || op == '%') {
                pos++
                val y = parsePower()
                if (op == '*') x *= y
                else if (op == '/') x = if (y != 0.0) x / y else 0.0
                else if (op == '%') x = x * (y / 100.0)
            } else if (op == '+' || op == '-') {
                pos++
                val y = parsePower()
                if (op == '+') x += y else x -= y
            } else {
                break
            }
        }
        return x
    }
}

private fun evaluateExpression(expr: String): Double {
    return ExpressionParser(expr.replace(" ", "")).parse()
}
