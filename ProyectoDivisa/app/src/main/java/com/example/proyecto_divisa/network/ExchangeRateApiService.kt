package com.example.proyecto_divisa.network

import com.example.proyecto_divisa.data.ExchangeRatesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApiService {
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    fun getExchangeRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String
    ): Call<ExchangeRatesResponse>
}