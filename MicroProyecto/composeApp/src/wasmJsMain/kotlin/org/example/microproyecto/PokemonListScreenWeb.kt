package org.example.microproyecto

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun PokemonListScreenWeb() {
    val pokemonList = rememberPokemonList()

    Style(PokemonStyles)

    Div({
        classes(PokemonStyles.container)
    }) {
        pokemonList.forEach { pokemon ->
            val id = pokemon.url.trimEnd('/').split("/").last()
            val name = pokemon.name.replaceFirstChar { it.uppercase() }
            val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

            Div({
                classes(PokemonStyles.card)
            }) {
                Img(src = imageUrl, alt = name, attrs = {
                    classes(PokemonStyles.image)
                })
                P {
                    Text(name)
                }
            }
        }
    }
}
