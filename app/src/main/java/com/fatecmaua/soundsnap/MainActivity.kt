package com.fatecmaua.soundsnap

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fatecmaua.soundsnap.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotify
import com.fatecmaua.soundsnap.models.TokenResponse
import com.fatecmaua.soundsnap.servicos.getAuthorizationHeader
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotifyApi
import kotlinx.coroutines.launch
import retrofit2.await
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    public lateinit var tokenSpotify: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            // Aqui você aguarda o token ser obtido
            val token = obterTokenSpotify()

            if (token != null) {
                Log.d("MainActivity", "Token obtido: $token")
                // Agora você pode usar o token, por exemplo, chamando getUsuario(token)
                token.also { tokenSpotify = it }
            } else {
                Log.e("MainActivity", "Falha ao obter o token.")
            }
            val result = getRand(tokenSpotify, "album")
            result?.let {
                Log.d("MainActivity", "Resultado do getRand: $it")
            }

        }
        // Acessando views diretamente
        binding.btntest.setOnClickListener {
            getUsuario()
            Log.d("MainActivity", "Token obtido: $tokenSpotify")

        }
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun getRandomCharacter(): Char {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return characters[Random.nextInt(characters.length)]
    }

    fun generateRandomSearch(): String {
        val randomCharacter = getRandomCharacter()
        return when (Random.nextInt(3)) {
            0 -> "$randomCharacter%25"
            1 -> "%25$randomCharacter%25"
            else -> "%25$randomCharacter"
        }
    }

    suspend fun getRand(token: String, obj: String): String? {
        val randomSearch = generateRandomSearch()
        val getRandomOffset = Random.nextInt(1000)

        return try {
            val response = RetrofitSpotifyApi.servicosSpotifyApi.search(
                authorization = "Bearer $token",
                query = randomSearch,
                offset = getRandomOffset,
                type = obj,
                market = "NL"
            )
            response.string()
        } catch (e: Exception) {
            Log.e("MainActivity", "Falha na requisição ${e.message}", e)
            null
        }
    }

    suspend fun obterTokenSpotify(): String? {
        val authorizationHeader = getAuthorizationHeader()

        return try {
            // Fazendo a chamada suspensa para obter o token
            val response = RetrofitSpotify.servicosSpotify.getAccessToken(authorizationHeader)

            // Verificando se a resposta foi bem-sucedida
            if (response.isSuccessful) {
                response.body()?.accessToken  // Use 'accessToken' conforme definido na classe TokenResponse
            } else {
                Log.e("MainActivity", "Erro ao obter token: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            // Tratando exceções em caso de falha na requisição
            Log.e("MainActivity", "Falha na requisição Spotify: ", e)
            null
        }
    }



   private fun getUsuario (){
        RetrofitClient.servicosFastAPI.buscarUsuario("Renato").enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    val usuario = response.body()
                    if (usuario != null) {
                        Log.d("MainActivity", "Usuário recebido: $usuario")

                        println("usuario" + usuario.toString())
                    } else {
                        Log.w("MainActivity", "Resposta vazia do servidor spotify")

                    }
                } else {
                    Log.e("MainActivity", "Erro na requisição spotify: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Log.e("MainActivity", "Falha na requisição", t)
                println("falha na requisição")
            }
        })
    }

}
