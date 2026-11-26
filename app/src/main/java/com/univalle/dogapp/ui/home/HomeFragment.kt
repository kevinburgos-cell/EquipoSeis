package com.univalle.dogapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.univalle.dogapp.R
import com.univalle.dogapp.data.local.Cita
import com.univalle.dogapp.databinding.FragmentHomeBinding

/**
 * HU 2.0 - Home "Administrador de Citas".
 *
 * Criterio 1: el botón físico "atrás" en esta pantalla manda la app al
 * escritorio del teléfono (no vuelve al login, que ya fue sacado del back stack).
 * Criterio 2: header oscuro curvado con título e identidad DogApp.
 * Criterio 4: FAB rosado -> HU 3.0 (Nueva Cita).
 * Criterio 5: clic en una tarjeta -> HU 4.0 (Detalle) enviando el id.
 * Criterio 6: lista observada desde Room.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarLista()
        configurarNavegacion()
        aplicarInsets()
    }

    /** Criterio 3 y 6: tarjetas construidas a partir del Flow de Room. */
    private fun configurarLista() {
        val adapter = CitaAdapter(::abrirDetalle)
        binding.rvCitas.adapter = adapter

        viewModel.citas.observe(viewLifecycleOwner) { citas ->
            adapter.submitList(citas)
            binding.tvSinCitas.isVisible = citas.isEmpty()
            binding.rvCitas.isVisible = citas.isNotEmpty()
        }
    }

    /** Criterios 1, 4 y 5. */
    private fun configurarNavegacion() {
        binding.fabNuevaCita.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_nueva_cita)
        }

        // Criterio 1: en el Home, "atrás" lleva al escritorio y no al login.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().moveTaskToBack(true)
        }
    }

    private fun abrirDetalle(cita: Cita) {
        findNavController().navigate(
            R.id.action_home_to_detalle,
            bundleOf("citaId" to cita.id)
        )
    }

    /** Respeta la barra de estado y de navegación (edge-to-edge de Android 15+). */
    private fun aplicarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootHome) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.toolbarHome.updatePadding(top = bars.top)
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
