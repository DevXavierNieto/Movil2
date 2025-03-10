@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.proyecto_divisa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_divisa.ui.theme.Proyecto_DivisaTheme
import com.example.proyecto_divisa.viewmodel.ExchangeRateViewModel
import com.example.proyecto_divisa.worker.scheduleExchangeRateWork
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.*

// Importaciones adicionales para solucionar los errores
import androidx.compose.ui.Alignment // Para Alignment
import androidx.compose.foundation.shape.RoundedCornerShape // Para RoundedCornerShape
import androidx.compose.material3.ButtonDefaults // Para ButtonDefaults
import androidx.compose.material3.OutlinedButton // Para OutlinedButton
import androidx.compose.material3.Icon // Para Icon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat

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
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                val chartData by viewModel.chartData.collectAsState() // Observar chartData

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CurrencyDropdown { currency ->
                            viewModel.setSelectedCurrency(currency)
                        }

                        DateSelectors { startDate, endDate ->
                            viewModel.setDateRange(startDate, endDate)
                        }

                        // Mostrar la gráfica si hay datos
                        if (chartData != null) {
                            ShowExchangeRateChart(chartData)
                        } else {
                            Text("Cargando datos...", modifier = Modifier.padding(16.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(onCurrencySelected: (String) -> Unit) {
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "CHF")
    var expanded by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(currencies[0]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            readOnly = true,
            value = selectedCurrency,
            onValueChange = { },
            label = { Text("Selecciona una divisa") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        selectedCurrency = currency
                        expanded = false
                        onCurrencySelected(currency)
                    }
                )
            }
        }
    }
}

@Composable
fun DateSelectors(onDateRangeSelected: (Long, Long) -> Unit) {
    val context = LocalContext.current
    var beforeDate by remember { mutableStateOf("") }
    var afterDate by remember { mutableStateOf("") }

    fun formatDate(day: Int, month: Int, year: Int): String {
        return "$day/${month + 1}/$year"
    }

    val calendar = Calendar.getInstance()
    val today = formatDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

    val yesterdayCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -1)
    }
    val yesterday = formatDate(yesterdayCalendar.get(Calendar.DAY_OF_MONTH), yesterdayCalendar.get(Calendar.MONTH), yesterdayCalendar.get(Calendar.YEAR))

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                onDateSelected(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = {
                showDatePicker { selectedDate ->
                    beforeDate = selectedDate
                    val startDate = parseDateToMillis(selectedDate, isStartDate = true)
                    val endDate = parseDateToMillis(afterDate, isStartDate = false)
                    if (startDate != -1L && endDate != -1L) {
                        onDateRangeSelected(startDate, endDate)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Fecha Antes"
                )
                Text("Antes: ${beforeDate.ifEmpty { yesterday }}")
            }
        }

        OutlinedButton(
            onClick = {
                showDatePicker { selectedDate ->
                    afterDate = selectedDate
                    val startDate = parseDateToMillis(beforeDate, isStartDate = true)
                    val endDate = parseDateToMillis(selectedDate, isStartDate = false)
                    if (startDate != -1L && endDate != -1L) {
                        onDateRangeSelected(startDate, endDate)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Fecha Después"
                )
                Text("Después: ${afterDate.ifEmpty { today }}")
            }
        }
    }
}

fun parseDateToMillis(date: String, isStartDate: Boolean): Long {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val parsedDate = format.parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate

        // Ajustar la hora según sea fecha de inicio o fin
        if (isStartDate) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)  // 00:00 horas
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23) // 23:59 horas
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
        }

        calendar.timeInMillis
    } catch (e: Exception) {
        Log.e("Fechas", "Error al parsear la fecha: $date", e)
        -1
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
        Text(text = "💱 Tasas de cambio:\n${exchangeRates ?: "Cargando..."}")
    }
}

@Composable
fun ShowExchangeRateChart(chartData: List<Float>?) {
    // Verificar si hay datos
    if (chartData.isNullOrEmpty()) {
        Text("No hay datos para mostrar", modifier = Modifier.padding(16.dp))
    } else {
        // Convertir los datos a entradas para la gráfica
        val entries = chartData.mapIndexed { index, rate ->
            Entry(index.toFloat(), rate)
        }

        // Mostrar la gráfica
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    val dataSet = LineDataSet(entries, "Tasa de cambio")
                    dataSet.color = 0xFF6200EE.toInt()
                    dataSet.valueTextColor = 0xFF6200EE.toInt()
                    dataSet.valueTextSize = 14f
                    dataSet.setDrawCircles(true) // Mostrar puntos en la gráfica
                    dataSet.setDrawValues(true) // Mostrar valores en la gráfica
                    val lineData = LineData(dataSet)
                    this.data = lineData
                    this.invalidate() // Actualizar la gráfica
                    this.description.isEnabled = false
                    this.setTouchEnabled(true) // Habilitar interacción con la gráfica
                    this.setPinchZoom(true) // Habilitar zoom
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            update = { chart ->
                // Limpiar la gráfica antes de agregar nuevos datos
                chart.clear()
                val dataSet = LineDataSet(entries, "Tasa de cambio")
                dataSet.color = 0xFF6200EE.toInt()
                dataSet.valueTextColor = 0xFF6200EE.toInt()
                dataSet.valueTextSize = 14f
                dataSet.setDrawCircles(true)
                dataSet.setDrawValues(true)
                val lineData = LineData(dataSet)
                chart.data = lineData
                chart.invalidate() // Actualizar la gráfica
            }
        )

        // Forzar la actualización de la gráfica cuando cambien los datos
        LaunchedEffect(chartData) {
            // Este bloque se ejecutará cada vez que cambie chartData
        }
    }
}

fun parseExchangeRatesToEntries(json: String, selectedCurrency: String): List<Entry> {
    val entries = mutableListOf<Entry>()
    val exchangeRates = parseJson(json, selectedCurrency)
    exchangeRates.forEachIndexed { index, rate ->
        entries.add(Entry(index.toFloat(), rate.toFloat()))
    }
    return entries
}

private fun parseJson(json: String, currency: String): List<Float> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Float>>() {}.type
    val ratesMap: Map<String, Float> = gson.fromJson(json, type)
    return ratesMap[currency]?.let { listOf(it) } ?: emptyList()
}