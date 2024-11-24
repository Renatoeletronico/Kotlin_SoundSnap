package com.fatecmaua.soundsnap.models

// Modelo principal que representa a resposta da API do Spotify
data class SpotifyResponse(
    val albums: Albums // Contém a lista de álbuns
)

// Modelo para a lista de álbuns
data class Albums(
    val items: List<AlbumItem> // Lista de itens de álbuns
)

// Modelo de um item de álbum
data class AlbumItem(
    val name: String, // Nome do álbum
    val release_date: String, // Data de lançamento do álbum
    val album_type: String, // Tipo do álbum (ex.: "single", "album")
    val total_tracks: Int, // Total de faixas
    val images: List<Image>, // Lista de imagens do álbum
    val artists: List<Artist> // Lista de artistas
)

// Modelo de um artista
data class Artist(
    val name: String // Nome do artista
)

// Modelo de uma imagem
data class Image(
    val url: String // URL da imagem
)
