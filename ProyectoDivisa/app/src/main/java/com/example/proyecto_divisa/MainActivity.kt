package com.example.proyecto_divisa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyecto_divisa.ui.theme.Proyecto_DivisaTheme
import com.example.proyecto_divisa.worker.scheduleExchangeRateWork
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Proyecto_DivisaTheme {
                var exchangeRates by remember { mutableStateOf<String?>(null) }

                // Programa el Worker para que se ejecute
                LaunchedEffect(Unit) {
                    scheduleExchangeRateWork(this@MainActivity).collectLatest { rates ->
                        exchangeRates = rates
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Divisas",
                        exchangeRates = exchangeRates,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, exchangeRates: String?, modifier: Modifier = Modifier) {
    Text(
        text = "Bienvenido a la app de $name!\n\nTasas de cambio:\n$exchangeRates",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Proyecto_DivisaTheme {
        Greeting("Divisas", null)
    }
}