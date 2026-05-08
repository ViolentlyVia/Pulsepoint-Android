package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.FMDAP.pulsepoint.viewmodel.ManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageIntegrationsScreen(vm: ManageViewModel, onBack: () -> Unit) {
    val intState    by vm.integrationsState.collectAsState()
    val omadaState  by vm.omadaSettingsState.collectAsState()
    val growState   by vm.growSettingsState.collectAsState()

    LaunchedEffect(Unit) { vm.loadIntegrations() }

    // Unraid form state
    var unraidHost        by remember { mutableStateOf("") }
    var unraidApiKey      by remember { mutableStateOf("") }
    var unraidApiKeyId    by remember { mutableStateOf("") }
    var unraidBearer      by remember { mutableStateOf("") }

    // iDRAC form state
    var idracHost         by remember { mutableStateOf("") }
    var idracUsername     by remember { mutableStateOf("") }
    var idracPassword     by remember { mutableStateOf("") }
    var idracPassVisible  by remember { mutableStateOf(false) }

    // Omada form state
    var omadaBaseUrl      by remember { mutableStateOf("") }
    var omadaOmadacId     by remember { mutableStateOf("") }
    var omadaClientId     by remember { mutableStateOf("") }
    var omadaSecret       by remember { mutableStateOf("") }
    var omadaSecretVisible by remember { mutableStateOf(false) }
    var omadaSiteId       by remember { mutableStateOf("") }

    // Grow form state
    var growUrl     by remember { mutableStateOf("") }
    var growRtspUrl by remember { mutableStateOf("") }
    var growHlsUrl  by remember { mutableStateOf("") }

    // Populate fields once data loads
    LaunchedEffect(intState.data) {
        intState.data?.let { d ->
            unraidHost     = d.unraid.host
            unraidApiKey   = d.unraid.apiKey
            unraidApiKeyId = d.unraid.apiKeyId
            unraidBearer   = d.unraid.bearerToken
            idracHost      = d.idrac.host
            idracUsername  = d.idrac.username
        }
    }
    LaunchedEffect(omadaState.data) {
        omadaState.data?.let { d ->
            omadaBaseUrl  = d.baseUrl
            omadaOmadacId = d.omadacId
            omadaClientId = d.clientId
            omadaSiteId   = d.preferSiteId
        }
    }
    LaunchedEffect(growState.data) {
        growState.data?.let { d ->
            growUrl     = d.url
            growRtspUrl = d.rtspUrl
            growHlsUrl  = d.hlsUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Integration Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            vm.integrationSaveError?.let {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            if (vm.integrationSaveSuccess) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Saved successfully", modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // --- Unraid ---
            SectionHeader("Unraid")
            OutlinedTextField(value = unraidHost, onValueChange = { unraidHost = it },
                label = { Text("Host (IP or hostname)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = unraidApiKey, onValueChange = { unraidApiKey = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = unraidApiKeyId, onValueChange = { unraidApiKeyId = it },
                label = { Text("API Key ID (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = unraidBearer, onValueChange = { unraidBearer = it },
                label = { Text("Bearer Token (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(
                onClick = {
                    vm.clearIntegrationStatus()
                    vm.saveUnraid(unraidHost, unraidApiKey, unraidApiKeyId, unraidBearer)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Unraid") }

            HorizontalDivider()

            // --- iDRAC ---
            SectionHeader("iDRAC 8")
            OutlinedTextField(value = idracHost, onValueChange = { idracHost = it },
                label = { Text("Host (IP or hostname)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = idracUsername, onValueChange = { idracUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(
                value = idracPassword,
                onValueChange = { idracPassword = it },
                label = {
                    val hasStored = intState.data?.idrac?.hasPassword == true
                    Text(if (hasStored && idracPassword.isEmpty()) "Password (leave blank to keep)" else "Password")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (idracPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { idracPassVisible = !idracPassVisible }) {
                        Icon(if (idracPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                }
            )
            Button(
                onClick = {
                    vm.clearIntegrationStatus()
                    vm.saveIdrac(idracHost, idracUsername, idracPassword)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save iDRAC") }

            HorizontalDivider()

            // --- Omada ---
            SectionHeader("Omada SDN")
            OutlinedTextField(value = omadaBaseUrl, onValueChange = { omadaBaseUrl = it },
                label = { Text("Base URL (e.g. https://192.168.1.10:8043)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = omadaOmadacId, onValueChange = { omadaOmadacId = it },
                label = { Text("Omada Controller ID") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = omadaClientId, onValueChange = { omadaClientId = it },
                label = { Text("OAuth Client ID") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(
                value = omadaSecret,
                onValueChange = { omadaSecret = it },
                label = {
                    val hasStored = omadaState.data?.hasSecret == true
                    Text(if (hasStored && omadaSecret.isEmpty()) "Client Secret (leave blank to keep)" else "Client Secret")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (omadaSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { omadaSecretVisible = !omadaSecretVisible }) {
                        Icon(if (omadaSecretVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                    }
                }
            )
            OutlinedTextField(value = omadaSiteId, onValueChange = { omadaSiteId = it },
                label = { Text("Preferred Site ID (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(
                onClick = {
                    vm.clearIntegrationStatus()
                    vm.saveOmada(omadaBaseUrl, omadaOmadacId, omadaClientId, omadaSecret, omadaSiteId)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Omada") }

            HorizontalDivider()

            // --- Grow ---
            SectionHeader("Grow Device")
            OutlinedTextField(value = growUrl, onValueChange = { growUrl = it },
                label = { Text("Device URL (e.g. http://192.168.1.x)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = growRtspUrl, onValueChange = { growRtspUrl = it },
                label = { Text("RTSP Stream URL (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = growHlsUrl, onValueChange = { growHlsUrl = it },
                label = { Text("HLS Stream URL (optional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                supportingText = { Text("If blank, auto-derived from RTSP URL") })
            Button(
                onClick = {
                    vm.clearIntegrationStatus()
                    vm.saveGrow(growUrl, growRtspUrl, growHlsUrl)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Grow") }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
