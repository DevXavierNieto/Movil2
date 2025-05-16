package org.example.microproyecto

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PokemonListResponse(val results: List<PokemonEntry>)

object PokemonApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getPokemonList(): List<PokemonEntry> {
        val response: PokemonListResponse =
            client.get("https://pokeapi.co/api/v2/pokemon?limit=151").body()
        return response.results
    }
}