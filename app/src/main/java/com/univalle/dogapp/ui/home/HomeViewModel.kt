package com.univalle.dogapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.univalle.dogapp.data.local.AppDatabase
import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.data.remote.RetrofitClient
import com.univalle.dogapp.data.repository.CitaRepository

/**
 * HU 2.0 - Criterio 6: expone la lista de citas que vive en Room.
 * El Flow del DAO se convierte en LiveData y se actualiza solo cuando
 * se inserta, edita o elimina una cita.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CitaRepository(
        AppDatabase.obtener(application).citaDao(),
        RetrofitClient.dogApi
    )

    val citas: LiveData<List<Cita>> = repository.listarTodas().asLiveData()
}
