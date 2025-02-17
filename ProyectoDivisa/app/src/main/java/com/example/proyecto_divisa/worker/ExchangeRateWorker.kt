import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.proyecto_divisa.data.AppDatabase
import com.example.proyecto_divisa.data.ExchangeRate
import com.example.proyecto_divisa.data.ExchangeRatesResponse
import com.example.proyecto_divisa.network.RetrofitClient
import com.google.gson.Gson
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
            val database = AppDatabase.getDatabase(applicationContext) // Instancia de la base de datos Room
            val dao = database.exchangeRateDao() // DAO para acceder a la base de datos

            try {
                // Realizar la llamada a la API
                val response: Response<ExchangeRatesResponse> =
                    RetrofitClient.instance.getExchangeRates(apiKey, baseCurrency).execute()

                // Verificar si la respuesta fue exitosa
                if (response.isSuccessful) {
                    val exchangeRates = response.body()

                    if (exchangeRates != null) {
                        // Convertir el Map de conversion_rates a JSON
                        val conversionRatesJson = Gson().toJson(exchangeRates.conversion_rates)

                        // Crear el objeto ExchangeRate con las tasas de cambio en formato JSON
                        val exchangeRate = ExchangeRate(
                            conversionRatesJson = conversionRatesJson,
                            lastUpdate = System.currentTimeMillis()
                        )

                        // Insertar en la base de datos
                        dao.insertExchangeRate(exchangeRate)

                        // Devolver los datos como resultado
                        val outputData = Data.Builder()
                            .putString("exchange_rates", conversionRatesJson)
                            .build()

                        // Aquí solo retornamos el Result fuera de la operación asincrónica
                        return@withContext Result.success(outputData)
                    } else {
                        return@withContext Result.failure()
                    }
                } else {
                    return@withContext Result.failure()
                }
            } catch (e: Exception) {
                return@withContext Result.failure()
            }
        }
    }
}
