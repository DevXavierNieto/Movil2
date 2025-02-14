package com.example.proyecto_divisa

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_divisa.ui.theme.Proyecto_DivisaTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.proyecto_divisa.network.RetrofitClient
import com.example.proyecto_divisa.data.ExchangeRatesResponse
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Proyecto_DivisaTheme {
                var exchangeRates by remember { mutableStateOf<String?>(null) }
                var history by remember { mutableStateOf<List<String>>(emptyList()) }
                var lastUpdate by remember { mutableStateOf<String?>(null) }
                var nextUpdate by remember { mutableStateOf<String?>(null) }
                val sharedPreferences = getSharedPreferences("ExchangeRatePrefs", Context.MODE_PRIVATE)
                val apiKey = "df8a3b73eaf32b000b2f0018"  // Reemplaza con tu clave de API
                val baseCurrency = "MXN"

                val lastUpdateTime = sharedPreferences.getLong("last_update_time", 0L)
                val currentTime = System.currentTimeMillis()
                val historySet = sharedPreferences.getStringSet("exchange_rate_history", mutableSetOf()) ?: mutableSetOf()

                LaunchedEffect(Unit) {
                    if (currentTime - lastUpdateTime >= 3600000) {
                        val client = RetrofitClient.instance
                        val call = client.getExchangeRates(apiKey, baseCurrency)
                        call.enqueue(object : Callback<ExchangeRatesResponse> {
                            override fun onResponse(call: Call<ExchangeRatesResponse>, response: Response<ExchangeRatesResponse>) {
                                if (response.isSuccessful) {
                                    val exchangeRatesResponse = response.body()
                                    exchangeRatesResponse?.let {
                                        val newRates = it.conversion_rates.entries.joinToString("\n") {
                                            "${it.key}: ${it.value} ${baseCurrency}"
                                        }

                                        historySet.add("${formatTime(currentTime)}\n$newRates")
                                        sharedPreferences.edit().putStringSet("exchange_rate_history", historySet).apply()
                                        sharedPreferences.edit().putLong("last_update_time", currentTime).apply()

                                        exchangeRates = newRates
                                        history = historySet.sortedDescending()
                                        nextUpdate = formatTime(currentTime + 3600000)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<ExchangeRatesResponse>, t: Throwable) {
                                exchangeRates = "Error al obtener datos de la API"
                            }
                        })
                    } else {
                        exchangeRates = sharedPreferences.getString("current_exchange_rates", null)
                        history = historySet.sortedDescending()
                        lastUpdate = formatTime(lastUpdateTime)
                        nextUpdate = formatTime(lastUpdateTime + 3600000)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Greeting(
                            exchangeRates = exchangeRates,
                            history = history,
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
fun Greeting(
    exchangeRates: String?,
    history: List<String>,
    lastUpdate: String?,
    nextUpdate: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Bienvenido a la app de Divisas!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "📅 Última actualización: ${lastUpdate ?: "Cargando..."}")
        Text(text = "⏳ Próxima actualización: ${nextUpdate ?: "Cargando..."}")
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "📜 Historial de tasas de cambio:")
        history.forEach { rate ->
            Text(text = rate)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "💱 Tasas de cambio actuales:")
        Text(text = exchangeRates ?: "Cargando...")
    }
}

fun formatTime(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timeInMillis)
}
