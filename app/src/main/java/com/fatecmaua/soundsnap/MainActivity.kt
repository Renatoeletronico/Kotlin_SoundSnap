package com.fatecmaua.soundsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
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
import com.fatecmaua.soundsnap.models.Albuns

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

    private lateinit var usernameText: TextView
    private lateinit var logoutButton: Button

    private lateinit var binding: ActivityMainBinding
    lateinit var tokenSpotify: String
    val albumItems = mutableListOf<AlbumItem>() // Sua lista de álbuns
    val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    val adapter = MyAdapter(albumItems) // Passa a lista de álbuns para o adapter
    private var isLoading = false // Flag para evitar múltiplos carregamentos simultâneos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Referências para os componentes
        usernameText = findViewById(R.id.usernameText)
        logoutButton = findViewById(R.id.logoutButton)

        // Verificar se o usuário está logado
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)
        // Atualizando o TextView com o nome do usuário
        val usernameTextView: TextView = findViewById(R.id.usernameText)
        usernameTextView.text = savedUsername
        if (savedUsername == null) {
            // Se não houver nome de usuário salvo, redireciona para a LoginActivity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Fecha a MainActivity para que o usuário não possa voltar a ela
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Adicionando o ScrollListener para detectar quando o usuário chega ao final
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Verificando se estamos perto do final da lista
                if (!isLoading && totalItemCount <= lastVisibleItem + 10) {
                    // Inicia uma coroutine para carregar mais dados
                    lifecycleScope.launch {
                        repeat(10){
                            loadMoreData() // Carregar mais dados
                        }

                    }
                }
            }
        })

        lifecycleScope.launch {
            // Obtendo o token do Spotify
            val token = obterTokenSpotify()
            if (token != null) {
                tokenSpotify = token
                Log.d("MainActivity", "Token obtido: $token")

                // Carregando dados iniciais
                loadMoreData()
            } else {
                Log.e("MainActivity", "Falha ao obter o token.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Botão de deslogar
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        logoutButton.setOnClickListener {
            // Remover o nome do usuário do SharedPreferences
            val editor = sharedPreferences.edit()
            editor.remove("username")
            editor.apply()

            // Redirecionar para a LoginActivity
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // Fecha a MainActivity
        }
    }

    // Função para carregar mais dados
    private suspend fun loadMoreData() {
        if (isLoading) return // Evita múltiplos carregamentos simultâneos
        isLoading = true // Marca como carregando

        val albumData = getRand(tokenSpotify, "album")
        albumData?.let {
            albumItems.addAll(it) // Adiciona os álbuns obtidos à lista
            adapter.notifyDataSetChanged() // Notifica o adapter para atualizar a visualização
        }

        isLoading = false // Desmarca como carregando
    }

    suspend fun getRand(token: String, obj: String): List<AlbumItem>? {
        val albumDataList = mutableListOf<AlbumItem>()

        repeat(2) {
            val randomSearch = generateRandomSearch() // Gera uma busca aleatória
            val getRandomOffset = Random.nextInt(1000) // Gera um offset aleatório

            try {
                val response = RetrofitSpotifyApi.servicosSpotifyApi.search(
                    authorization = "Bearer $token",
                    query = randomSearch,
                    offset = getRandomOffset,
                    limit = 1,
                    type = obj,
                    market = "NL"
                )

                if (response.isSuccessful) {
                    val spotifyResponse = response.body()
                    Log.d("MainActivity", "Resposta da API: $spotifyResponse")

                    val albumItems = spotifyResponse?.albums?.items?.mapNotNull { albumItem ->
                        AlbumItem(
                            name = albumItem.name,
                            release_date = albumItem.release_date,
                            album_type = albumItem.album_type,
                            total_tracks = albumItem.total_tracks,
                            images = albumItem.images,
                            artists = albumItem.artists
                        )
                    }

                    albumDataList.addAll(albumItems ?: emptyList())
                } else {
                    Log.e("MainActivity", "Erro na resposta da API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Falha na requisição: ${e.message}", e)
            }
        }

        return if (albumDataList.isNotEmpty()) albumDataList else null
    }

    suspend fun obterTokenSpotify(): String? {
        val authorizationHeader = getAuthorizationHeader()

        return try {
            val response = RetrofitSpotify.servicosSpotify.getAccessToken(authorizationHeader)

            if (response.isSuccessful) {
                response.body()?.accessToken
            } else {
                Log.e("MainActivity", "Erro ao obter token: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Falha na requisição Spotify: ", e)
            null
        }
    }

    fun generateRandomSearch(): String {
        val randomCharacter = getRandomCharacter()
        return when (Random.nextInt(3)) {
            0 -> "$randomCharacter%"
            1 -> "%$randomCharacter%"
            else -> "%$randomCharacter"
        }
    }

    fun getRandomCharacter(): Char {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return characters[Random.nextInt(characters.length)]
    }
}
