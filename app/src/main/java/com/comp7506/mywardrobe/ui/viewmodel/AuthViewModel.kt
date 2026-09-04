package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.auth.FirebaseAuthManager
import com.comp7506.mywardrobe.auth.FirebaseAuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel(private val authManager: FirebaseAuthManager) : ViewModel() {
    private val state = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = state

    val isLoggedIn: StateFlow<Boolean> =
        authManager.currentUserFlow
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.Eagerly, authManager.currentUser() != null)

    fun setEmail(email: String) {
        state.value = state.value.copy(email = email, errorMessage = null)
    }

    fun setPassword(password: String) {
        state.value = state.value.copy(password = password, errorMessage = null)
    }

    fun login() {
        val current = state.value
        viewModelScope.launch {
            try {
                state.value = current.copy(isLoading = true, errorMessage = null)
                val result = authManager.login(
                    email = current.email,
                    password = current.password,
                )
                state.value = when (result) {
                    is FirebaseAuthResult.Success -> AuthUiState()
                    is FirebaseAuthResult.Error -> current.copy(isLoading = false, errorMessage = result.message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                state.value = current.copy(isLoading = false, errorMessage = "Sign in failed. Please try again later")
            }
        }
    }

    fun register() {
        val current = state.value
        viewModelScope.launch {
            try {
                state.value = current.copy(isLoading = true, errorMessage = null)
                val result = authManager.register(
                    email = current.email,
                    password = current.password,
                )
                state.value = when (result) {
                    is FirebaseAuthResult.Success -> AuthUiState()
                    is FirebaseAuthResult.Error -> current.copy(isLoading = false, errorMessage = result.message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                state.value = current.copy(isLoading = false, errorMessage = "Sign up failed. Please try again later")
            }
        }
    }

    fun logout() {
        authManager.logout()
    }
}

