package com.fatecmaua.soundsnap.models
import java.io.Serializable

class Usuario (
        val usuario: String,
        val nome: String,
        val email: String,
        val senha: String,
        val imagem: String
    ):Serializable

{
    override fun toString(): String {
        return "Usuario(usuario='$usuario', nome='$nome', email='$email', imagem='$imagem')"
    }
}
































