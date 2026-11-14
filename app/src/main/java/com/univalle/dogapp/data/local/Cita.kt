package com.univalle.dogapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas")
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val raza: String,
    val propietario: String,
    val telefono: String,
    val sintoma: String,
    val imagenUrl: String
)
