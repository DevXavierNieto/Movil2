package com.example.proyecto_divisa.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

fun scheduleExchangeRateWork(context: Context): Flow<String?> {
    return callbackFlow {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(ExchangeRateWorker::class.java)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        // Observa el resultado del Worker
        // Observa el resultado del Worker
        val observer = androidx.lifecycle.Observer<WorkInfo?> { workInfo ->
            if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                val exchangeRates = workInfo.outputData.getString("exchange_rates")
                trySend(exchangeRates) // Envía los datos al flujo
            }
        }


        // Registra el observer
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(workRequest.id)
            .observeForever(observer)

        // Limpia el observer cuando el flujo se cancela
        awaitClose {
            WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(workRequest.id)
                .removeObserver(observer)
        }
    }
}