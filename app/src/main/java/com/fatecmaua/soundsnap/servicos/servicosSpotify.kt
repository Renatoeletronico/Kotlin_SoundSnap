package com.fatecmaua.soundsnap.servicos

import android.util.Base64
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import com.fatecmaua.soundsnap.models.TokenResponse
import com.fatecmaua.soundsnap.models.spotifyCredentials
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ServicosAPiSpotify {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<TokenResponse>

    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 1,
        @Query("type") type: String,
        @Query("market") market: String
    ): ResponseBody
    }

fun getAuthorizationHeader(): String {
    val credentials = "${spotifyCredentials.CLIENT_ID}:${spotifyCredentials.CLIENT_SECRET}"
    return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
}
