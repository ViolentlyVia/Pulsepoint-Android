package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.FMDAP.pulsepoint.viewmodel.ManageViewModel
import com.FMDAP.pulsepoint.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLoginScreen(
    vm: ManageViewModel,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val settingsVm: SettingsViewModel = viewModel()
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var prefilled    by remember { mutableStateOf(false) }

    // Pre-fill saved password once settings load
    LaunchedEffect(settingsVm.managePassword) {
        if (!prefilled && settingsVm.managePassword.isNotEmpty()) {
            password = settingsVm.managePassword
            prefilled = true
        }
    }

    LaunchedEffect(vm.isLoggedIn) {
        if (vm.isLoggedIn) onLoginSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Management Login", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Default.AdminPanelSettings, null,
                Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Enter management password", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                isError = vm.loginError != null,
                supportingText = vm.loginError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.login(password) },
                enabled = !vm.isLoggingIn && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (vm.isLoggingIn) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Login")
            }
        }
    }
}
