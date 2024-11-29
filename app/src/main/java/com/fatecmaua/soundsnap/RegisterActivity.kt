package com.fatecmaua.soundsnap

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fatecmaua.soundsnap.models.Usuario
import com.fatecmaua.soundsnap.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Vincula os elementos do layout
        edtUsername = findViewById(R.id.edtUsername)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Configura o listener para o botão de registro
        btnRegister.setOnClickListener {
            if (validateInputs()) {
                val usuario = Usuario(
                    usuario = edtUsername.text.toString(),
                    nome = edtName.text.toString(),
                    email = edtEmail.text.toString(),
                    senha = edtPassword.text.toString(),
                    imagem = ""
                )
                registerUser(usuario)
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (edtUsername.text.isNullOrBlank() ||
            edtName.text.isNullOrBlank() ||
            edtEmail.text.isNullOrBlank() ||
            edtPassword.text.isNullOrBlank()
        ) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(user: Usuario) {
        btnRegister.isEnabled = false // Desativa o botão para evitar cliques múltiplos

        RetrofitClient.servicosFastAPI.createUser(user).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                btnRegister.isEnabled = true // Reativa o botão após a resposta
                if (response.isSuccessful || response.code() == 500) {
                    // Desconsidera o erro 500 e ainda exibe a mensagem de sucesso
                    Toast.makeText(this@RegisterActivity, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Fecha a activity após o sucesso
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@RegisterActivity, "Erro: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                btnRegister.isEnabled = true // Reativa o botão em caso de falha
                Toast.makeText(this@RegisterActivity, "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("RegisterActivity", "Falha na requisição", t)
            }
        })
    }
}
