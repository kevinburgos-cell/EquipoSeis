package com.univalle.dogapp.ui.editarcita

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.data.repository.CitaRepository
import kotlinx.coroutines.launch

class EditarCitaViewModel(private val repository: CitaRepository) : ViewModel() {

    private val _citaActual = MutableLiveData<Cita?>()
    val citaActual: LiveData<Cita?> = _citaActual

    private val _listaRazas = MutableLiveData<List<String>>()
    val listaRazas: LiveData<List<String>> = _listaRazas

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _citaActualizada = MutableLiveData<Boolean>()
    val citaActualizada: LiveData<Boolean> = _citaActualizada

    fun cargarDatosIniciales(citaId: Long) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // 1. Cargar la cita desde Room
                val cita = repository.obtenerPorId(citaId)
                _citaActual.value = cita

                // 2. Cargar las razas desde Dog CEO API
                val mapaRazas = repository.listarRazas()
                _listaRazas.value = mapaRazas.keys.toList().sorted()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _cargando.value = false
            }
        }
    }

    fun guardarCambios(
        nombre: String,
        raza: String,
        propietario: String,
        telefono: String
    ) {
        val original = _citaActual.value ?: return
        val razaLimpia = raza.trim().lowercase()

        viewModelScope.launch {
            _cargando.value = true
            try {
                // Obtener imagen aleatoria de la raza seleccionada (Criterio 8)
                val nuevaImagenUrl = try {
                    repository.obtenerImagen(razaLimpia)
                } catch (e: Exception) {
                    original.imagenUrl
                }

                val citaModificada = original.copy(
                    nombre = nombre.trim(),
                    raza = razaLimpia,
                    propietario = propietario.trim(),
                    telefono = telefono.trim(),
                    imagenUrl = nuevaImagenUrl
                )

                repository.actualizar(citaModificada)
                _citaActualizada.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _cargando.value = false
            }
        }
    }
}

class EditarCitaViewModelFactory(
    private val repository: CitaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditarCitaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditarCitaViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}