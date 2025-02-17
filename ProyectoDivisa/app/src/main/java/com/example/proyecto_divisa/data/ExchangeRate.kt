package com.example.proyecto_divisa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rate") // Asegúrate de que el nombre de la tabla sea "exchange_rate"
data class ExchangeRate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // La columna id es la clave primaria
    val conversionRatesJson: String,  // Las tasas de cambio en formato JSON
    val lastUpdate: Long  // Marca de tiempo de la última actualización
)
