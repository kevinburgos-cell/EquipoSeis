package com.univalle.dogapp.data.repository

import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.data.local.CitaDao
import com.univalle.dogapp.data.remote.DogApiService

class CitaRepository(
    private val citaDao: CitaDao,
    private val dogApi: DogApiService
) {
    fun listarTodas() = citaDao.listarTodas()

    suspend fun obtenerPorId(id: Long) = citaDao.obtenerPorId(id)

    suspend fun insertar(cita: Cita) = citaDao.insertar(cita)

    suspend fun actualizar(cita: Cita) = citaDao.actualizar(cita)

    suspend fun eliminar(cita: Cita) = citaDao.eliminar(cita)

    suspend fun listarRazas() = dogApi.listarRazas().message

    suspend fun obtenerImagen(raza: String) = dogApi.obtenerImagen(raza).message

    suspend fun imagenAleatoria() = dogApi.imagenAleatoria().message
}
