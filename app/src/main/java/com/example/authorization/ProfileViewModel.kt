package com.example.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authorization.data.repository.SessionRepositoryImpl
import com.example.authorization.data.repository.UserRepositoryImpl
import com.example.authorization.domain.model.FullUser
import com.example.authorization.domain.model.User
import com.example.authorization.domain.usecase.GetProfileUseCase
import com.example.authorization.domain.usecase.SessionUseCase
import com.example.authorization.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    // read-only
    val inn: String = "",
    val kpp: String = "",
    val companyName: String = "",
    val form: String = "",
    val ogrn: String = "",
    val okpo: String = "",
    val companyAddress: String = "",
    // editable
    val email: String = "",
    val phone: String = "",
    val newPassword: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val bank: String = "",
    val bik: String = "",
    val rs: String = "",
    val ks: String = "",
    // errors
    val emailErr: String? = null,
    val phoneErr: String? = null,
    val passwordErr: String? = null,
    val firstnameErr: String? = null,
    val lastnameErr: String? = null,
    val bankErr: String? = null,
    val bikErr: String? = null,
    val rsErr: String? = null,
    val ksErr: String? = null,
    // statuses
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val resultMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val getProfileUseCase = GetProfileUseCase(UserRepositoryImpl)
    private val updateProfileUseCase = UpdateProfileUseCase(UserRepositoryImpl)
    private val sessionUseCase = SessionUseCase(SessionRepositoryImpl)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var userId: Long = -1
    private var currentPassword: String = ""

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val sessionUser = sessionUseCase.get()
            if (sessionUser == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            userId = sessionUser.id

            val full = getProfileUseCase(userId)
            if (full == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resultMessage = "❌ Не удалось загрузить данные"
                    )
                }
                return@launch
            }

            currentPassword = full.password
            _uiState.update {
                it.copy(
                    isLoading = false,
                    email = full.email,
                    phone = full.phone,
                    firstname = full.firstname,
                    lastname = full.lastname,
                    bank = full.bank,
                    bik = full.bik,
                    rs = full.rs,
                    ks = full.ks,
                    inn = full.inn,
                    kpp = full.kpp,
                    companyName = full.companyName,
                    form = full.form,
                    ogrn = full.ogrn,
                    okpo = full.okpo,
                    companyAddress = full.companyAddress
                )
            }
        }
    }

    // ---- Обработчики ввода ----
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailErr = null, resultMessage = null) }
    fun onPhoneChange(v: String) = _uiState.update {
        it.copy(phone = v.filter { c -> c.isDigit() }.take(11), phoneErr = null, resultMessage = null)
    }
    fun onNewPasswordChange(v: String) = _uiState.update { it.copy(newPassword = v, passwordErr = null, resultMessage = null) }
    fun onFirstnameChange(v: String) = _uiState.update { it.copy(firstname = v, firstnameErr = null, resultMessage = null) }
    fun onLastnameChange(v: String) = _uiState.update { it.copy(lastname = v, lastnameErr = null, resultMessage = null) }
    fun onBankChange(v: String) = _uiState.update {
        it.copy(bank = v.take(100), bankErr = null, resultMessage = null)
    }
    fun onBikChange(v: String) = _uiState.update {
        it.copy(bik = v.filter { c -> c.isDigit() }.take(9), bikErr = null, resultMessage = null)
    }
    fun onRsChange(v: String) = _uiState.update {
        it.copy(rs = v.filter { c -> c.isDigit() }.take(20), rsErr = null, resultMessage = null)
    }
    fun onKsChange(v: String) = _uiState.update {
        it.copy(ks = v.filter { c -> c.isDigit() }.take(20), ksErr = null, resultMessage = null)
    }

    fun save() {
        val s = _uiState.value
        val emailErr = validateEmail(s.email)
        val phoneErr = validatePhone(s.phone)
        val passwordErr = if (s.newPassword.isNotBlank() && s.newPassword.length < 6)
            "Пароль минимум 6 символов" else null
        val firstnameErr = validateRequiredName(s.firstname, "Имя")
        val lastnameErr = validateName(s.lastname, "Фамилия")
        val bankErr = validateBank(s.bank)
        val bikErr = validateBik(s.bik)
        val rsErr = validateRs(s.rs, "Р/с")
        val ksErr = validateRs(s.ks, "К/с")

        val hasErrors = listOf(
            emailErr, phoneErr, passwordErr,
            firstnameErr, lastnameErr, bankErr, bikErr, rsErr, ksErr
        ).any { it != null }

        if (hasErrors) {
            _uiState.update {
                it.copy(
                    emailErr = emailErr,
                    phoneErr = phoneErr,
                    passwordErr = passwordErr,
                    firstnameErr = firstnameErr,
                    lastnameErr = lastnameErr,
                    bankErr = bankErr,
                    bikErr = bikErr,
                    rsErr = rsErr,
                    ksErr = ksErr
                )
            }
            return
        }

        val finalPassword = if (s.newPassword.isNotBlank()) s.newPassword else currentPassword

        _uiState.update { it.copy(isSaving = true, resultMessage = null) }
        viewModelScope.launch {
            val updated = FullUser(
                id = userId,
                email = s.email,
                phone = s.phone,
                password = finalPassword,
                companyName = s.companyName,
                inn = s.inn,
                kpp = s.kpp,
                form = s.form,
                ogrn = s.ogrn,
                okpo = s.okpo,
                companyAddress = s.companyAddress,
                firstname = s.firstname,
                lastname = s.lastname,
                bank = s.bank,
                bik = s.bik,
                rs = s.rs,
                ks = s.ks
            )
            val result = updateProfileUseCase(updated)
            result.onSuccess {
                currentPassword = finalPassword
                sessionUseCase.save(User(id = userId, email = s.email, companyName = s.companyName))
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        newPassword = "",
                        resultMessage = "✅ Данные сохранены"
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSaving = false, resultMessage = "❌ Ошибка: ${e.message}")
                }
            }
        }
    }

    // ---- Валидация ----
    private fun validateEmail(s: String): String? {
        if (s.isBlank()) return "Введите email"
        val regex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
        if (!regex.matches(s)) return "Некорректный email"
        return null
    }
    private fun validatePhone(s: String): String? {
        if (s.isBlank()) return "Введите телефон"
        val digits = s.filter { it.isDigit() }
        if (digits.length < 10 || digits.length > 11) return "Телефон: 10–11 цифр"
        return null
    }

    private fun validateRequiredName(s: String, field: String): String? {
        if (s.isBlank()) return "Введите $field"
        if (s.length < 2) return "$field: минимум 2 символа"
        if (!s.all { it.isLetter() || it == '-' || it == ' ' }) return "$field: только буквы"
        return null
    }

    private fun validateName(s: String, field: String): String? {
        if (s.isBlank()) return null
        if (s.length < 2) return "$field: минимум 2 символа"
        if (!s.all { it.isLetter() || it == '-' || it == ' ' }) return "$field: только буквы"
        return null
    }
    private fun validateBank(s: String): String? {
        if (s.isBlank()) return null
        if (s.length < 3) return "Банк: минимум 3 символа"
        return null
    }
    private fun validateBik(s: String): String? {
        if (s.isBlank()) return null
        if (s.length != 9) return "БИК: 9 цифр"
        return null
    }
    private fun validateRs(s: String, field: String): String? {
        if (s.isBlank()) return null
        if (s.length != 20) return "$field: 20 цифр"
        return null
    }
}