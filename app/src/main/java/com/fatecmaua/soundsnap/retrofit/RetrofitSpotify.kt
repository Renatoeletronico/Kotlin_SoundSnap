package com.fatecmaua.soundsnap.retrofit


import com.fatecmaua.soundsnap.servicos.ServicosAPiSpotify
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSpotify {
   private const val BASE_URL = "https://accounts.spotify.com/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(RetrofitSpotify.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val servicosSpotify: ServicosAPiSpotify = retrofit.create(ServicosAPiSpotify::class.java)
}