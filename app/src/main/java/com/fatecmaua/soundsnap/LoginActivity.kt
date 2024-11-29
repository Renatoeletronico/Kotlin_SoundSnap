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
    private var buttonRegister: Button? = null  // Botão de cadastro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializando os componentes
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegister = findViewById(R.id.buttonRegister)  // Referência ao botão de cadastro

        // Verificar se o usuário já está logado
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)

        Log.d("LoginActivity", "Nome de usuário recuperado: $savedUsername")

        if (savedUsername != null) {
            // Usuário já está logado, redireciona diretamente para a MainActivity
            navigateToMainActivity()
        }

        // Lógica para fazer login
        buttonLogin?.setOnClickListener(View.OnClickListener {
            val username = editTextUsername?.text.toString().trim()
            val password = editTextPassword?.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val loginRequest = LoginRequest(username, password)

                RetrofitClient.servicosFastAPI.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            if (loginResponse.success) {
                                val editor = sharedPreferences.edit()
                                editor.putString("username", username)
                                editor.commit()

                                Log.d("LoginActivity", "Nome de usuário salvo: $username")

                                navigateToMainActivity()
                            } else {
                                Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Erro na comunicação com o servidor", Toast.LENGTH_SHORT).show()
                            Log.d("LoginActivity", "Erro na comunicação com o servidor: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Erro na conexão com o servidor", Toast.LENGTH_SHORT).show()
                        t.printStackTrace()
                        Log.d("LoginActivity", "Erro na conexão: ${t.message}")
                    }
                })
            } else {
                Toast.makeText(this@LoginActivity, "Por favor, insira usuário e senha", Toast.LENGTH_SHORT).show()
            }
        })

        // Lógica para redirecionar para a tela de cadastro
        buttonRegister?.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()  // Fecha a LoginActivity para que o usuário não possa voltar a ela
    }
}

