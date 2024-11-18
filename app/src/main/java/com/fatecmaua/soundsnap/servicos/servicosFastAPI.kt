package com.fatecmaua.soundsnap.servicos
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*


interface ServicosFastAPI {
    @GET("users/{username}")
    fun buscarUsuario(@Path("username") username: String): Call<Usuario>
}