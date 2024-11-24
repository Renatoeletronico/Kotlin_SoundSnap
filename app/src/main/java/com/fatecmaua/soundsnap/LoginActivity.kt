package com.fatecmaua.soundsnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

            // Validação simples
            if (username == "Teste" && password == "senha") {
                // Salvar o nome de usuário no SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.apply()

                // Redirecionar para a MainActivity
                navigateToMainActivity()
            } else {
                // Exibir mensagem de erro
                Toast.makeText(this@LoginActivity, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()  // Fecha a LoginActivity para que o usuário não possa voltar a ela
    }
}
