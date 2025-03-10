package com.example.proyecto_divisa.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_divisa.data.ExchangeRateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.proyecto_divisa.worker.scheduleExchangeRateWork
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExchangeRateViewModel(application: Application) : AndroidViewModel(application) {

    private val _exchangeRates = MutableStateFlow<String?>(null)
    val exchangeRates: StateFlow<String?> = _exchangeRates

    private val _lastUpdate = MutableStateFlow<String?>(null)
    val lastUpdate: StateFlow<String?> = _lastUpdate

    private val _nextUpdate = MutableStateFlow<String?>(null)
    val nextUpdate: StateFlow<String?> = _nextUpdate

    private val _selectedCurrency = MutableStateFlow("USD")
    val selectedCurrency: StateFlow<String> = _selectedCurrency

    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)

    // Estado para los datos de la gráfica
    private val _chartData = MutableStateFlow<List<Float>?>(null)
    val chartData: StateFlow<List<Float>?> = _chartData

    // SharedPreferences
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("ExchangeRatePrefs", Context.MODE_PRIVATE)

    fun setSelectedCurrency(currency: String) {
        _selectedCurrency.value = currency
        fetchChartData() // Actualizar datos de la gráfica
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        _startDate.value = startDate
        _endDate.value = endDate
        fetchChartData() // Actualizar datos de la gráfica
    }

    private fun fetchChartData() {
        viewModelScope.launch {
            val startDate = _startDate.value
            val endDate = _endDate.value
            val currency = _selectedCurrency.value

            Log.d("ExchangeRateViewModel", "Moneda seleccionada: $currency")
            Log.d("ExchangeRateViewModel", "Fecha de inicio: $startDate")
            Log.d("ExchangeRateViewModel", "Fecha de fin: $endDate")

            if (startDate != null && endDate != null) {
                val uri = ExchangeRateProvider.CONTENT_URI
                val cursor: Cursor? = getApplication<Application>().contentResolver.query(
                    uri,
                    null,
                    null,
                    arrayOf(startDate.toString(), endDate.toString()),
                    null
                )

                cursor?.use {
                    val rates = mutableListOf<Float>()
                    val conversionRatesJsonIndex = it.getColumnIndex("conversionRatesJson")
                    if (conversionRatesJsonIndex >= 0) {
                        while (it.moveToNext()) {
                            val conversionRatesJson = it.getString(conversionRatesJsonIndex)
                            Log.d("ExchangeRateViewModel", "JSON crudo: $conversionRatesJson")

                            val parsedRates = parseJson(conversionRatesJson, currency)
                            Log.d("ExchangeRateViewModel", "Datos parseados: $parsedRates")

                            rates.addAll(parsedRates)
                        }
                    }
                    _chartData.value = rates // Actualizar los datos de la gráfica
                    Log.d("ExchangeRateViewModel", "Datos de la gráfica: $rates")
                }
            } else {
                Log.d("ExchangeRateViewModel", "Fechas no válidas")
            }
        }
    }

    init {
        viewModelScope.launch {
            scheduleExchangeRateWork(application.applicationContext).collect { result ->
                val exchangeRatesJson = result.first
                val lastUpdateTime = result.second

                _exchangeRates.value = exchangeRatesJson
                _lastUpdate.value = lastUpdateTime

                val nextUpdateMillis = sharedPreferences.getLong("next_update", 0)
                if (nextUpdateMillis > 0) {
                    _nextUpdate.value = formatTime(nextUpdateMillis)
                }
            }
        }

        // Inicializar chartData al inicio
        fetchChartData()
    }

    private fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun parseJson(json: String, currency: String): List<Float> {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Float>>() {}.type
        val ratesMap: Map<String, Float> = gson.fromJson(json, type)
        return ratesMap[currency]?.let { listOf(it) } ?: emptyList()
    }
}