package com.univalle.dogapp.ui.detalle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.univalle.dogapp.data.local.AppDatabase
import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.data.remote.RetrofitClient
import com.univalle.dogapp.data.repository.CitaRepository
import kotlinx.coroutines.launch

/**
 * HU 4.0 - Detalle de la cita.
 *
 * Criterio 2 y 3: expone la cita guardada en Room para pintar la tarjeta.
 * Criterio 4: elimina la cita de la base de datos local.
 */
class DetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CitaRepository(
        AppDatabase.obtener(application).citaDao(),
        RetrofitClient.dogApi
    )

    private val _cita = MutableLiveData<Cita?>()
    val cita: LiveData<Cita?> = _cita

    // Eventos de un solo uso para no repetir navegación o mensajes al rotar.
    private val _citaNoEncontrada = MutableLiveData(false)
    val citaNoEncontrada: LiveData<Boolean> = _citaNoEncontrada

    private val _citaEliminada = MutableLiveData<Boolean?>(null)
    val citaEliminada: LiveData<Boolean?> = _citaEliminada

    fun cargarCita(citaId: Long) {
        if (citaId <= 0L) {
            _citaNoEncontrada.value = true
            return
        }
        viewModelScope.launch {
            val cita = try {
                repository.obtenerPorId(citaId)
            } catch (e: Exception) {
                null
            }

            if (cita == null) {
                _citaNoEncontrada.value = true
            } else {
                _cita.value = cita
            }
        }
    }

    /** Criterio 4: elimina sin diálogo de confirmación (la vista navega al Home). */
    fun eliminarCita() {
        val cita = _cita.value ?: return
        viewModelScope.launch {
            _citaEliminada.value = try {
                repository.eliminar(cita)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun consumirEliminada() {
        _citaEliminada.value = null
    }
}
