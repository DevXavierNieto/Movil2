package org.example.microproyecto

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

@Composable
fun PokemonListScreenWeb() {
    val scope = rememberCoroutineScope()
    var pokemonList by remember { mutableStateOf<List<PokemonEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            pokemonList = PokemonApi.getPokemonList()
        }
    }

    Style(PokemonStyles)

    Div({
        classes(PokemonStyles.container)
    }) {
        pokemonList.forEach { pokemon ->
            val id = pokemon.url.trimEnd('/').split("/").last()
            Div({
                classes(PokemonStyles.card)
            }) {
                Img(src = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                    alt = pokemon.name,
                    attrs = {
                        classes(PokemonStyles.image)
                    })
                P {
                    Text(pokemon.name.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}
