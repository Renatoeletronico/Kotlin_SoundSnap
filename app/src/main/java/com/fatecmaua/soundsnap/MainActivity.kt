package com.fatecmaua.soundsnap

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fatecmaua.soundsnap.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.fatecmaua.soundsnap.retrofit.RetrofitSpotify
import com.fatecmaua.soundsnap.models.TokenResponse
import com.fatecmaua.soundsnap.servicos.getAuthorizationHeader
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    public lateinit var tokenSpotify: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Acessando views diretamente
        binding.btntest.setOnClickListener {
            getUsuario()
            obterTokenSpotify()
        }
    }

    private fun configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun get_rand(token : String, obj : String){

        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val randomCharacter = characters.random()
        var ProcuraRandomica = ""
        var urlPesquisa = ""

        when (Random.nextInt(3)) {
            0 -> ProcuraRandomica = randomCharacter + "%25"
            1 -> ProcuraRandomica = "%25" + randomCharacter + "%25"
            2 -> ProcuraRandomica = "%25" + randomCharacter
        }
        RetrofitSpotify.servicosSpotify.get_rand(urlPesquisa).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
        val getRandomOffset = Random.nextInt(999) // Gera um número aleatório entre 0 e 998

    }
    private fun obterTokenSpotify() {
        val authorizationHeader = getAuthorizationHeader()

        RetrofitSpotify.servicosSpotify.getAccessToken(authorizationHeader).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    tokenResponse?.let {
                        Log.d("MainActivity", "Token de acesso: ${it.access_token}")
                        var tokenSpotify = tokenResponse.access_token.toString()

                    }
                } else {
                    Log.e("MainActivity", "Erro ao obter token: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.e("MainActivity", "Falha na requisição Spotify: ", t)
            }
        })
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
