package com.example.authorization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {

    // ---- Read-only (из ЕГРЮЛ) ----
    var inn by mutableStateOf("")
        private set
    var kpp by mutableStateOf("")
        private set
    var companyName by mutableStateOf("")
        private set
    var form by mutableStateOf("")
        private set
    var ogrn by mutableStateOf("")
        private set
    var okpo by mutableStateOf("")
        private set
    var companyAddress by mutableStateOf("")
        private set

    // ---- Редактируемые ----
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var newPassword by mutableStateOf("")   // пусто = не менять
    var firstname by mutableStateOf("")
    var lastname by mutableStateOf("")
    var bank by mutableStateOf("")
    var bik by mutableStateOf("")
    var rs by mutableStateOf("")
    var ks by mutableStateOf("")

    // ---- Служебное ----
    private var userId: Long = -1
    private var currentPassword: String = ""

    // ---- Ошибки ----
    var emailErr by mutableStateOf<String?>(null)
    var phoneErr by mutableStateOf<String?>(null)
    var passwordErr by mutableStateOf<String?>(null)
    var firstnameErr by mutableStateOf<String?>(null)
    var lastnameErr by mutableStateOf<String?>(null)
    var bankErr by mutableStateOf<String?>(null)
    var bikErr by mutableStateOf<String?>(null)
    var rsErr by mutableStateOf<String?>(null)
    var ksErr by mutableStateOf<String?>(null)

    // ---- Статусы ----
    var isLoading by mutableStateOf(true)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var resultMessage by mutableStateOf<String?>(null)

    init {
        load()
    }

    private fun load() {
        val sessionUser = SessionManager.getUser()
        if (sessionUser == null) {
            isLoading = false
            return
        }
        userId = sessionUser.id

        viewModelScope.launch(Dispatchers.IO) {
            val full = DatabaseManager.getUserById(userId)
            withContext(Dispatchers.Main) {
                isLoading = false
                if (full == null) {
                    resultMessage = "❌ Не удалось загрузить данные"
                    return@withContext
                }
                email = full.email
                phone = full.phone
                currentPassword = full.password
                firstname = full.firstname
                lastname = full.lastname
                bank = full.bank
                bik = full.bik
                rs = full.rs
                ks = full.ks

                inn = full.inn
                kpp = full.kpp
                companyName = full.companyName
                form = full.form
                ogrn = full.ogrn
                okpo = full.okpo
                companyAddress = full.companyAddress
            }
        }
    }

    // ---- Обработчики ввода ----
    fun onEmailChange(v: String) { email = v; emailErr = null; resultMessage = null }
    fun onPhoneChange(v: String) { phone = v.filter { it.isDigit() }.take(11); phoneErr = null; resultMessage = null }
    fun onNewPasswordChange(v: String) { newPassword = v; passwordErr = null; resultMessage = null }
    fun onFirstnameChange(v: String) { firstname = v; firstnameErr = null; resultMessage = null }
    fun onLastnameChange(v: String) { lastname = v; lastnameErr = null; resultMessage = null }
    fun onBankChange(v: String) { bank = v; bankErr = null; resultMessage = null }
    fun onBikChange(v: String) { bik = v.filter { it.isDigit() }.take(9); bikErr = null; resultMessage = null }
    fun onRsChange(v: String) { rs = v.filter { it.isDigit() }.take(20); rsErr = null; resultMessage = null }
    fun onKsChange(v: String) { ks = v.filter { it.isDigit() }.take(20); ksErr = null; resultMessage = null }

    // ---- Сохранение ----
    fun save() {
        resultMessage = null
        emailErr = validateEmail(email)
        phoneErr = validatePhone(phone)
        passwordErr = if (newPassword.isNotBlank() && newPassword.length < 6)
            "Пароль минимум 6 символов" else null
        firstnameErr = validateName(firstname, "Имя")
        lastnameErr = validateName(lastname, "Фамилия")
        bankErr = validateBank(bank)
        bikErr = validateBik(bik)
        rsErr = validateRs(rs, "Р/с")
        ksErr = validateRs(ks, "К/с")

        val hasErrors = listOf(
            emailErr, phoneErr, passwordErr,
            firstnameErr, lastnameErr, bankErr, bikErr, rsErr, ksErr
        ).any { it != null }
        if (hasErrors) return

        val finalPassword = if (newPassword.isNotBlank()) newPassword else currentPassword

        isSaving = true
        viewModelScope.launch(Dispatchers.IO) {
            val updated = FullUser(
                id = userId,
                email = email,
                phone = phone,
                password = finalPassword,
                companyName = companyName,
                inn = inn,
                kpp = kpp,
                form = form,
                ogrn = ogrn,
                okpo = okpo,
                companyAddress = companyAddress,
                firstname = firstname,
                lastname = lastname,
                bank = bank,
                bik = bik,
                rs = rs,
                ks = ks
            )
            val result = DatabaseManager.updateUser(updated)
            withContext(Dispatchers.Main) {
                isSaving = false
                result.onSuccess {
                    currentPassword = finalPassword
                    newPassword = ""
                    SessionManager.saveUser(
                        User(id = userId, email = email, companyName = companyName)
                    )
                    resultMessage = "✅ Данные сохранены"
                }.onFailure { e ->
                    resultMessage = "❌ Ошибка: ${e.message}"
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