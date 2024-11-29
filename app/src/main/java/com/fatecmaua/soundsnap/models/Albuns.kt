package com.fatecmaua.soundsnap.models

data class Albuns(
    val id : String,
    val name : String,
    val image : String,
    val release_date : String,
    val album_type : String,
    val total_tracks : String,
    var isliked : Boolean
)
