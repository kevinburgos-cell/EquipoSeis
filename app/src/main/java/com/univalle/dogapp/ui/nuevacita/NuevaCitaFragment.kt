package com.univalle.dogapp.ui.nuevacita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.univalle.dogapp.R
import com.univalle.dogapp.databinding.FragmentNuevaCitaBinding

/**
 * HU 3.0 - Ventana Nueva Cita (DGPP6-5).
 *
 * Este Fragment es la pantalla de "Nueva Cita". No se abre sola: la Activity
 * (MainActivity) tiene un NavHostFragment que carga los Fragments según el
 * archivo res/navigation/nav_graph.xml. En ese gráfico este destino se llama
 * "nuevaCitaFragment" y la acción para volver al Home es
 * "action_nueva_cita_to_home".
 *
 * La conexión completa es:
 * MainActivity -> activity_main.xml -> NavHostFragment -> nav_graph.xml -> este Fragment.
 */
class NuevaCitaFragment : Fragment() {

    // ViewBinding: genera una clase con las referencias a los ids del XML
    // fragment_nueva_cita.xml (etNombreMascota, actRaza, btnGuardarCita, etc.).
    private var _binding: FragmentNuevaCitaBinding? = null
    private val binding get() = _binding!!

    // ViewModel de la pantalla. Guarda los datos y las operaciones (Room y API)
    // aunque el Fragment se vuelva a crear al girar la pantalla.
    private val viewModel: NuevaCitaViewModel by viewModels()

    // Campos de texto que se validan para habilitar el botón Guardar Cita.
    private lateinit var campos: List<EditText>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el XML de la pantalla y devuelve la vista raíz al Fragment.
        _binding = FragmentNuevaCitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarToolbar()
        configurarSintomas()
        configurarRazas()
        configurarValidacion()
        observarViewModel()
    }

    /** Criterio 1: la flecha rosada vuelve al Home (HU 2.0). */
    private fun configurarToolbar() {
        binding.btnAtras.setOnClickListener {
            // navigateUp() devuelve al destino anterior del nav_graph,
            // que en este caso es el Home desde donde se abrió esta pantalla.
            findNavController().navigateUp()
        }
    }

    /** Criterio 6: lista de síntomas quemada en res/values/strings.xml. */
    private fun configurarSintomas() {
        // ArrayAdapter convierte el arreglo de strings en los ítems del Spinner.
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sintomas,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSintomas.adapter = adapter
        // La posición 0 es "Síntomas", por eso se usa como valor por defecto.
    }

    /** Criterio 3: razas traídas de dog.ceo y sugeridas en el AutoCompleteTextView. */
    private fun configurarRazas() {
        // El ViewModel expone la lista de razas con LiveData. Cuando la API responde,
        // el observer actualiza las sugerencias del AutoCompleteTextView.
        viewModel.razas.observe(viewLifecycleOwner) { razas ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                razas
            )
            binding.actRaza.setAdapter(adapter)
        }
        viewModel.cargarRazas()
    }

    /** Criterios 7 y 9: el botón solo se activa con Nombre, Raza, Propietario y Teléfono. */
    private fun configurarValidacion() {
        campos = listOf(
            binding.etNombreMascota,
            binding.actRaza,
            binding.etPropietario,
            binding.etTelefono
        )
        // Cada vez que el usuario escribe, se revisa si ya están los cuatro campos.
        campos.forEach { campo ->
            campo.doAfterTextChanged { actualizarBoton() }
        }
        binding.btnGuardarCita.setOnClickListener { guardarCita() }
    }

    private fun actualizarBoton() {
        binding.btnGuardarCita.isEnabled = campos.all { it.text.isNotBlank() }
    }

    /** Criterio 8: si faltan los cuatro campos no se puede llegar aquí (botón inactivo). */
    private fun guardarCita() {
        if (binding.spSintomas.selectedItemPosition == 0) {
            // No se ha elegido un síntoma: se avisa y no se guarda.
            Toast.makeText(requireContext(), R.string.selecciona_sintoma, Toast.LENGTH_SHORT).show()
            return
        }

        // Se desactiva el botón para evitar guardar dos veces por doble toque.
        binding.btnGuardarCita.isEnabled = false
        viewModel.guardarCita(
            nombre = binding.etNombreMascota.text.toString().trim(),
            raza = binding.actRaza.text.toString().trim(),
            propietario = binding.etPropietario.text.toString().trim(),
            telefono = binding.etTelefono.text.toString().trim(),
            sintoma = binding.spSintomas.selectedItem.toString()
        )
    }

    /** Criterio 10: cuando Room termina de guardar, se navega al Home. */
    private fun observarViewModel() {
        viewModel.guardado.observe(viewLifecycleOwner) { guardado ->
            when (guardado) {
                true -> {
                    Toast.makeText(requireContext(), R.string.cita_guardada, Toast.LENGTH_SHORT).show()
                    viewModel.reiniciarGuardado()
                    // Acción declarada en nav_graph.xml: Nueva Cita -> Home.
                    findNavController().navigate(R.id.action_nueva_cita_to_home)
                }

                false -> {
                    actualizarBoton()
                    Toast.makeText(requireContext(), R.string.error_guardando, Toast.LENGTH_SHORT).show()
                    viewModel.reiniciarGuardado()
                }

                null -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Se libera el binding cuando la vista se destruye para evitar fugas de memoria.
        _binding = null
    }
}
