package com.example.proyecto_divisa.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRate(exchangeRate: ExchangeRate)

    // Obtener la última tasa de cambio
    @Query("SELECT * FROM exchange_rate ORDER BY lastUpdate DESC LIMIT 1")
    suspend fun getLastExchangeRate(): ExchangeRate?

    // Consulta para obtener tasas de cambio en un rango de fechas
    @Query("SELECT * FROM exchange_rate WHERE lastUpdate BETWEEN :startDate AND :endDate ORDER BY lastUpdate ASC")
    suspend fun getExchangeRatesInRange(startDate: Long, endDate: Long): List<ExchangeRate>

    @Query("SELECT * FROM exchange_rate ORDER BY lastUpdate ASC")
    suspend fun getAllExchangeRates(): List<ExchangeRate>

    @Query("SELECT * FROM exchange_rate WHERE lastUpdate = :timestamp LIMIT 1")
    suspend fun getExchangeRateByTimestamp(timestamp: Long): ExchangeRate?


}