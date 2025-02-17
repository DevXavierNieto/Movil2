package com.example.proyecto_divisa

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_divisa.data.AppDatabase
import com.example.proyecto_divisa.data.ExchangeRate
import com.example.proyecto_divisa.ui.theme.Proyecto_DivisaTheme
import com.example.proyecto_divisa.worker.scheduleExchangeRateWork
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Proyecto_DivisaTheme {
                var exchangeRates by remember { mutableStateOf<String?>(null) }
                var lastUpdate by remember { mutableStateOf<String?>(null) }
                var nextUpdate by remember { mutableStateOf<String?>(null) }

                // Obtener la base de datos y DAO
                val database = AppDatabase.getDatabase(this@MainActivity)
                val dao = database.exchangeRateDao()

                LaunchedEffect(Unit) {
                    // Obtener la última tasa de cambio de la base de datos
                    val lastExchangeRate = dao.getLastExchangeRate()
                    if (lastExchangeRate != null) {
                        exchangeRates = lastExchangeRate.conversionRatesJson
                        lastUpdate = formatTime(lastExchangeRate.lastUpdate)
                    }

                    // Ejecutar el worker y sincronizar
                    scheduleExchangeRateWork(this@MainActivity).collectLatest { (rates, lastUpdateTime) ->
                        exchangeRates = rates
                        lastUpdate = lastUpdateTime

                        // Si necesitas actualizar la próxima actualización (puedes usar Room también)
                        val sharedPreferences = getSharedPreferences("ExchangeRatePrefs", Context.MODE_PRIVATE)
                        val updatedNextUpdateMillis = sharedPreferences.getLong("next_update", 0)
                        if (updatedNextUpdateMillis > 0) {
                            nextUpdate = formatTime(updatedNextUpdateMillis)
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
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

// Composable actualizado con los nuevos mensajes
@Composable
fun Greeting(name: String, exchangeRates: String?, lastUpdate: String?, nextUpdate: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Bienvenido a la app de $name!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "📅 Última actualización: ${lastUpdate ?: "Cargando..."}")
        Text(text = "⏳ Próxima actualización: ${nextUpdate ?: "Cargando..."}")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "💱 Tasas de cambio:\n${exchangeRates ?: "Cargando..."}")
    }
}

// Función para formatear la fecha en MainActivity
fun formatTime(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timeInMillis)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Proyecto_DivisaTheme {
        Greeting("Divisas", null, "14:00:00 10/02/2025", "15:00:00 10/02/2025")
    }
}
