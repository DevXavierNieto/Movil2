package com.example.proyecto_divisa.worker

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

        // Obtener o guardar la hora de entrada
        val entryTimeMillis = sharedPreferences.getLong("entry_time", 0)
        if (entryTimeMillis == 0L) {
            val nowMillis = System.currentTimeMillis()
            sharedPreferences.edit().putLong("entry_time", nowMillis).apply()
        }

        val now = Calendar.getInstance()
        val nextUpdateTime = Calendar.getInstance().apply {
            timeInMillis = sharedPreferences.getLong("entry_time", now.timeInMillis)
            add(Calendar.HOUR_OF_DAY, 1) // Próxima actualización +1 hora desde la entrada
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val delay = nextUpdateTime.timeInMillis - now.timeInMillis

        sharedPreferences.edit().putLong("next_update", nextUpdateTime.timeInMillis).apply()

        val immediateWorkRequest = OneTimeWorkRequest.Builder(ExchangeRateWorker::class.java)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        workManager.enqueue(immediateWorkRequest)

        val periodicWorkRequest = PeriodicWorkRequest.Builder(ExchangeRateWorker::class.java, 1, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniquePeriodicWork("ExchangeRateWorker", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest)

        val observer = androidx.lifecycle.Observer<WorkInfo?> { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                val exchangeRates = workInfo.outputData.getString("exchange_rates")
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

private fun formatTime(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timeInMillis)
}
