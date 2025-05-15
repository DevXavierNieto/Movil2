package org.example.microproyecto

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PokemonListScreen() {
    var pokemonList by remember { mutableStateOf<List<PokemonEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        pokemonList = PokemonApi.getPokemonList()
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(pokemonList) { pokemon ->
            PokemonCard(pokemon)
        }
    }
}