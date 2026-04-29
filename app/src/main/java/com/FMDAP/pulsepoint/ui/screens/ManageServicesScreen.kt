package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.FMDAP.pulsepoint.viewmodel.ManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServicesScreen(
    vm: ManageViewModel,
    onNavigateAssets: () -> Unit,
    onNavigateIntegrations: () -> Unit,
    onBack: () -> Unit
) {
    val state by vm.servicesState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadServices() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Services", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onNavigateAssets) {
                        Icon(Icons.Default.Computer, "Manage Hosts")
                    }
                    IconButton(onClick = onNavigateIntegrations) {
                        Icon(Icons.Default.Hub, "Integration Settings")
                    }
                    IconButton(onClick = vm::logout) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Service")
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = vm::loadServices) { Text("Retry") }
                    }
                }
            }
            state.data?.isEmpty() == true -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No services configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { showAddDialog = true }) { Text("Add First Service") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(padding)
                ) {
                    items(state.data ?: emptyList(), key = { it.id }) { entry ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(entry.name, fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge)
                                    Text(entry.url, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { vm.deleteService(entry.id) }) {
                                    Icon(Icons.Default.Delete, "Delete",
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddServiceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, address ->
                vm.addService(name, address) { showAddDialog = false }
            }
        )
    }
}

@Composable
private fun AddServiceDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name    by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Service Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text("Address (IP:port or URL)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, address) },
                enabled = name.isNotBlank() && address.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
