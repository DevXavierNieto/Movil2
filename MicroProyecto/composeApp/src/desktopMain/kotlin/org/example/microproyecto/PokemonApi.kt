package org.example.microproyecto

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class PokemonListResponse(val results: List<PokemonEntry>)

actual object PokemonApi {
    actual suspend fun getPokemonList(): List<PokemonEntry> {
        val responseText = window.fetch("https://pokeapi.co/api/v2/pokemon?limit=151")
            .await()
            .text()
            .await()

        val parsed = Json { ignoreUnknownKeys = true }
            .decodeFromString<PokemonListResponse>(responseText)

        return parsed.results
    }
}
