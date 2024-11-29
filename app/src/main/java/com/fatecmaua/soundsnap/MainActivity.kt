package com.fatecmaua.soundsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fatecmaua.soundsnap.adapter.MyAdapter
import com.fatecmaua.soundsnap.databinding.ActivityMainBinding
import com.fatecmaua.soundsnap.models.AlbumItem
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotifyApi
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotify
import com.fatecmaua.soundsnap.servicos.getAuthorizationHeader
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var usernameText: TextView
    private lateinit var logoutButton: Button

    private lateinit var binding: ActivityMainBinding
    lateinit var tokenSpotify: String
    val albumItems = mutableListOf<AlbumItem>()
    val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    val adapter = MyAdapter(albumItems, mutableSetOf()) { albumId -> handleLikeButtonClick(albumId) }
    private var loadingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usernameText = findViewById(R.id.usernameText)
        logoutButton = findViewById(R.id.logoutButton)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)
        usernameText.text = "Olá, $savedUsername"

        if (savedUsername == null) {
            redirectToLogin()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (loadingJob == null && totalItemCount <= lastVisibleItem + 10) {
                    loadingJob = lifecycleScope.launch {
                        loadMoreData()
                        loadingJob = null
                    }
                }
            }
        })

        lifecycleScope.launch {
            tokenSpotify = obterTokenSpotify() ?: run {
                Log.e("MainActivity", "Falha ao obter token do Spotify")
                return@launch
            }
            loadMoreData()
        }
    }

    override fun onResume() {
        super.onResume()
        logoutButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().remove("username").apply()
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private suspend fun loadMoreData() {
        val newAlbums = getRand(tokenSpotify, "album")
        if (!newAlbums.isNullOrEmpty()) {
            albumItems.addAll(newAlbums)
            adapter.notifyDataSetChanged()
        }
    }

    suspend fun getRand(token: String, obj: String): List<AlbumItem>? {
        val albumDataList = mutableListOf<AlbumItem>()
        repeat(2) {
            val randomSearch = generateRandomSearch()
            val offset = Random.nextInt(1000)

            try {
                val response = RetrofitSpotifyApi.servicosSpotifyApi.search(
                    authorization = "Bearer $token",
                    query = randomSearch,
                    offset = offset,
                    limit = 1,
                    type = obj,
                    market = "NL"
                )

                if (response.isSuccessful) {
                    Log.d("MyAdapter", "Resposta da API: ${response.body()}")
                    response.body()?.albums?.items?.mapNotNull { albumItem ->
                        AlbumItem(
                            id = albumItem.id,
                            name = albumItem.name,
                            release_date = albumItem.release_date,
                            album_type = albumItem.album_type,
                            total_tracks = albumItem.total_tracks,
                            images = albumItem.images,
                            artists = albumItem.artists,
                            isLiked = false // Inicializa sem likes
                        )
                    }?.let { albumDataList.addAll(it) }
                } else {
                    Log.e("MainActivity", "Erro na API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erro ao buscar álbuns: ${e.message}", e)
            }
        }
        return albumDataList
    }

    suspend fun obterTokenSpotify(): String? {
        return try {
            RetrofitSpotify.servicosSpotify.getAccessToken(getAuthorizationHeader())
                .takeIf { it.isSuccessful }
                ?.body()?.accessToken
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao obter token: ${e.message}")
            null
        }
    }

    private fun handleLikeButtonClick(albumId: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)

        Log.d("Like", "Clicou no botão de like para o álbum: $albumId")
        Log.d("MainActivity", "Nome de usuário recuperado: $savedUsername")

        if (savedUsername != null) {
            lifecycleScope.launch {
                if (isAlbumLiked(albumId, savedUsername)) {
                    updateLike(albumId, savedUsername, false)
                    Log.d("Like", "Deslike no álbum: $albumId")
                } else {
                    updateLike(albumId, savedUsername, true)
                    Log.d("Like", "Like no álbum: $albumId")
                }
            }
        }
    }

    // Função para verificar se o álbum já está nos "likes" do usuário utilizando um for
    private suspend fun isAlbumLiked(albumId: String, username: String): Boolean {
        val user = try {
            // Busca o usuário através do Retrofit
            RetrofitClient.servicosFastAPI.buscarUsuario(username)
        } catch (e: Exception) {
            Log.e("isAlbumLiked", "Erro ao buscar usuário: ${e.message}")
            null
        }

        // Se o usuário foi encontrado e a lista de likes não for null
        user?.likes?.let { likes ->
            // Usando um loop for para iterar sobre os likes
            for (like in likes) {
                if (like == albumId) {
                    return true  // Se o álbum está nos likes, retorna true
                }
            }
        }

        return false  // Se o álbum não estiver nos likes, retorna false
    }

    private fun addLike(user: Usuario, albumId: String): List<String> {
        return user.likes.toMutableList().apply { add(albumId) }
    }

    private fun removeLike(user: Usuario, albumId: String): List<String> {
        return user.likes.toMutableList().apply { remove(albumId) }
    }

    private fun getUserFromDatabase(username: String?): Usuario? {
        if (username == null) return null

        val userJson = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("user", null) ?: return null

        return Gson().fromJson(userJson, Usuario::class.java)
    }

    private fun generateRandomSearch(): String {
        val randomCharacter = ('a'..'z').random()
        return "%$randomCharacter%"
    }

    private suspend fun updateLike(albumId: String, username: String, isLike: Boolean) {
        try {
            val response = if (isLike) {
                RetrofitClient.servicosFastAPI.addLikes(username, albumId).awaitResponse()
            } else {
                RetrofitClient.servicosFastAPI.removeLike(username, albumId).awaitResponse()
            }

            if (response.isSuccessful) {
                Log.d("Like", if (isLike) "Like adicionado" else "Like removido")

                // Atualiza o usuário no SharedPreferences após sucesso
                var user = getUserFromDatabase(username)
                if (user != null) {
                    val updatedLikes = if (isLike) {
                        addLike(user, albumId)
                    } else {
                        removeLike(user, albumId)
                    }
                    user.likes = updatedLikes

                    // Salva o usuário atualizado no SharedPreferences
                    val updatedUserJson = Gson().toJson(user)
                    getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("user", updatedUserJson)
                        .apply()
                }
            } else {
                Log.e("Like", "Erro na requisição: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("Like", "Erro ao atualizar like: ${e.message}")
        }
    }
}
