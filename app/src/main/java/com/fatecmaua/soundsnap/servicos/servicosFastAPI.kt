package com.fatecmaua.soundsnap.servicos
import com.fatecmaua.soundsnap.models.LoginResponse
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

data class LoginRequest(
    val usuario: String,
    val senha: String
)

interface ServicosFastAPI {
    @GET("users/{username}")
    suspend fun buscarUsuario(@Path("username") username: String): Usuario

    // POST para criar um usuário
    @POST("users")
    fun createUser(@Body user: Usuario): Call<Usuario>

    @POST("login/")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @PUT("addlike/{user_id}/{album_id}")
    fun addLikes(
        @Path("user_id") userId: String,
        @Path("album_id") albumId: String
    ): Call<Void>

    @DELETE("removelike/{user_id}/{album_id}")
    fun removeLike(
        @Path("user_id") userId: String,
        @Path("album_id") albumId: String
    ): Call<Void>
}




