package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.FMDAP.pulsepoint.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToManage: () -> Unit, vm: SettingsViewModel = viewModel()) {
    var showPassword by remember { mutableStateOf(false) }
    val versionState = vm.versionState

    LaunchedEffect(Unit) { vm.fetchVersion() }

    if (vm.saveSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            vm.saveSuccess = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection section
            SectionCard(title = "Connection") {
                OutlinedTextField(
                    value = vm.serverUrl,
                    onValueChange = { vm.serverUrl = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("http://192.168.1.10:5000/") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Language, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.apiKey,
                    onValueChange = { vm.apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Key, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Management section
            SectionCard(title = "Management") {
                Text("Save your management password to auto-fill the login screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.managePassword,
                    onValueChange = { vm.managePassword = it },
                    label = { Text("Management Password (optional)") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Default.VisibilityOff
                                 else Icons.Default.Visibility, null)
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None
                                          else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onNavigateToManage, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open Management Console")
                }
            }

            // Save button
            Button(onClick = vm::save, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Settings")
            }

            if (vm.saveSuccess) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Settings saved!", Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Server info section
            SectionCard(title = "Server Info") {
                when {
                    versionState.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    versionState.error != null -> Text(versionState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                    versionState.data != null -> {
                        val v = versionState.data
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            InfoRow2("Server Version", v.version)
                            InfoRow2(".NET Version",   v.dotnet)
                            InfoRow2("PID",            v.pid.toString())
                            val upH = v.uptimeS / 3600; val upM = (v.uptimeS % 3600) / 60
                            InfoRow2("Server Uptime",  "${upH}h ${upM}m")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = vm::fetchVersion, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow2(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
