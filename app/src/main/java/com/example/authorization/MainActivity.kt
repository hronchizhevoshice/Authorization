package com.example.authorization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authorization.ui.theme.AuthorizationTheme

enum class Screen { CHOICE, LOGIN, REGISTER, HOME, PROFILE, VEHICLES, ORDERS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionManager.init(this)
        val startScreen = if (SessionManager.getUser() != null) Screen.HOME else Screen.CHOICE

        setContent {
            AuthorizationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var screen by remember { mutableStateOf(startScreen) }
                    var loginKey by remember { mutableStateOf(0) }
                    var registerKey by remember { mutableStateOf(0) }
                    var profileKey by remember { mutableStateOf(0) }

                    val baseModifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)

                    when (screen) {
                        Screen.CHOICE -> ChoiceScreen(
                            modifier = baseModifier,
                            onLogin = {
                                loginKey++
                                screen = Screen.LOGIN
                            },
                            onRegister = {
                                registerKey++
                                screen = Screen.REGISTER
                            }
                        )
                        Screen.LOGIN -> LoginScreen(
                            modifier = baseModifier,
                            vmKey = loginKey,
                            onBack = { screen = Screen.CHOICE },
                            onLoginSuccess = { screen = Screen.HOME }
                        )
                        Screen.REGISTER -> RegisterScreen(
                            modifier = baseModifier,
                            vmKey = registerKey,
                            onBack = { screen = Screen.CHOICE },
                            onRegisterSuccess = { screen = Screen.HOME }
                        )
                        Screen.HOME -> HomeScreen(
                            modifier = baseModifier,
                            onProfile = {
                                profileKey++
                                screen = Screen.PROFILE
                            },
                            onVehicles = { screen = Screen.VEHICLES },
                            onOrders = { screen = Screen.ORDERS },
                            onLogout = { screen = Screen.CHOICE }
                        )
                        Screen.PROFILE -> ProfileScreen(
                            modifier = baseModifier,
                            vmKey = profileKey,
                            onBack = { screen = Screen.HOME }
                        )
                        Screen.VEHICLES -> PlaceholderScreen(
                            modifier = baseModifier,
                            title = "Техника",
                            onBack = { screen = Screen.HOME }
                        )
                        Screen.ORDERS -> PlaceholderScreen(
                            modifier = baseModifier,
                            title = "Заказы",
                            onBack = { screen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }
}

// ---------- CHOICE ----------
@Composable
fun ChoiceScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Авторизация", style = MaterialTheme.typography.headlineMedium)
        Text("Выберите действие", style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text("Войти") }

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Зарегистрироваться") }
    }
}

// ---------- LOGIN ----------
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vmKey: Int,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel(key = "login_$vmKey")
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Вход", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = vm.inn,
            onValueChange = vm::onInnChange,
            label = { Text("ИНН") },
            isError = vm.innErr != null,
            supportingText = { vm.innErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Пароль") },
            isError = vm.passwordErr != null,
            supportingText = { vm.passwordErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.login(onLoginSuccess) },
            enabled = !vm.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (vm.isLoading) "Проверка..." else "ВОЙТИ") }

        vm.resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("← Назад")
        }
    }
}

// ---------- REGISTER ----------
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    vmKey: Int,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    vm: RegisterViewModel = viewModel(key = "register_$vmKey")
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Регистрация", style = MaterialTheme.typography.headlineSmall)

        Text("Контактные данные", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = vm.email,
            onValueChange = vm::onEmailChange,
            label = { Text("E-mail*") },
            isError = vm.emailErr != null,
            supportingText = { vm.emailErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.phone,
            onValueChange = vm::onPhoneChange,
            label = { Text("Телефон*") },
            isError = vm.phoneErr != null,
            supportingText = { vm.phoneErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.firstname,
            onValueChange = vm::onFirstnameChange,
            label = { Text("Имя") },
            isError = vm.firstnameErr != null,
            supportingText = { vm.firstnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.lastname,
            onValueChange = vm::onLastnameChange,
            label = { Text("Фамилия") },
            isError = vm.lastnameErr != null,
            supportingText = { vm.lastnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Пароль*") },
            isError = vm.passwordErr != null,
            supportingText = { vm.passwordErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.passwordRepeat,
            onValueChange = vm::onPasswordRepeatChange,
            label = { Text("Повторите пароль*") },
            isError = vm.passwordRepeatErr != null,
            supportingText = { vm.passwordRepeatErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Данные компании", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = vm.inn,
            onValueChange = vm::onInnChange,
            label = { Text("ИНН*") },
            isError = vm.innErr != null,
            supportingText = { vm.innErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.companyName,
            onValueChange = { },
            label = { Text("Название фирмы (авто)") },
            enabled = false,
            supportingText = { vm.innStatusMessage?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.form,
            onValueChange = { },
            label = { Text("Форма (авто)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.kpp,
            onValueChange = { },
            label = { Text("КПП (авто)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.ogrn,
            onValueChange = { },
            label = { Text("ОГРН (авто)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.okpo,
            onValueChange = { },
            label = { Text("ОКПО (авто)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.companyAddress,
            onValueChange = { },
            label = { Text("Юридический адрес (авто)") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Банковские реквизиты", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = vm.bank,
            onValueChange = vm::onBankChange,
            label = { Text("Банк") },
            isError = vm.bankErr != null,
            supportingText = { vm.bankErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.bik,
            onValueChange = vm::onBikChange,
            label = { Text("БИК") },
            isError = vm.bikErr != null,
            supportingText = { vm.bikErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.rs,
            onValueChange = vm::onRsChange,
            label = { Text("Р/с") },
            isError = vm.rsErr != null,
            supportingText = { vm.rsErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.ks,
            onValueChange = vm::onKsChange,
            label = { Text("К/с") },
            isError = vm.ksErr != null,
            supportingText = { vm.ksErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.register(onRegisterSuccess) },
            enabled = !vm.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (vm.isLoading) "Запись..." else "РЕГИСТРАЦИЯ") }

        vm.resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("← Назад")
        }
    }
}

// ---------- HOME ----------
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onProfile: () -> Unit,
    onVehicles: () -> Unit,
    onOrders: () -> Unit,
    onLogout: () -> Unit
) {
    val user = SessionManager.getUser()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Главный экран", style = MaterialTheme.typography.headlineMedium)

        if (user != null) {
            Text("Вы вошли как:", style = MaterialTheme.typography.bodyMedium)
            Text(user.companyName, style = MaterialTheme.typography.titleLarge)
            Text(user.email, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = onProfile,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text("Данные") }

        Button(
            onClick = onVehicles,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Техника") }

        Button(
            onClick = onOrders,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Заказы") }

        Button(
            onClick = {
                SessionManager.clear()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text("Выйти") }
    }
}

// ---------- PROFILE ----------
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    vmKey: Int,
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel(key = "profile_$vmKey")
) {
    if (vm.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Загрузка данных...", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Данные профиля", style = MaterialTheme.typography.headlineSmall)

        Text("Контактные данные", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = vm.email,
            onValueChange = vm::onEmailChange,
            label = { Text("E-mail*") },
            isError = vm.emailErr != null,
            supportingText = { vm.emailErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.phone,
            onValueChange = vm::onPhoneChange,
            label = { Text("Телефон*") },
            isError = vm.phoneErr != null,
            supportingText = { vm.phoneErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.firstname,
            onValueChange = vm::onFirstnameChange,
            label = { Text("Имя") },
            isError = vm.firstnameErr != null,
            supportingText = { vm.firstnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.lastname,
            onValueChange = vm::onLastnameChange,
            label = { Text("Фамилия") },
            isError = vm.lastnameErr != null,
            supportingText = { vm.lastnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.newPassword,
            onValueChange = vm::onNewPasswordChange,
            label = { Text("Новый пароль (пусто = не менять)") },
            isError = vm.passwordErr != null,
            supportingText = { vm.passwordErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Данные компании (только просмотр)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = vm.inn,
            onValueChange = { },
            label = { Text("ИНН") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.companyName,
            onValueChange = { },
            label = { Text("Название фирмы") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.form,
            onValueChange = { },
            label = { Text("Форма") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.kpp,
            onValueChange = { },
            label = { Text("КПП") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.ogrn,
            onValueChange = { },
            label = { Text("ОГРН") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.okpo,
            onValueChange = { },
            label = { Text("ОКПО") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.companyAddress,
            onValueChange = { },
            label = { Text("Юридический адрес") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Банковские реквизиты", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = vm.bank,
            onValueChange = vm::onBankChange,
            label = { Text("Банк") },
            isError = vm.bankErr != null,
            supportingText = { vm.bankErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.bik,
            onValueChange = vm::onBikChange,
            label = { Text("БИК") },
            isError = vm.bikErr != null,
            supportingText = { vm.bikErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.rs,
            onValueChange = vm::onRsChange,
            label = { Text("Р/с") },
            isError = vm.rsErr != null,
            supportingText = { vm.rsErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = vm.ks,
            onValueChange = vm::onKsChange,
            label = { Text("К/с") },
            isError = vm.ksErr != null,
            supportingText = { vm.ksErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.save() },
            enabled = !vm.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (vm.isSaving) "Сохранение..." else "СОХРАНИТЬ") }

        vm.resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("← Назад")
        }
    }
}

// ---------- PLACEHOLDER (Техника / Заказы) ----------
@Composable
fun PlaceholderScreen(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text("Раздел в разработке", style = MaterialTheme.typography.bodyMedium)

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text("← Назад")
        }
    }
}