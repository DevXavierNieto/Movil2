package com.example.proyecto_divisa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rate")
data class ExchangeRate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversionRatesJson: String,  // JSON de tasas de cambio
    val lastUpdate: Long  // Timestamp de la última actualización
)
