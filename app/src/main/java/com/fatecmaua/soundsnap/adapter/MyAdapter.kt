package com.fatecmaua.soundsnap.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fatecmaua.soundsnap.R
import com.fatecmaua.soundsnap.models.AlbumItem
import java.text.SimpleDateFormat
import java.util.*

class MyAdapter(
    private val items: MutableList<AlbumItem>,
    private var likedAlbumIds: MutableSet<String>,
    private val onLikeClick: (String) -> Unit
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumImage: ImageView = itemView.findViewById(R.id.imageView)
        val artistaTxt: TextView = itemView.findViewById(R.id.ArtistaTxt)
        val albumNameTxt: TextView = itemView.findViewById(R.id.AlbumTxt)
        val albumTypeTxt: TextView = itemView.findViewById(R.id.AlbumTypeTxt)
        val totalTracksTxt: TextView = itemView.findViewById(R.id.TotalTracksTxt)
        val dataLancamentoTxt: TextView = itemView.findViewById(R.id.DataLancamentoTxt)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val albumItem = items[position]

        // Carregar a imagem do álbum com Glide
        if (albumItem.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(albumItem.images[0].url)
                .into(holder.albumImage)
        }

        // Exibir informações do álbum
        holder.artistaTxt.text = albumItem.artists.joinToString(", ") { it.name }
        holder.albumNameTxt.text = albumItem.name
        holder.albumTypeTxt.text = albumItem.album_type
        holder.totalTracksTxt.text = "Total de faixas: ${albumItem.total_tracks}"
        holder.dataLancamentoTxt.text = "Lançamento: ${formatDate(albumItem.release_date)}"

        // Verificar se o álbum está curtido
        val isLiked = likedAlbumIds.contains(albumItem.id)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.like_escuro else R.drawable.like_claro
        )

        // Configurar o botão de like
        holder.likeButton.setOnClickListener {
            val albumId = albumItem.id
            if (likedAlbumIds.contains(albumId)) {
                likedAlbumIds.remove(albumId) // Remove da lista de curtidos
            } else {
                likedAlbumIds.add(albumId) // Adiciona à lista de curtidos
            }

            onLikeClick(albumId) // Notifica a MainActivity sobre a mudança
            notifyItemChanged(position) // Atualiza o item no RecyclerView
        }
    }

    override fun getItemCount(): Int = items.size

    // Atualiza a lista de likes
    fun updateLikes(updatedLikes: Set<String>) {
        // Evita sobrescrever diretamente likedAlbumIds
        likedAlbumIds.clear() // Limpa a lista atual
        likedAlbumIds.addAll(updatedLikes) // Adiciona os novos likes
        notifyDataSetChanged() // Notifica o RecyclerView para atualizar
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString // Retorna a string original se houver erro
        }
    }
}
