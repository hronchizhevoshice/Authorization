package com.example.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authorization.data.repository.SessionRepositoryImpl
import com.example.authorization.data.repository.UserRepositoryImpl
import com.example.authorization.data.repository.VkRepositoryImpl
import com.example.authorization.domain.model.FullUser
import com.example.authorization.domain.usecase.GetProfileUseCase
import com.example.authorization.domain.usecase.ResolveVkIdUseCase
import com.example.authorization.domain.usecase.SessionUseCase
import com.example.authorization.domain.usecase.UpdateVkIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val profile: FullUser? = null,
    val isLoading: Boolean = true,
    val showVkDialog: Boolean = false,
    val vkInput: String = "",
    val isSavingVk: Boolean = false,
    val vkError: String? = null
)

class HomeViewModel : ViewModel() {
    private val sessionUseCase = SessionUseCase(SessionRepositoryImpl)
    private val getProfileUseCase = GetProfileUseCase(UserRepositoryImpl)
    private val updateVkIdUseCase = UpdateVkIdUseCase(UserRepositoryImpl)
    private val resolveVkIdUseCase = ResolveVkIdUseCase(VkRepositoryImpl)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Следим за сессией. Как только юзер меняется (вход/выход/регистрация) —
        // перезапрашиваем его полный профиль из БД
        viewModelScope.launch {
            sessionUseCase.observe().collect { sessionUser ->
                if (sessionUser == null) {
                    _uiState.update { it.copy(profile = null, isLoading = false) }
                } else {
                    val fullProfile = getProfileUseCase(sessionUser.id)
                    _uiState.update { it.copy(profile = fullProfile, isLoading = false) }
                }
            }
        }
    }

    fun openVkDialog() {
        val currentVk = _uiState.value.profile?.vkUserId?.toString() ?: ""
        _uiState.update { it.copy(showVkDialog = true, vkInput = currentVk, vkError = null) }
    }

    fun closeVkDialog() {
        _uiState.update { it.copy(showVkDialog = false) }
    }

    fun onVkInputChange(v: String) {
        _uiState.update { it.copy(vkInput = v, vkError = null) }
    }

    fun saveVk() {
        val input = _uiState.value.vkInput.trim()
        if (input.isBlank()) {
            _uiState.update { it.copy(vkError = "Введите ссылку или ID") }
            return
        }

        _uiState.update { it.copy(isSavingVk = true, vkError = null) }
        viewModelScope.launch {
            val resolveResult = resolveVkIdUseCase(input)

            resolveResult.onFailure { e ->
                _uiState.update { it.copy(isSavingVk = false, vkError = e.message) }
            }.onSuccess { vkId ->
                val sessionUser = sessionUseCase.get()
                if (sessionUser == null) {
                    _uiState.update { it.copy(isSavingVk = false) }
                    return@launch
                }

                val success = updateVkIdUseCase(sessionUser.id, vkId)
                if (success) {
                    _uiState.update {
                        it.copy(
                            isSavingVk = false,
                            showVkDialog = false,
                            // Обновляем только текущий профиль в памяти
                            profile = it.profile?.copy(vkUserId = vkId)
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSavingVk = false, vkError = "Ошибка сохранения в БД") }
                }
            }
        }
    }
}