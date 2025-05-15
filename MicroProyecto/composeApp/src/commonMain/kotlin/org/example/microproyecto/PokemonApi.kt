package org.example.microproyecto

expect object PokemonApi {
    suspend fun getPokemonList(): List<PokemonEntry>
}
