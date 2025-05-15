package org.example.microproyecto

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PokemonCard(pokemon: PokemonEntry) {
    val name = pokemon.name.replaceFirstChar { it.uppercase() }
    val id = pokemon.url.trimEnd('/').split("/").last()
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PlatformImage(url = imageUrl, altText = name)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name)
        }
    }
}
