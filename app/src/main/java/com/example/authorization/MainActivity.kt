package com.example.authorization

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.authorization.data.local.SessionDataStore
import com.example.authorization.data.repository.SessionRepositoryImpl
import com.example.authorization.domain.usecase.SessionUseCase
import com.example.authorization.presentation.ChoiceKey
import com.example.authorization.presentation.HomeKey
import com.example.authorization.presentation.LoginKey
import com.example.authorization.presentation.OrdersKey
import com.example.authorization.presentation.ProfileKey
import com.example.authorization.presentation.RegisterKey
import com.example.authorization.presentation.VehiclesKey
import com.example.authorization.ui.theme.AuthorizationTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionDataStore.init(this)

        setContent {
            AuthorizationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoot(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    val sessionUseCase = remember { SessionUseCase(SessionRepositoryImpl) }
    var startKey by remember { mutableStateOf<NavKey?>(null) }

    LaunchedEffect(Unit) {
        startKey = if (sessionUseCase.get() != null) HomeKey else ChoiceKey
    }

    val key = startKey
    if (key == null) {
        Box(modifier = modifier.fillMaxSize())
    } else {
        AppNav(startKey = key, modifier = modifier)
    }
}

@Composable
fun AppNav(
    startKey: NavKey,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(startKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = entryProvider<NavKey> {
            entry<ChoiceKey> {
                ChoiceScreen(
                    modifier = Modifier,
                    onLogin = { backStack.add(LoginKey) },
                    onRegister = { backStack.add(RegisterKey) }
                )
            }
            entry<LoginKey> {
                LoginScreen(
                    modifier = Modifier,
                    onBack = { backStack.removeLastOrNull() },
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.add(HomeKey)
                    }
                )
            }
            entry<RegisterKey> {
                RegisterScreen(
                    modifier = Modifier,
                    onBack = { backStack.removeLastOrNull() },
                    onRegisterSuccess = {
                        backStack.clear()
                        backStack.add(HomeKey)
                    }
                )
            }
            entry<HomeKey> {
                HomeScreen(
                    modifier = Modifier,
                    onProfile = { backStack.add(ProfileKey) },
                    onVehicles = { backStack.add(VehiclesKey) },
                    onOrders = { backStack.add(OrdersKey) },
                    onLogout = {
                        backStack.clear()
                        backStack.add(ChoiceKey)
                    }
                )
            }
            entry<ProfileKey> {
                ProfileScreen(
                    modifier = Modifier,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<VehiclesKey> {
                PlaceholderScreen(
                    modifier = Modifier,
                    title = stringResource(R.string.btn_vehicles),
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<OrdersKey> {
                PlaceholderScreen(
                    modifier = Modifier,
                    title = stringResource(R.string.btn_orders),
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
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
        Text(stringResource(R.string.choice_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.choice_subtitle), style = MaterialTheme.typography.bodyMedium)

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text(stringResource(R.string.btn_login)) }

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text(stringResource(R.string.btn_register)) }
    }
}

// ---------- LOGIN ----------
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.inn,
            onValueChange = vm::onInnChange,
            label = { Text(stringResource(R.string.label_inn)) },
            isError = state.innErr != null,
            supportingText = { state.innErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = { Text(stringResource(R.string.label_password)) },
            isError = state.passwordErr != null,
            supportingText = { state.passwordErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.login(onLoginSuccess) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (state.isLoading) R.string.btn_checking else R.string.btn_login_upper
                )
            )
        }

        state.resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_back))
        }
    }
}

// ---------- REGISTER ----------
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    vm: RegisterViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineSmall)

        Text(stringResource(R.string.section_contacts), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            label = { Text(stringResource(R.string.label_email_required)) },
            isError = state.emailErr != null,
            supportingText = { state.emailErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            label = { Text(stringResource(R.string.label_phone_required)) },
            isError = state.phoneErr != null,
            supportingText = { state.phoneErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.firstname,
            onValueChange = vm::onFirstnameChange,
            label = { Text(stringResource(R.string.label_firstname_required)) },
            isError = state.firstnameErr != null,
            supportingText = { state.firstnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.lastname,
            onValueChange = vm::onLastnameChange,
            label = { Text(stringResource(R.string.label_lastname)) },
            isError = state.lastnameErr != null,
            supportingText = { state.lastnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = { Text(stringResource(R.string.label_password_required)) },
            isError = state.passwordErr != null,
            supportingText = { state.passwordErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.passwordRepeat,
            onValueChange = vm::onPasswordRepeatChange,
            label = { Text(stringResource(R.string.label_password_repeat_required)) },
            isError = state.passwordRepeatErr != null,
            supportingText = { state.passwordRepeatErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.section_company), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.inn,
            onValueChange = vm::onInnChange,
            label = { Text(stringResource(R.string.label_inn_required)) },
            isError = state.innErr != null,
            supportingText = { state.innErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.companyName,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_company_name_auto)) },
            enabled = false,
            supportingText = { state.innStatusMessage?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.form,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_form_auto)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.kpp,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_kpp_auto)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.ogrn,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_ogrn_auto)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.okpo,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_okpo_auto)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.companyAddress,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_address_auto)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.section_bank), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.bank,
            onValueChange = vm::onBankChange,
            label = { Text(stringResource(R.string.label_bank)) },
            isError = state.bankErr != null,
            supportingText = { state.bankErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.bik,
            onValueChange = vm::onBikChange,
            label = { Text(stringResource(R.string.label_bik)) },
            isError = state.bikErr != null,
            supportingText = { state.bikErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.rs,
            onValueChange = vm::onRsChange,
            label = { Text(stringResource(R.string.label_rs)) },
            isError = state.rsErr != null,
            supportingText = { state.rsErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.ks,
            onValueChange = vm::onKsChange,
            label = { Text(stringResource(R.string.label_ks)) },
            isError = state.ksErr != null,
            supportingText = { state.ksErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.register(onRegisterSuccess) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (state.isLoading) R.string.btn_writing else R.string.btn_register_upper
                )
            )
        }

        state.resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_back))
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
    onLogout: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val sessionUseCase = remember { SessionUseCase(SessionRepositoryImpl) }
    val scope = rememberCoroutineScope()
    val user by sessionUseCase.observe().collectAsState(initial = null)
    val state by vm.uiState.collectAsStateWithLifecycle()

    if (state.showVkDialog) {
        AlertDialog(
            onDismissRequest = { vm.closeVkDialog() },
            title = { Text(stringResource(R.string.dialog_vk_title)) },
            text = {
                Column {
                    Text("Введите ID профиля ВКонтакте:")
                    OutlinedTextField(
                        value = state.vkInput,
                        onValueChange = vm::onVkInputChange,
                        placeholder = { Text(stringResource(R.string.dialog_vk_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    state.vkError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.saveVk() }, enabled = !state.isSavingVk) {
                    Text(if (state.isSavingVk) "..." else stringResource(R.string.btn_attach))
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.closeVkDialog() }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)

        user?.let { u ->
            Text(stringResource(R.string.label_logged_in_as), style = MaterialTheme.typography.bodyMedium)
            Text(u.companyName, style = MaterialTheme.typography.titleLarge)
            Text(u.email, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = onProfile,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text(stringResource(R.string.btn_data)) }

        Button(
            onClick = onVehicles,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text(stringResource(R.string.btn_vehicles)) }

        Button(
            onClick = onOrders,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text(stringResource(R.string.btn_orders)) }

        // Кнопка ВК
        val vkId = state.profile?.vkUserId
        Button(
            onClick = { vm.openVkDialog() },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(
                if (vkId != null) "Изменить ВК (ID: $vkId)"
                else stringResource(R.string.btn_link_vk)
            )
        }

        Button(
            onClick = {
                scope.launch {
                    sessionUseCase.clear()
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) { Text(stringResource(R.string.btn_logout)) }
    }
}

// ---------- PROFILE ----------
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineSmall)

        Text(stringResource(R.string.section_contacts), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            label = { Text(stringResource(R.string.label_email_required)) },
            isError = state.emailErr != null,
            supportingText = { state.emailErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.phone,
            onValueChange = vm::onPhoneChange,
            label = { Text(stringResource(R.string.label_phone_required)) },
            isError = state.phoneErr != null,
            supportingText = { state.phoneErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.firstname,
            onValueChange = vm::onFirstnameChange,
            label = { Text(stringResource(R.string.label_firstname_required)) },
            isError = state.firstnameErr != null,
            supportingText = { state.firstnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.lastname,
            onValueChange = vm::onLastnameChange,
            label = { Text(stringResource(R.string.label_lastname)) },
            isError = state.lastnameErr != null,
            supportingText = { state.lastnameErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = vm::onNewPasswordChange,
            label = { Text(stringResource(R.string.label_new_password)) },
            isError = state.passwordErr != null,
            supportingText = { state.passwordErr?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.section_company_view), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.inn,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_inn)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.companyName,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_company_name)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.form,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_form)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.kpp,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_kpp)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.ogrn,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_ogrn)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.okpo,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_okpo)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.companyAddress,
            onValueChange = { },
            label = { Text(stringResource(R.string.label_address)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.section_bank), style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.bank,
            onValueChange = vm::onBankChange,
            label = { Text(stringResource(R.string.label_bank)) },
            isError = state.bankErr != null,
            supportingText = { state.bankErr?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.bik,
            onValueChange = vm::onBikChange,
            label = { Text(stringResource(R.string.label_bik)) },
            isError = state.bikErr != null,
            supportingText = { state.bikErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.rs,
            onValueChange = vm::onRsChange,
            label = { Text(stringResource(R.string.label_rs)) },
            isError = state.rsErr != null,
            supportingText = { state.rsErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.ks,
            onValueChange = vm::onKsChange,
            label = { Text(stringResource(R.string.label_ks)) },
            isError = state.ksErr != null,
            supportingText = { state.ksErr?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.save() },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(
                    if (state.isSaving) R.string.btn_saving else R.string.btn_save_upper
                )
            )
        }

        state.resultMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_back))
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
        Text(
            stringResource(R.string.placeholder_under_construction),
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text(stringResource(R.string.btn_back))
        }
    }
}