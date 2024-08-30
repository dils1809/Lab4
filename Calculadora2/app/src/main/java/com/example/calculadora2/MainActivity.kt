package com.example.calculadora2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.CircleShape // Importa la forma circular

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var input by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display result
        Text(
            text = result.ifEmpty { input },
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.End
        )

        // Calculator Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly // Espacio uniforme entre las filas
        ) {
            val buttons = listOf(
                listOf("7", "8", "9", "/"),
                listOf("4", "5", "6", "*"),
                listOf("1", "2", "3", "-"),
                listOf("C", "0", "=", "+")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // Espacio uniforme entre los botones
                ) {
                    row.forEach { label ->
                        Button(
                            onClick = {
                                handleButtonClick(label, input, result) { newInput, newResult ->
                                    input = newInput
                                    result = newResult
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Para que los botones sean perfectamente redondos
                                .padding(4.dp), // Espacio mínimo entre botones
                            shape = CircleShape, // Forma circular
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Color naranja
                        ) {
                            Text(label, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun handleButtonClick(
    label: String,
    currentInput: String,
    currentResult: String,
    updateState: (String, String) -> Unit
) {
    when (label) {
        "C" -> updateState("0", "")
        "=" -> {
            try {
                val evaluatedResult = eval(currentInput)
                updateState(evaluatedResult.toString(), "")
            } catch (e: Exception) {
                updateState("", "Error")
            }
        }
        else -> {
            val newInput = if (currentInput == "0") label else currentInput + label
            updateState(newInput, "")
        }
    }
}

fun eval(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch: Char = ' '

        fun nextChar() {
            ch = if (++pos < str.length) str[pos] else '\u0000'
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: $ch")
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> x /= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            when {
                eat('+') -> return parseFactor()
                eat('-') -> return -parseFactor()
            }

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if (ch in '0'..'9' || ch == '.') {
                while (ch in '0'..'9' || ch == '.') nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: $ch")
            }
            return x
        }
    }.parse()
}

@Preview(showBackground = true)
@Composable
fun PreviewCalculatorApp() {
    CalculatorTheme {
        CalculatorApp()
    }
}

