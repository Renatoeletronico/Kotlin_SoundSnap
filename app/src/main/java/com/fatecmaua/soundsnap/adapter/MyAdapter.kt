package com.fatecmaua.soundsnap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fatecmaua.soundsnap.R
import com.fatecmaua.soundsnap.models.AlbumItem
import java.text.SimpleDateFormat
import java.util.*

class MyAdapter(private val items: MutableList<AlbumItem>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // ViewHolder que contém as views do item da lista
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumImage: ImageView = itemView.findViewById(R.id.imageView) // Imagem do álbum
        val artistaTxt: TextView = itemView.findViewById(R.id.ArtistaTxt) // Nome do artista(s)
        val albumNameTxt: TextView = itemView.findViewById(R.id.AlbumTxt) // Nome do álbum
        val albumTypeTxt: TextView = itemView.findViewById(R.id.AlbumTypeTxt) // Tipo do álbum
        val totalTracksTxt: TextView = itemView.findViewById(R.id.TotalTracksTxt) // Total de faixas
        val dataLancamentoTxt: TextView = itemView.findViewById(R.id.DataLancamentoTxt) // Data de lançamento
    }

    // Infla o layout e retorna o ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    // Vincula os dados ao item do RecyclerView
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val albumItem = items[position]

        // Configura a imagem do álbum
        if (albumItem.images.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(albumItem.images[1].url) // Usando a primeira imagem
                .into(holder.albumImage)
        }

        // Configura o nome do artista (pode ser mais de um artista, concatenados)
        holder.artistaTxt.text = albumItem.artists.joinToString(", ") { it.name }

        // Configura o nome do álbum
        holder.albumNameTxt.text = albumItem.name

        // Configura o tipo do álbum (ex: "single", "album")
        holder.albumTypeTxt.text = albumItem.album_type

        // Configura o número de faixas do álbum
        holder.totalTracksTxt.text = "Total de faixas: ${albumItem.total_tracks}"

        // Configura a data de lançamento
        holder.dataLancamentoTxt.text = "Lançamento: ${formatDate(albumItem.release_date)}"
    }

    // Retorna o tamanho da lista
    override fun getItemCount(): Int = items.size
}


fun formatDate(dateString: String): String {
    try {
        // Definindo o formato da data que vem da API
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // Formato da data recebida (exemplo: 2024-11-23)
        val date = inputFormat.parse(dateString)

        // Definindo o formato da data que queremos exibir
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Exemplo: 23/11/2024
        return outputFormat.format(date ?: Date())  // Retorna a data formatada
    } catch (e: Exception) {
        e.printStackTrace()
        return dateString  // Caso haja erro, retorna o valor original
    }
}
