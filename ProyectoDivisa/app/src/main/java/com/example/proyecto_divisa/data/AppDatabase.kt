package com.example.proyecto_divisa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExchangeRate::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "exchange_rate_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
