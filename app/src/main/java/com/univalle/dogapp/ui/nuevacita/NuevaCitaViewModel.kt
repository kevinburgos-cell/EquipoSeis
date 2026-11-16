package com.univalle.dogapp.ui.nuevacita

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
 * ViewModel de la HU 3.0 (Nueva Cita).
 *
 * En MVVM el Fragment es la Vista: solo pinta y escucha. Este ViewModel es el
 * que tiene la lógica: pide las razas a la API, pide la imagen y guarda la cita
 * en Room. El Fragment se comunica con él mediante LiveData.
 */
class NuevaCitaViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio: une la base de datos local (Room) con el servicio web (Retrofit).
    // Se necesitan los datos de la Application para abrir la base de datos.
    private val repository = CitaRepository(
        AppDatabase.obtener(application).citaDao(),
        RetrofitClient.dogApi
    )

    // LiveData de solo lectura: lista de razas que llega desde dog.ceo.
    private val _razas = MutableLiveData<List<String>>(emptyList())
    val razas: LiveData<List<String>> = _razas

    // LiveData que avisa al Fragment si la cita se guardó (true) o falló (false).
    private val _guardado = MutableLiveData<Boolean?>(null)
    val guardado: LiveData<Boolean?> = _guardado

    /** Criterio 3: consume https://dog.ceo/api/breeds/list/all con Retrofit. */
    fun cargarRazas() {
        viewModelScope.launch {
            _razas.value = try {
                // La API devuelve un mapa raza -> subrazas; solo se usan los nombres.
                repository.listarRazas().keys.sorted()
            } catch (e: Exception) {
                // Si no hay internet, el AutoCompleteTextView queda vacío pero la pantalla sigue funcionando.
                emptyList()
            }
        }
    }

    /** Criterio 10: obtiene la imagen de la raza y guarda la cita en Room. */
    fun guardarCita(
        nombre: String,
        raza: String,
        propietario: String,
        telefono: String,
        sintoma: String
    ) {
        viewModelScope.launch {
            _guardado.value = try {
                val imagen = try {
                    // Imagen alusiva a la raza elegida.
                    repository.obtenerImagen(raza)
                } catch (e: Exception) {
                    // Si la raza no existe en la API, se usa una imagen aleatoria.
                    repository.imagenAleatoria()
                }

                repository.insertar(
                    Cita(
                        nombre = nombre,
                        raza = raza,
                        propietario = propietario,
                        telefono = telefono,
                        sintoma = sintoma,
                        imagenUrl = imagen
                    )
                )
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /** Limpia el aviso para que no se repita al girar la pantalla. */
    fun reiniciarGuardado() {
        _guardado.value = null
    }
}
