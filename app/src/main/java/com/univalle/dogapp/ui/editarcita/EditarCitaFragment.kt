package com.univalle.dogapp.ui.editarcita

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.univalle.dogapp.R
import com.univalle.dogapp.data.local.AppDatabase
import com.univalle.dogapp.data.remote.RetrofitClient
import com.univalle.dogapp.data.repository.CitaRepository

class EditarCitaFragment : Fragment(R.layout.fragment_editar_cita) {

    private val viewModel: EditarCitaViewModel by viewModels {
        val dao = AppDatabase.obtener(requireContext()).citaDao()
        val repo = CitaRepository(dao, RetrofitClient.dogApi)
        EditarCitaViewModelFactory(repo)
    }

    private lateinit var etNombre: TextInputEditText
    private lateinit var actvRaza: AutoCompleteTextView
    private lateinit var etPropietario: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var btnEditar: AppCompatButton
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias de UI
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackEditar)
        etNombre = view.findViewById(R.id.etNombreMascota)
        actvRaza = view.findViewById(R.id.actvRaza)
        etPropietario = view.findViewById(R.id.etPropietario)
        etTelefono = view.findViewById(R.id.etTelefono)
        btnEditar = view.findViewById(R.id.btnEditarCita)
        progressBar = view.findViewById(R.id.progressEditar)

        // Recibir el ID de la cita enviado desde DetalleFragment
        val citaId = arguments?.getLong("citaId") ?: -1L

        // Criterio 1: Flecha atrás navega a HU 4.0 Detalle de la Cita
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Validación en tiempo real de los 4 campos (Criterios 6 y 7)
        etNombre.doAfterTextChanged { validarCampos() }
        actvRaza.doAfterTextChanged { validarCampos() }
        etPropietario.doAfterTextChanged { validarCampos() }
        etTelefono.doAfterTextChanged { validarCampos() }

        // Observar datos de la cita para precargar el formulario (Criterio 2)
        viewModel.citaActual.observe(viewLifecycleOwner) { cita ->
            if (cita != null) {
                etNombre.setText(cita.nombre)
                actvRaza.setText(cita.raza, false)
                etPropietario.setText(cita.propietario)
                etTelefono.setText(cita.telefono)
                validarCampos()
            }
        }

        // Observar lista de razas de Retrofit para el AutoCompleteTextView (Criterio 3)
        viewModel.listaRazas.observe(viewLifecycleOwner) { razas ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                razas
            )
            actvRaza.setAdapter(adapter)
        }

        // Observar estado de carga
        viewModel.cargando.observe(viewLifecycleOwner) { estaCargando ->
            progressBar.visibility = if (estaCargando) View.VISIBLE else View.GONE
            if (estaCargando) {
                btnEditar.isEnabled = false
            } else {
                validarCampos()
            }
        }

        // Observar finalización de actualización y navegar a HU 2.0 Home (Criterio 8)
        viewModel.citaActualizada.observe(viewLifecycleOwner) { actualizada ->
            if (actualizada) {
                Toast.makeText(requireContext(), "Cita actualizada correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editar_cita_to_home)
            }
        }

        // Acción del botón Editar Cita
        btnEditar.setOnClickListener {
            viewModel.guardarCambios(
                nombre = etNombre.text.toString(),
                raza = actvRaza.text.toString(),
                propietario = etPropietario.text.toString(),
                telefono = etTelefono.text.toString()
            )
        }

        // Iniciar carga si el ID es válido
        if (citaId != -1L) {
            viewModel.cargarDatosIniciales(citaId)
        }
    }

    private fun validarCampos() {
        val camposLlenos = !etNombre.text.isNullOrBlank() &&
                !actvRaza.text.isNullOrBlank() &&
                !etPropietario.text.isNullOrBlank() &&
                !etTelefono.text.isNullOrBlank()

        btnEditar.isEnabled = camposLlenos
        // Criterio 7: Texto blanco bold cuando se habilita
        btnEditar.setTextColor(
            if (camposLlenos) Color.WHITE else Color.parseColor("#4A2528")
        )
    }
}