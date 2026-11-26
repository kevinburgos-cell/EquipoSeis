package com.univalle.dogapp.ui.detalle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.univalle.dogapp.R
import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.databinding.FragmentDetalleBinding
import java.util.Locale

/**
 * HU 4.0 - Ventana Detalle de la Cita.
 *
 * Criterio 1: toolbar con el nombre de la mascota y flecha rosada al Home.
 * Criterio 2 y 3: tarjeta de imagen y tarjeta de información de la cita.
 * Criterio 4: FAB rosado elimina de Room y vuelve a la HU 2.0.
 * Criterio 5: FAB azul oscuro navega a la HU 5.0 enviando el id de la cita.
 */
class DetalleFragment : Fragment() {

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalleViewModel by viewModels()

    private var citaActual: Cita? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarToolbar()
        configurarObservadores()
        configurarBotones()
        aplicarInsets()

        val citaId = arguments?.getLong("citaId") ?: -1L
        viewModel.cargarCita(citaId)
    }

    /** Criterio 1: la flecha vuelve a la HU 2.0 (Home). */
    private fun configurarToolbar() {
        binding.btnAtrasDetalle.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun configurarObservadores() {
        viewModel.cita.observe(viewLifecycleOwner) { cita ->
            if (cita != null) {
                mostrarCita(cita)
            }
        }

        viewModel.citaNoEncontrada.observe(viewLifecycleOwner) { noEncontrada ->
            if (noEncontrada) {
                Toast.makeText(requireContext(), R.string.detalle_no_encontrada, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.citaEliminada.observe(viewLifecycleOwner) { eliminada ->
            when (eliminada) {
                true -> {
                    viewModel.consumirEliminada()
                    findNavController().navigate(R.id.action_detalle_to_home)
                }

                false -> {
                    viewModel.consumirEliminada()
                    Toast.makeText(requireContext(), R.string.detalle_error_eliminar, Toast.LENGTH_SHORT).show()
                }

                null -> Unit
            }
        }
    }

    /** Criterios 2, 3 y 5. */
    private fun mostrarCita(cita: Cita) {
        citaActual = cita

        binding.tvTituloDetalle.text = cita.nombre
        binding.tvTurnoDetalle.text = getString(R.string.home_turno, cita.id)
        binding.tvRazaDetalle.text = formatearRaza(cita.raza)
        binding.tvSintomaDetalle.text = cita.sintoma
        binding.tvPropietarioDetalle.text = getString(R.string.detalle_propietario, cita.propietario)
        binding.tvTelefonoDetalle.text = getString(R.string.detalle_telefono, cita.telefono)
        binding.ivMascotaDetalle.contentDescription =
            getString(R.string.home_foto_mascota, cita.nombre)

        Glide.with(binding.ivMascotaDetalle)
            .load(cita.imagenUrl)
            .placeholder(R.drawable.dog_head)
            .error(R.drawable.dog_head)
            .centerCrop()
            .into(binding.ivMascotaDetalle)
    }

    private fun configurarBotones() {
        // Criterio 4: eliminar de Room sin confirmación y volver al Home.
        binding.fabEliminar.setOnClickListener {
            viewModel.eliminarCita()
        }

        // Criterio 5: abrir la HU 5.0 con el id de la cita actual.
        binding.fabEditar.setOnClickListener {
            val cita = citaActual ?: return@setOnClickListener
            findNavController().navigate(
                R.id.action_detalle_to_editar_cita,
                bundleOf("citaId" to cita.id)
            )
        }
    }

    /** La raza se guarda en minúsculas; en el detalle se muestra capitalizada. */
    private fun formatearRaza(raza: String): String =
        raza.replaceFirstChar { letra ->
            if (letra.isLowerCase()) letra.titlecase(Locale.getDefault()) else letra.toString()
        }

    /** Respeta la barra de estado y de navegación (edge-to-edge de Android 15+). */
    private fun aplicarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootDetalle) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.toolbarDetalle.updatePadding(top = bars.top)
            root.updatePadding(
                left = bars.left,
                right = bars.right,
                bottom = bars.bottom
            )
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
