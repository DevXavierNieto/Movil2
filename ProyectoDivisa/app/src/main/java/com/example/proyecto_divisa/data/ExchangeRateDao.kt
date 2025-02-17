package com.example.proyecto_divisa.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExchangeRateDao {

    @Insert
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate)

    // Consulta para obtener la última tasa de cambio
    @Query("SELECT * FROM exchange_rate ORDER BY lastUpdate DESC LIMIT 1")
    suspend fun getLastExchangeRate(): ExchangeRate?
}
