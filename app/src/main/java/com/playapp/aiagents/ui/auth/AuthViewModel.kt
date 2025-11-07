package com.playapp.aiagents.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playapp.aiagents.data.service.FirebaseAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authService: FirebaseAuthService) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signInWithEmailAndPassword(email, password)
                result.fold(
                    onSuccess = { authResult ->
                        _authState.value = AuthState.Success(authResult.user?.uid ?: "")
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.createUserWithEmailAndPassword(email, password)
                result.fold(
                    onSuccess = { authResult ->
                        _authState.value = AuthState.Success(authResult.user?.uid ?: "")
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        println("AuthViewModel: signInWithGoogle called with idToken: ${idToken.take(20)}...")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signInWithGoogle(idToken)
                result.fold(
                    onSuccess = { authResult ->
                        println("AuthViewModel: signInWithGoogle success, user: ${authResult.user?.email}")
                        _authState.value = AuthState.Success(authResult.user?.uid ?: "")
                    },
                    onFailure = { exception ->
                        println("AuthViewModel: signInWithGoogle failed: ${exception.message}")
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                println("AuthViewModel: signInWithGoogle exception: ${e.message}")
                _authState.value = AuthState.Error("Google sign in failed")
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signInAnonymously()
                result.fold(
                    onSuccess = { authResult ->
                        _authState.value = AuthState.Success(authResult.user?.uid ?: "")
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Anonymous sign in failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                val result = authService.sendPasswordResetEmail(email)
                result.fold(
                    onSuccess = {
                        _authState.value = AuthState.Success("")
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to send reset email")
            }
        }
    }

    fun sendSignInLinkToEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.sendSignInLinkToEmail(email)
                result.fold(
                    onSuccess = {
                        _authState.value = AuthState.Success("Email link sent successfully")
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to send email link")
            }
        }
    }

    fun signInWithEmailLink(email: String, emailLink: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authService.signInWithEmailLink(email, emailLink)
                result.fold(
                    onSuccess = { authResult ->
                        _authState.value = AuthState.Success(authResult.user?.uid ?: "")
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(getErrorMessage(exception as Exception))
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Email link sign in failed")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("password") == true -> "Invalid password"
            exception.message?.contains("email") == true -> "Invalid email address"
            exception.message?.contains("user") == true -> "User not found"
            exception.message?.contains("network") == true -> "Network error. Please check your connection"
            else -> exception.message ?: "Authentication failed"
        }
    }
}