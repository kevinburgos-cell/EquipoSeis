package com.univalle.dogapp.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.univalle.dogapp.R
import com.univalle.dogapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricPrompt = BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val navController = findNavController()
                if (navController.currentDestination?.id == R.id.loginFragment) {
                    navController.navigate(R.id.action_login_to_home)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                showToast(errString)
            }

            override fun onAuthenticationFailed() {
                showToast(getString(R.string.login_biometric_failed))
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLoginBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val safeArea = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            root.updatePadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom)
            insets
        }

        binding.fingerprintAnimation.setOnClickListener { authenticate() }
    }

    private fun authenticate() {
        val availability = BiometricManager.from(requireContext())
            .canAuthenticate(BIOMETRIC_STRONG)

        val errorMessage = when (availability) {
            BiometricManager.BIOMETRIC_SUCCESS -> null
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> R.string.login_biometric_no_hardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> R.string.login_biometric_not_enrolled
            else -> R.string.login_biometric_unavailable
        }

        if (errorMessage != null) {
            showToast(getString(errorMessage))
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_biometric_title))
            .setSubtitle(getString(R.string.login_biometric_subtitle))
            .setNegativeButtonText(getString(R.string.login_biometric_cancel))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showToast(message: CharSequence) {
        context?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }
}
