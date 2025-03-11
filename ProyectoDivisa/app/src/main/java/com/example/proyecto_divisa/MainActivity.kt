@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.proyecto_divisa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyecto_divisa.ui.theme.Proyecto_DivisaTheme
import com.example.proyecto_divisa.viewmodel.ExchangeRateViewModel
import com.example.proyecto_divisa.worker.scheduleExchangeRateWork
import androidx.compose.foundation.rememberScrollState

class MainActivity : ComponentActivity() {
    private val viewModel: ExchangeRateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleExchangeRateWork(applicationContext)

        setContent {
            Proyecto_DivisaTheme {
                val exchangeRates by viewModel.exchangeRates.collectAsState()
                val lastUpdate by viewModel.lastUpdate.collectAsState()
                val nextUpdate by viewModel.nextUpdate.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Greeting(
                            name = "Divisas",
                            exchangeRates = exchangeRates,
                            lastUpdate = lastUpdate,
                            nextUpdate = nextUpdate
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, exchangeRates: String?, lastUpdate: String?, nextUpdate: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Bienvenido a la app de $name!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "📅 Última actualización: ${lastUpdate ?: "Cargando..."}")
        Text(text = "⏳ Próxima actualización: ${nextUpdate ?: "Cargando..."}")
        Spacer(modifier = Modifier.height(16.dp))

        if (exchangeRates.isNullOrEmpty()) {
            Text(text = "❌ No se encontraron tasas de cambio. Verifica la base de datos o el ContentProvider.", color = MaterialTheme.colorScheme.error)
        } else {
            Text(text = "💱 Tasas de cambio:\n$exchangeRates")
        }
    }
}

