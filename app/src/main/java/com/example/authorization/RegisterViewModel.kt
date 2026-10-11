package com.example.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authorization.data.repository.CompanyRepositoryImpl
import com.example.authorization.data.repository.SessionRepositoryImpl
import com.example.authorization.data.repository.UserRepositoryImpl
import com.example.authorization.domain.model.RegisterData
import com.example.authorization.domain.usecase.CheckInnExistsUseCase
import com.example.authorization.domain.usecase.FetchCompanyUseCase
import com.example.authorization.domain.usecase.RegisterUseCase
import com.example.authorization.domain.usecase.SessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    // ввод пользователя
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val bank: String = "",
    val bik: String = "",
    val rs: String = "",
    val ks: String = "",

    // автозаполнение по ИНН
    val inn: String = "",
    val kpp: String = "",
    val companyName: String = "",
    val form: String = "",
    val ogrn: String = "",
    val okpo: String = "",
    val companyAddress: String = "",

    // ошибки полей
    val emailErr: String? = null,
    val phoneErr: String? = null,
    val passwordErr: String? = null,
    val passwordRepeatErr: String? = null,
    val firstnameErr: String? = null,
    val lastnameErr: String? = null,
    val bankErr: String? = null,
    val bikErr: String? = null,
    val rsErr: String? = null,
    val ksErr: String? = null,
    val innErr: String? = null,

    // статусы
    val isCheckingInn: Boolean = false,
    val innStatusMessage: String? = null,
    val isLoading: Boolean = false,
    val resultMessage: String? = null
)

class RegisterViewModel : ViewModel() {

    private val checkInnExistsUseCase = CheckInnExistsUseCase(UserRepositoryImpl)
    private val fetchCompanyUseCase = FetchCompanyUseCase(CompanyRepositoryImpl)
    private val registerUseCase = RegisterUseCase(UserRepositoryImpl)
    private val sessionUseCase = SessionUseCase(SessionRepositoryImpl)

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var innCheckJob: Job? = null

    // ---- Обработчики ввода ----
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailErr = null) }
    fun onPhoneChange(v: String) = _uiState.update {
        it.copy(phone = v.filter { c -> c.isDigit() }.take(11), phoneErr = null)
    }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordErr = null) }
    fun onPasswordRepeatChange(v: String) = _uiState.update { it.copy(passwordRepeat = v, passwordRepeatErr = null) }
    fun onFirstnameChange(v: String) = _uiState.update { it.copy(firstname = v, firstnameErr = null) }
    fun onLastnameChange(v: String) = _uiState.update { it.copy(lastname = v, lastnameErr = null) }
    fun onBankChange(v: String) = _uiState.update {
        it.copy(bank = v.take(100), bankErr = null)
    }
    fun onBikChange(v: String) = _uiState.update {
        it.copy(bik = v.filter { c -> c.isDigit() }.take(9), bikErr = null)
    }
    fun onRsChange(v: String) = _uiState.update {
        it.copy(rs = v.filter { c -> c.isDigit() }.take(20), rsErr = null)
    }
    fun onKsChange(v: String) = _uiState.update {
        it.copy(ks = v.filter { c -> c.isDigit() }.take(20), ksErr = null)
    }

    fun onInnChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(10)
        _uiState.update {
            it.copy(
                inn = digits,
                innErr = null,
                kpp = "",
                companyName = "",
                form = "",
                ogrn = "",
                okpo = "",
                companyAddress = "",
                innStatusMessage = null
            )
        }

        innCheckJob?.cancel()

        if (digits.length == 10) {
            innCheckJob = viewModelScope.launch {
                delay(300)
                checkInnAndFill(digits)
            }
        }
    }

    private suspend fun checkInnAndFill(innValue: String) {
        _uiState.update { it.copy(isCheckingInn = true, innStatusMessage = "Проверка ИНН...") }

        val alreadyExists = checkInnExistsUseCase(innValue)

        // Если пользователь поменял ИНН за время запроса — выходим
        if (_uiState.value.inn != innValue) {
            _uiState.update { it.copy(isCheckingInn = false) }
            return
        }

        if (alreadyExists) {
            _uiState.update {
                it.copy(
                    isCheckingInn = false,
                    innErr = "Организация с таким ИНН уже зарегистрирована",
                    innStatusMessage = null
                )
            }
            return
        }

        _uiState.update { it.copy(innStatusMessage = "Проверка ИНН в ЕГРЮЛ...") }
        val info = fetchCompanyUseCase(innValue)

        if (_uiState.value.inn != innValue) {
            _uiState.update { it.copy(isCheckingInn = false) }
            return
        }

        if (info == null) {
            _uiState.update {
                it.copy(
                    isCheckingInn = false,
                    innErr = "Организация с таким ИНН не найдена в ЕГРЮЛ",
                    innStatusMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isCheckingInn = false,
                    kpp = info.kpp,
                    companyName = info.companyName,
                    form = info.form,
                    ogrn = info.ogrn,
                    okpo = info.okpo,
                    companyAddress = info.address,
                    innStatusMessage = "✅ ${info.companyName}"
                )
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val s = _uiState.value

        val emailErr = validateEmail(s.email)
        val phoneErr = validatePhone(s.phone)
        val passwordErr = validatePassword(s.password)
        val passwordRepeatErr = if (s.passwordRepeat != s.password) "Пароли не совпадают" else null
        val firstnameErr = validateRequiredName(s.firstname, "Имя")
        val lastnameErr = validateName(s.lastname, "Фамилия")
        val bankErr = validateBank(s.bank)
        val bikErr = validateBik(s.bik)
        val rsErr = validateRs(s.rs, "Р/с")
        val ksErr = validateRs(s.ks, "К/с")
        var innErr = validateInn(s.inn)

        if (s.kpp.isBlank() && innErr == null) {
            innErr = "Дождитесь проверки ИНН"
        }

        val hasErrors = listOf(
            emailErr, phoneErr, passwordErr, passwordRepeatErr,
            firstnameErr, lastnameErr, bankErr, bikErr, rsErr, ksErr, innErr
        ).any { it != null }

        if (hasErrors) {
            _uiState.update {
                it.copy(
                    emailErr = emailErr,
                    phoneErr = phoneErr,
                    passwordErr = passwordErr,
                    passwordRepeatErr = passwordRepeatErr,
                    firstnameErr = firstnameErr,
                    lastnameErr = lastnameErr,
                    bankErr = bankErr,
                    bikErr = bikErr,
                    rsErr = rsErr,
                    ksErr = ksErr,
                    innErr = innErr
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, resultMessage = null) }
        viewModelScope.launch {
            if (checkInnExistsUseCase(s.inn)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        innErr = "Организация с таким ИНН уже зарегистрирована"
                    )
                }
                return@launch
            }

            val data = RegisterData(
                email = s.email,
                phone = s.phone,
                password = s.password,
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
            val result = registerUseCase(data)
            result.onSuccess { user ->
                sessionUseCase.save(user)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, resultMessage = "❌ Ошибка: ${e.message}")
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
    private fun validatePassword(s: String): String? {
        if (s.isBlank()) return "Введите пароль"
        if (s.length < 6) return "Пароль минимум 6 символов"
        return null
    }
    private fun validateInn(s: String): String? {
        if (s.isBlank()) return "Введите ИНН"
        if (!s.all { it.isDigit() }) return "ИНН: только цифры"
        if (s.length != 10) return "ИНН: ровно 10 цифр"
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