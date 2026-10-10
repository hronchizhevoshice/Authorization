package com.example.authorization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    var inn by mutableStateOf("")
    var password by mutableStateOf("")

    var innErr by mutableStateOf<String?>(null)
    var passwordErr by mutableStateOf<String?>(null)

    var isLoading by mutableStateOf(false)
        private set
    var resultMessage by mutableStateOf<String?>(null)

    fun onInnChange(v: String) {
        inn = v.filter { it.isDigit() }.take(10)
        innErr = null
    }
    fun onPasswordChange(v: String) { password = v; passwordErr = null }

    fun login(onSuccess: () -> Unit) {
        resultMessage = null
        innErr = validateInn(inn)
        passwordErr = if (password.isBlank()) "Введите пароль" else null
        if (innErr != null || passwordErr != null) return

        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val user = DatabaseManager.loginUser(inn, password)
            withContext(Dispatchers.Main) {
                isLoading = false
                if (user != null) {
                    SessionManager.saveUser(user)
                    onSuccess()
                } else {
                    resultMessage = "❌ Неверный ИНН или пароль"
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