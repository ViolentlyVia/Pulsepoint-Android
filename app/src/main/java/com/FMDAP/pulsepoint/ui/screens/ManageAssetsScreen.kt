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
import com.FMDAP.pulsepoint.data.model.Host
import com.FMDAP.pulsepoint.ui.components.StatusBadge
import com.FMDAP.pulsepoint.viewmodel.ManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAssetsScreen(vm: ManageViewModel, onBack: () -> Unit) {
    val state by vm.assetsState.collectAsState()
    var editingHost by remember { mutableStateOf<Host?>(null) }

    LaunchedEffect(Unit) { vm.loadAssets() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Hosts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = vm::loadAssets) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
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
                        Button(onClick = vm::loadAssets) { Text("Retry") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(padding)
                ) {
                    items(state.data ?: emptyList(), key = { it.hostname }) { host ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(host.hostname, fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium)
                                        StatusBadge(host.isOnline)
                                    }
                                    if (!host.friendlyName.isNullOrBlank()) {
                                        Text("\"${host.friendlyName}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(host.ip, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { editingHost = host }) {
                                    Icon(Icons.Default.Edit, "Rename")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingHost?.let { host ->
        RenameDialog(
            host = host,
            onDismiss = { editingHost = null },
            onSave = { newName ->
                vm.renameAsset(host.hostname, newName)
                editingHost = null
            }
        )
    }
}

@Composable
private fun RenameDialog(host: Host, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(host.friendlyName ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename ${host.hostname}") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Friendly Name") },
                placeholder = { Text(host.hostname) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
