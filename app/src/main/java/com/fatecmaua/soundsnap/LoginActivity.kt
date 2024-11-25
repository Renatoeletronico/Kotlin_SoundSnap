package com.fatecmaua.soundsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecmaua.soundsnap.models.LoginResponse
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import com.fatecmaua.soundsnap.servicos.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private var editTextUsername: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonLogin: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializando os componentes
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Verificar se o usuário já está logado
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)

        if (savedUsername != null) {
            // Usuário já está logado, redireciona diretamente para a MainActivity
            navigateToMainActivity()
        }

        // Lógica para fazer login
        buttonLogin?.setOnClickListener(View.OnClickListener {
            val username = editTextUsername?.text.toString()
            val password = editTextPassword?.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val loginRequest = LoginRequest(username, password)

                // Chamar a API para fazer login
                RetrofitClient.servicosFastAPI.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            if (loginResponse.success) {
                                // Salvar o nome de usuário no SharedPreferences
                                val editor = sharedPreferences.edit()
                                editor.putString("username", username)
                                editor.apply()

                                // Redirecionar para a MainActivity
                                navigateToMainActivity()
                            } else {
                                // Exibir mensagem de erro
                                Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Erro na comunicação com o servidor", Toast.LENGTH_SHORT).show()
                            Log.d("LoginActivity", "Erro na conexão com o servidor: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Erro na conexão com o servidor", Toast.LENGTH_SHORT).show()
                        t.printStackTrace() // Para exibir o stack trace no log do console
                        Log.d("LoginActivity", "Erro na conexão: ${t.message}")
                    }

                })
            } else {
                Toast.makeText(this@LoginActivity, "Por favor, insira usuário e senha", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()  // Fecha a LoginActivity para que o usuário não possa voltar a ela
    }
}


