package com.example.proyecto_divisa.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
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
                        // Devuelve los datos como resultado
                        val outputData = Data.Builder()
                            .putString("exchange_rates", exchangeRates.toString())
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