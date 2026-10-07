package com.memecio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memecio.app.ui.theme.CyberAccentPink
import com.memecio.app.ui.theme.CyberPrimary
import com.memecio.app.ui.theme.CyberPrimaryBright
import com.memecio.app.ui.theme.CyberSecondary
import com.memecio.app.ui.theme.DarkBackground
import com.memecio.app.ui.theme.DarkSurfaceCard
import com.memecio.app.ui.theme.DarkSurfaceVariant
import com.memecio.app.ui.theme.GlassBorder
import com.memecio.app.ui.theme.TextMuted
import com.memecio.app.ui.theme.TextPrimary
import com.memecio.app.ui.theme.TextSecondary
import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun CalculatorScreen(
    secretPin: String,
    onUnlock: () -> Unit,
    onSecretCodeTriggered: (String) -> Unit
) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var isScientific by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    fun onButtonClick(label: String) {
        notificationMessage = null
        when (label) {
            "AC" -> {
                expression = ""
                resultText = "0"
            }
            "DEL" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                val cleanExp = expression.trim()
                // Secret PIN check
                if (cleanExp == secretPin) {
                    expression = ""
                    resultText = "UNLOCKED"
                    onUnlock()
                    return
                }

                // Secret code triggers
                val knownCodes = setOf("000", "111", "222", "333", "444", "555", "666", "777", "888", "999", "101", "103", "104", "123", "456", "789", "808")
                if (knownCodes.contains(cleanExp)) {
                    expression = ""
                    resultText = "CODE: $cleanExp"
                    onSecretCodeTriggered(cleanExp)
                    return
                }

                // Normal math evaluation
                try {
                    val res = evaluateExpression(expression)
                    val df = DecimalFormat("#.########")
                    resultText = df.format(res)
                } catch (e: Exception) {
                    resultText = "Error"
                }
            }
            "sin" -> expression += "sin("
            "cos" -> expression += "cos("
            "tan" -> expression += "tan("
            "ln" -> expression += "ln("
            "log" -> expression += "log("
            "√" -> expression += "sqrt("
            "π" -> expression += "3.141592"
            "e" -> expression += "2.71828"
            "^" -> expression += "^"
            else -> {
                expression += label
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calculator_screen"),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kalkulator Ilmiah",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { isScientific = !isScientific }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Toggle Ilmiah",
                            tint = if (isScientific) CyberPrimaryBright else TextMuted
                        )
                    }

                    // Subtle shortcut for unlocking if user forgot PIN (helpful accessibility)
                    IconButton(
                        onClick = {
                            expression = secretPin
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Isi PIN Default",
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Display area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = resultText,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (resultText == "UNLOCKED") CyberTertiary else TextPrimary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                if (notificationMessage != null) {
                    Text(
                        text = notificationMessage ?: "",
                        color = CyberPrimaryBright,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Scientific extension keypad
            AnimatedVisibility(visible = isScientific) {
                val sciButtons = listOf(
                    "sin", "cos", "tan", "ln",
                    "log", "√", "^", "(",
                    ")", "π", "e", "C"
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sciButtons) { btn ->
                        CalcButton(
                            label = btn,
                            bgColor = DarkSurfaceVariant,
                            textColor = CyberSecondary,
                            onClick = { onButtonClick(if (btn == "C") "AC" else btn) }
                        )
                    }
                }
            }

            // Standard keypad
            val standardRows = listOf(
                listOf("AC", "DEL", "%", "/"),
                listOf("7", "8", "9", "*"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "(", "=")
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in standardRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in row) {
                            val isOp = item in listOf("/", "*", "-", "+")
                            val isAction = item in listOf("AC", "DEL", "%")
                            val isEquals = item == "="
                            val bgColor = when {
                                isEquals -> CyberPrimary
                                isOp -> DarkSurfaceVariant
                                isAction -> DarkSurfaceVariant.copy(alpha = 0.8f)
                                else -> DarkSurfaceCard
                            }
                            val textColor = when {
                                isEquals -> Color.White
                                isOp -> CyberPrimaryBright
                                isAction -> CyberAccentPink
                                else -> TextPrimary
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                CalcButton(
                                    label = item,
                                    bgColor = bgColor,
                                    textColor = textColor,
                                    isEquals = isEquals,
                                    onClick = { onButtonClick(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Discreet info footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ketik PIN ($secretPin) lalu '=' untuk membuka multimedia",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    bgColor: Color,
    textColor: Color,
    isEquals: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isEquals) CyberPrimaryBright else GlassBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("calc_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Hapus",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = if (label.length > 2) 15.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// Simple arithmetic & expression parser for calculator
fun evaluateExpression(expr: String): Double {
    var s = expr.replace(" ", "")
        .replace("x", "*")
        .replace("÷", "/")
        .replace("%", "*0.01")

    // Handle functions
    while (s.contains("sin(")) {
        s = evaluateFunction(s, "sin") { sin(Math.toRadians(it)) }
    }
    while (s.contains("cos(")) {
        s = evaluateFunction(s, "cos") { cos(Math.toRadians(it)) }
    }
    while (s.contains("tan(")) {
        s = evaluateFunction(s, "tan") { tan(Math.toRadians(it)) }
    }
    while (s.contains("ln(")) {
        s = evaluateFunction(s, "ln") { ln(it) }
    }
    while (s.contains("log(")) {
        s = evaluateFunction(s, "log") { log10(it) }
    }
    while (s.contains("sqrt(")) {
        s = evaluateFunction(s, "sqrt") { sqrt(it) }
    }

    return evalSimple(s)
}

private fun evaluateFunction(str: String, fnName: String, fn: (Double) -> Double): String {
    val startIndex = str.indexOf("$fnName(")
    if (startIndex == -1) return str
    var openCount = 0
    var endIndex = -1
    for (i in startIndex + fnName.length until str.length) {
        if (str[i] == '(') openCount++
        else if (str[i] == ')') {
            openCount--
            if (openCount == 0) {
                endIndex = i
                break
            }
        }
    }
    if (endIndex == -1) return str
    val inner = str.substring(startIndex + fnName.length + 1, endIndex)
    val innerVal = evaluateExpression(inner)
    val computed = fn(innerVal)
    return str.substring(0, startIndex) + computed + str.substring(endIndex + 1)
}

// Recursive parser for arithmetic
private fun evalSimple(str: String): Double {
    var pos = -1
    var ch = ' '

    fun nextChar() {
        pos++
        ch = if (pos < str.length) str[pos] else '\u0000'
    }

    fun eat(charToEat: Char): Boolean {
        while (ch == ' ') nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parseExpression(): Double {
        nextChar()
        var x = parseTerm()
        while (true) {
            if (eat('+')) x += parseTerm()
            else if (eat('-')) x -= parseTerm()
            else return x
        }
    }

    fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*')) x *= parseFactor()
            else if (eat('/')) x /= parseFactor()
            else return x
        }
    }

    fun parseFactor(): Double {
        if (eat('+')) return parseFactor()
        if (eat('-')) return -parseFactor()

        var x: Double
        val startPos = pos
        if (eat('(')) {
            x = parseExpression()
            eat(')')
        } else if ((ch in '0'..'9') || ch == '.') {
            while ((ch in '0'..'9') || ch == '.') nextChar()
            x = str.substring(startPos, pos).toDouble()
        } else {
            return 0.0
        }

        if (eat('^')) x = x.pow(parseFactor())
        return x
    }

    return parseExpression()
}
