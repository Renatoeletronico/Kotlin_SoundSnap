package com.fatecmaua.soundsnap.servicos

import android.util.Base64
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import com.fatecmaua.soundsnap.models.TokenResponse
import com.fatecmaua.soundsnap.models.spotifyCredentials
import retrofit2.http.GET
import retrofit2.http.Header

interface ServicosAPiSpotify {
    @FormUrlEncoded
    @POST("api/token")
    fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Call<TokenResponse>

    @FormUrlEncoded
    @GET("v1/search?query=")
    fun get_rand(
        @Header ("urlPesquisa") urlPesquisa: String)
        ): Call<String>

}
fun getAuthorizationHeader(): String {
    val credentials = "${spotifyCredentials.CLIENT_ID}:${spotifyCredentials.CLIENT_SECRET}"
    return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
}
