package com.example.proyecto_divisa.worker

import android.content.Context
import androidx.work.*
import com.example.proyecto_divisa.data.ExchangeRatesResponse
import com.example.proyecto_divisa.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ExchangeRateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val apiKey = "df8a3b73eaf32b000b2f0018"
            val baseCurrency = "MXN"

            try {
                val response: Response<ExchangeRatesResponse> =
                    RetrofitClient.instance.getExchangeRates(apiKey, baseCurrency).execute()

                if (response.isSuccessful) {
                    val exchangeRates = response.body()
                    if (exchangeRates != null) {
                        // Guardar la tasa de cambio inicial en SharedPreferences
                        val sharedPreferences = applicationContext.getSharedPreferences("ExchangeRatePrefs", Context.MODE_PRIVATE)
                        val initialRate = exchangeRates.conversion_rates["USD"]?.toString() // Guardamos la tasa USD/MXN
                        sharedPreferences.edit().putString("initial_rate", initialRate).apply()

                        // Pasar las tasas de cambio actuales en el formato necesario
                        val exchangeRatesString = exchangeRates.conversion_rates.entries.joinToString("\n") {
                            "${it.key}: ${it.value} ${baseCurrency}"
                        }

                        val outputData = Data.Builder()
                            .putString("exchange_rates", exchangeRatesString)
                            .build()
                        Result.success(outputData)
                    } else {
                        Result.failure()
                    }
                } else {
                    Result.failure()
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}
