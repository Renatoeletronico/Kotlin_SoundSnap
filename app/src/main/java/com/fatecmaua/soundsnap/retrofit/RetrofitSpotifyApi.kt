package com.fatecmaua.soundsnap.retrofit

import com.fatecmaua.soundsnap.servicos.ServicosAPiSpotify
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSpotifyApi {
    private const val BASE_URL = "https://api.spotify.com/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val servicosSpotifyApi: ServicosAPiSpotify = retrofit.create(ServicosAPiSpotify::class.java)
}
