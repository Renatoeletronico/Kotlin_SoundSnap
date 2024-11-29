package com.fatecmaua.soundsnap.models
import java.io.Serializable

data class Usuario(
    val usuario: String,
    val nome: String,
    val email: String,
    val senha: String,
    val imagem: String = "",
    var likes: List<String> = listOf(), // Lista de IDs de álbuns que o usuário curtiu
    val deslikes: List<String> = listOf() // Lista de IDs de álbuns que o usuário não curtiu
) : Serializable {
    override fun toString(): String {
        return "Usuario(usuario='$usuario', nome='$nome', email='$email', imagem='$imagem', likes=$likes, deslikes=$deslikes)"
    }
}
