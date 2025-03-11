package com.example.proyecto_divisa.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.runBlocking

class ExchangeRateProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.proyecto_divisa.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/exchange_rate")
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val database = context?.let { AppDatabase.getDatabase(it) } ?: return null
        val dao = database.exchangeRateDao()

        val exchangeRates = runBlocking {
            val startDate = selectionArgs?.getOrNull(0)?.toLongOrNull()
            val endDate = selectionArgs?.getOrNull(1)?.toLongOrNull()

            val result = if (startDate != null && endDate != null) {
                dao.getExchangeRatesInRange(startDate, endDate)
            } else {
                dao.getAllExchangeRates()
            }

            Log.d("ExchangeRateProvider", "Datos obtenidos desde BD: $result")
            result
        }

        val cursor = MatrixCursor(arrayOf("id", "conversionRatesJson", "lastUpdate"))
        exchangeRates.forEach {
            cursor.addRow(arrayOf(it.id, it.conversionRatesJson, it.lastUpdate))
        }
        return cursor
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Insert operation is not supported")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Update operation is not supported")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Delete operation is not supported")
    }

    override fun getType(uri: Uri): String? {
        return "vnd.android.cursor.dir/vnd.$AUTHORITY.exchange_rate"
    }
}