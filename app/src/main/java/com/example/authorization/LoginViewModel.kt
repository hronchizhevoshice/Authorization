package com.example.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authorization.data.repository.SessionRepositoryImpl
import com.example.authorization.data.repository.UserRepositoryImpl
import com.example.authorization.domain.usecase.LoginUseCase
import com.example.authorization.domain.usecase.SessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val inn: String = "",
    val password: String = "",
    val innErr: String? = null,
    val passwordErr: String? = null,
    val isLoading: Boolean = false,
    val resultMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val loginUseCase = LoginUseCase(UserRepositoryImpl)
    private val sessionUseCase = SessionUseCase(SessionRepositoryImpl)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onInnChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(inn = digits, innErr = null) }
    }

    fun onPasswordChange(v: String) {
        _uiState.update { it.copy(password = v, passwordErr = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        val innErr = validateInn(state.inn)
        val passwordErr = if (state.password.isBlank()) "Введите пароль" else null

        if (innErr != null || passwordErr != null) {
            _uiState.update { it.copy(innErr = innErr, passwordErr = passwordErr) }
            return
        }

        _uiState.update { it.copy(isLoading = true, resultMessage = null) }
        viewModelScope.launch {
            val user = loginUseCase(state.inn, state.password)
            if (user != null) {
                sessionUseCase.save(user)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = "❌ Неверный ИНН или пароль"
                    )
                }
            }
        }
    }

    private fun validateInn(s: String): String? {
        if (s.isBlank()) return "Введите ИНН"
        if (!s.all { it.isDigit() }) return "ИНН: только цифры"
        if (s.length != 10) return "ИНН: ровно 10 цифр"
        return null
    }
}