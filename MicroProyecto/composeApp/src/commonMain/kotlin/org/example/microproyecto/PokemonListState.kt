package org.example.microproyecto

import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
fun rememberPokemonList(): List<PokemonEntry> {
    var pokemonList by remember { mutableStateOf<List<PokemonEntry>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            pokemonList = PokemonApi.getPokemonList()
        }
    }

    return pokemonList
}
