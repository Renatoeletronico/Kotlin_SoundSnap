package com.fatecmaua.soundsnap.models

data class SpotifyResponse(
    val albums: Albums
)

data class Albums(
    val items: List<AlbumItem>
)

data class AlbumItem(
    val artists: List<Artist>
)

data class Artist(
    val name: String
)