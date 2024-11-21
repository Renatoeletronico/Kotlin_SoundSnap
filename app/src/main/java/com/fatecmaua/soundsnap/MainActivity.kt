package com.fatecmaua.soundsnap

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatecmaua.soundsnap.adapter.MyAdapter
import com.fatecmaua.soundsnap.databinding.ActivityMainBinding
import com.fatecmaua.soundsnap.models.AlbumItem

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotify
import com.fatecmaua.soundsnap.models.TokenResponse
import com.fatecmaua.soundsnap.servicos.getAuthorizationHeader
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotifyApi
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.await
import kotlin.random.Random
import com.fatecmaua.soundsnap.models.SpotifyResponse

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var tokenSpotify: String
    var artistNamesList = mutableListOf<String>()
    lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MyAdapter(artistNamesList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val token = obterTokenSpotify()

            if (token != null) {
                Log.d("MainActivity", "Token obtido: $token")
                token.also { tokenSpotify = it }
            } else {
                Log.e("MainActivity", "Falha ao obter o token.")
            }

            val result = getRand(tokenSpotify, "album")
            result?.let {
                Log.d("MainActivity", "Resultado do getRand: $it")
                // Atualiza a lista de artistas e notifica o RecyclerView

                adapter.notifyDataSetChanged()
            }
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
            0 -> "$randomCharacter%"
            1 -> "%$randomCharacter%"
            else -> "%$randomCharacter"
        }
    }

    suspend fun getRand(token: String, obj: String): List<String>? {

        repeat(5) {
            val randomSearch = generateRandomSearch()
            val getRandomOffset = Random.nextInt(1000)
            try {
                // Realizando a chamada para a API do Spotify
                val response = RetrofitSpotifyApi.servicosSpotifyApi.search(
                    authorization = "Bearer $token",
                    query = randomSearch,
                    offset = getRandomOffset,
                    limit = 1, // Definindo o limite para 1 álbum
                    type = obj,
                    market = "NL"
                )

                // Verificando se a resposta foi bem-sucedida
                if (response.isSuccessful) {
                    val spotifyResponse = response.body() // Obtemos o corpo da resposta que já é do tipo SpotifyResponse

                    // Extraindo os nomes dos artistas
                    val artistNames = spotifyResponse?.albums?.items?.flatMap { albumItem: AlbumItem ->
                        albumItem.artists.map { artist ->
                            artist.name
                        }
                    }

                    // Adicionando os nomes dos artistas na lista
                    artistNames?.let {
                        artistNamesList.addAll(it)  // Adiciona os nomes extraídos à lista
                    }
                } else {
                    Log.e("MainActivity", "Erro na resposta da API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Falha na requisição: ${e.message}", e)
            }
        }

        // Retorna a lista com os nomes dos artistas
        return if (artistNamesList.isNotEmpty()) artistNamesList else null
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
