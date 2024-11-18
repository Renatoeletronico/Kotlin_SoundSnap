package com.fatecmaua.soundsnap.retrofit

import com.fatecmaua.soundsnap.servicos.ServicosFastAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://spotifyapi-hct0.onrender.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val servicosFastAPI: ServicosFastAPI = retrofit.create(ServicosFastAPI::class.java)
}




