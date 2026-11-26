package com.univalle.dogapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.univalle.dogapp.R
import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.databinding.ItemCitaBinding

/**
 * HU 2.0 - Criterio 3: cada cita se pinta como una tarjeta con imagen circular,
 * nombre, síntoma y turno (id autogenerado por Room).
 */
class CitaAdapter(
    private val onCitaClick: (Cita) -> Unit
) : ListAdapter<Cita, CitaAdapter.CitaViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(getItem(position), onCitaClick)
    }

    class CitaViewHolder(
        private val binding: ItemCitaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cita: Cita, onCitaClick: (Cita) -> Unit) {
            val context = binding.root.context
            binding.tvNombre.text = cita.nombre
            binding.tvSintoma.text = cita.sintoma
            binding.tvTurno.text = context.getString(R.string.home_turno, cita.id)
            binding.ivMascota.contentDescription =
                context.getString(R.string.home_foto_mascota, cita.nombre)

            Glide.with(binding.ivMascota)
                .load(cita.imagenUrl)
                .placeholder(R.drawable.dog_head)
                .error(R.drawable.dog_head)
                .centerCrop()
                .into(binding.ivMascota)

            binding.root.setOnClickListener { onCitaClick(cita) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Cita>() {
            override fun areItemsTheSame(oldItem: Cita, newItem: Cita): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Cita, newItem: Cita): Boolean =
                oldItem == newItem
        }
    }
}
