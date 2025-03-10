package com.example.proyecto_divisa.worker

import ExchangeRateWorker
import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

fun scheduleExchangeRateWork(context: Context): Flow<Pair<String?, String?>> {
    return callbackFlow {
        val workManager = WorkManager.getInstance(context)
        val sharedPreferences = context.getSharedPreferences("ExchangeRatePrefs", Context.MODE_PRIVATE)

        // Calcular la próxima hora exacta + 1 minuto
        val now = Calendar.getInstance()
        val nextUpdateTime = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1) // Siguiente hora
            set(Calendar.MINUTE, 1) // +1 minuto
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val delay = nextUpdateTime.timeInMillis - now.timeInMillis

        // Guardar la próxima actualización en SharedPreferences
        sharedPreferences.edit().putLong("next_update", nextUpdateTime.timeInMillis).apply()

        // Encolar el trabajo para obtener tasas de cambio de inmediato
        val immediateWorkRequest = OneTimeWorkRequest.Builder(ExchangeRateWorker::class.java)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueue(immediateWorkRequest)

        // Worker para sincronizar con la hora exacta + 1 minuto
        val initialWorkRequest = OneTimeWorkRequest.Builder(ExchangeRateWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueue(initialWorkRequest)

        // Worker periódico cada hora en punto + 1 minuto
        val periodicWorkRequest = PeriodicWorkRequest.Builder(ExchangeRateWorker::class.java, 1, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "ExchangeRateWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )

        // Observar el resultado del Worker
        val observer = androidx.lifecycle.Observer<WorkInfo?> { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                val exchangeRates = workInfo.outputData.getString("exchange_rates")

                // Guardar la última ejecución en SharedPreferences
                val lastUpdateTime = System.currentTimeMillis()
                sharedPreferences.edit().putLong("last_update", lastUpdateTime).apply()

                trySend(Pair(exchangeRates, formatTime(lastUpdateTime)))
            }
        }

        workManager.getWorkInfoByIdLiveData(immediateWorkRequest.id).observeForever(observer)

        awaitClose {
            workManager.getWorkInfoByIdLiveData(immediateWorkRequest.id).removeObserver(observer)
        }
    }
}


//Formatear la fecha
private fun formatTime(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timeInMillis)
}
