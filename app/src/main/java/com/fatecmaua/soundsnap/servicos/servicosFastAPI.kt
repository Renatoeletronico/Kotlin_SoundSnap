package com.fatecmaua.soundsnap.servicos
import com.fatecmaua.soundsnap.models.LoginResponse
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

data class LoginRequest(
    val username: String,
    val password: String
)

interface ServicosFastAPI {
    @GET("users/{username}")
    fun buscarUsuario(@Path("username") username: String): Call<Usuario>

    @POST("login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>
}




