package org.example.microproyecto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonEntry(
    val name: String,
    val url: String
)
