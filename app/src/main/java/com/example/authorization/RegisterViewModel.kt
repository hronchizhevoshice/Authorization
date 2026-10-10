package com.example.authorization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class RegisterViewModel : ViewModel() {

    private val DADATA_TOKEN = "9030d41022d88502759c3ba88225a98e9223f1fb"

    // ---- Вводит пользователь ----
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordRepeat by mutableStateOf("")
    var firstname by mutableStateOf("")
    var lastname by mutableStateOf("")
    var bank by mutableStateOf("")
    var bik by mutableStateOf("")
    var rs by mutableStateOf("")
    var ks by mutableStateOf("")

    // ---- Автозаполнение по ИНН ----
    var inn by mutableStateOf("")
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

    // ---- Ошибки полей ----
    var emailErr by mutableStateOf<String?>(null)
    var phoneErr by mutableStateOf<String?>(null)
    var passwordErr by mutableStateOf<String?>(null)
    var passwordRepeatErr by mutableStateOf<String?>(null)
    var firstnameErr by mutableStateOf<String?>(null)
    var lastnameErr by mutableStateOf<String?>(null)
    var bankErr by mutableStateOf<String?>(null)
    var bikErr by mutableStateOf<String?>(null)
    var rsErr by mutableStateOf<String?>(null)
    var ksErr by mutableStateOf<String?>(null)
    var innErr by mutableStateOf<String?>(null)

    // ---- Состояние проверки ИНН ----
    var isCheckingInn by mutableStateOf(false)
        private set
    var innStatusMessage by mutableStateOf<String?>(null)
        private set

    // ---- Состояние регистрации ----
    var isLoading by mutableStateOf(false)
        private set
    var resultMessage by mutableStateOf<String?>(null)

    private var innCheckJob: Job? = null

    // ---- Обработчики ввода ----
    fun onEmailChange(v: String) { email = v; emailErr = null }
    fun onPhoneChange(v: String) {
        phone = v.filter { it.isDigit() }.take(11)
        phoneErr = null
    }
    fun onPasswordChange(v: String) { password = v; passwordErr = null }
    fun onPasswordRepeatChange(v: String) { passwordRepeat = v; passwordRepeatErr = null }
    fun onFirstnameChange(v: String) { firstname = v; firstnameErr = null }
    fun onLastnameChange(v: String) { lastname = v; lastnameErr = null }
    fun onBankChange(v: String) { bank = v; bankErr = null }
    fun onBikChange(v: String) { bik = v.filter { it.isDigit() }.take(9); bikErr = null }
    fun onRsChange(v: String) { rs = v.filter { it.isDigit() }.take(20); rsErr = null }
    fun onKsChange(v: String) { ks = v.filter { it.isDigit() }.take(20); ksErr = null }

    fun onInnChange(v: String) {
        val digits = v.filter { it.isDigit() }.take(10)
        inn = digits
        innErr = null
        kpp = ""; companyName = ""; form = ""; ogrn = ""; okpo = ""; companyAddress = ""
        innStatusMessage = null

        innCheckJob?.cancel()

        if (digits.length == 10) {
            innCheckJob = viewModelScope.launch(Dispatchers.IO) {
                delay(300)
                checkInnAndFill(digits)
            }
        }
    }

    private suspend fun checkInnAndFill(innValue: String) {
        withContext(Dispatchers.Main) {
            isCheckingInn = true
            innStatusMessage = "Проверка ИНН..."
        }

        // 1) Проверяем, нет ли уже такого ИНН в нашей БД
        val alreadyExists = DatabaseManager.isInnExists(innValue)

        // Если пользователь поменял ИНН за время запроса — выходим
        if (inn != innValue) {
            withContext(Dispatchers.Main) { isCheckingInn = false }
            return
        }

        if (alreadyExists) {
            withContext(Dispatchers.Main) {
                isCheckingInn = false
                innErr = "Организация с таким ИНН уже зарегистрирована"
                innStatusMessage = null
            }
            return
        }

        // 2) Если в БД нет — идём в ЕГРЮЛ
        withContext(Dispatchers.Main) {
            innStatusMessage = "Проверка ИНН в ЕГРЮЛ..."
        }

        val info = fetchFromEgryul(innValue)

        if (inn != innValue) {
            withContext(Dispatchers.Main) { isCheckingInn = false }
            return
        }

        withContext(Dispatchers.Main) {
            isCheckingInn = false
            if (info == null) {
                innErr = "Организация с таким ИНН не найдена в ЕГРЮЛ"
                innStatusMessage = null
            } else {
                kpp = info.kpp
                companyName = info.companyName
                form = info.form
                ogrn = info.ogrn
                okpo = info.okpo
                companyAddress = info.address
                innStatusMessage = "✅ ${info.companyName}"
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        resultMessage = null

        emailErr = validateEmail(email)
        phoneErr = validatePhone(phone)
        passwordErr = validatePassword(password)
        passwordRepeatErr = if (passwordRepeat != password) "Пароли не совпадают" else null
        firstnameErr = validateName(firstname, "Имя")
        lastnameErr = validateName(lastname, "Фамилия")
        bankErr = validateBank(bank)
        bikErr = validateBik(bik)
        rsErr = validateRs(rs, "Р/с")
        ksErr = validateRs(ks, "К/с")
        innErr = validateInn(inn)

        if (kpp.isBlank() && innErr == null) {
            innErr = "Дождитесь проверки ИНН"
        }

        val hasErrors = listOf(
            emailErr, phoneErr, passwordErr, passwordRepeatErr,
            firstnameErr, lastnameErr, bankErr, bikErr, rsErr, ksErr, innErr
        ).any { it != null }
        if (hasErrors) return

        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            // Защита от гонки: на случай, если между проверкой и нажатием
            // кто-то успел зарегистрировать этот ИНН
            if (DatabaseManager.isInnExists(inn)) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    innErr = "Организация с таким ИНН уже зарегистрирована"
                }
                return@launch
            }

            val data = RegisterData(
                email = email,
                phone = phone,
                password = password,
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
            val result = DatabaseManager.registerUser(data)
            withContext(Dispatchers.Main) {
                isLoading = false
                result.onSuccess { user ->
                    SessionManager.saveUser(user)
                    onSuccess()
                }.onFailure { e ->
                    resultMessage = "❌ Ошибка: ${e.message}"
                }
            }
        }
    }

    private data class EgryulInfo(
        val kpp: String,
        val companyName: String,
        val form: String,
        val ogrn: String,
        val okpo: String,
        val address: String
    )

    private suspend fun fetchFromEgryul(inn: String): EgryulInfo? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val bodyJson = """{"query":"$inn","count":1}"""
            val request = Request.Builder()
                .url("https://suggestions.dadata.ru/suggestions/api/4_1/rs/findById/party")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Token $DADATA_TOKEN")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val suggestions = json.optJSONArray("suggestions") ?: return@withContext null
                if (suggestions.length() == 0) return@withContext null

                val first = suggestions.getJSONObject(0)
                val data = first.optJSONObject("data") ?: return@withContext null

                val kpp = data.optString("kpp", "")
                if (kpp.isBlank()) return@withContext null

                val nameObj = data.optJSONObject("name")
                val companyName = nameObj?.optString("short_with_opf", "") ?: ""

                val opfObj = data.optJSONObject("opf")
                val form = opfObj?.optString("short", "") ?: ""

                val addressObj = data.optJSONObject("address")
                val address = addressObj?.optString("value", "") ?: ""

                EgryulInfo(
                    kpp = kpp,
                    companyName = companyName,
                    form = form,
                    ogrn = data.optString("ogrn", ""),
                    okpo = data.optString("okpo", ""),
                    address = address
                )
            }
        } catch (e: Exception) {
            null
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