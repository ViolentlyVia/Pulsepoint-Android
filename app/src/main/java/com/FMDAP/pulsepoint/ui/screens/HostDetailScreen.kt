package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.FMDAP.pulsepoint.data.model.AssetUpdateRequest
import com.FMDAP.pulsepoint.ui.components.StatBar
import com.FMDAP.pulsepoint.ui.components.StatusBadge
import com.FMDAP.pulsepoint.viewmodel.HostDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDetailScreen(hostname: String, onBack: () -> Unit, vm: HostDetailViewModel = viewModel()) {
    val state     by vm.state.collectAsState()
    val pingState by vm.pingState.collectAsState()
    var showEdit  by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(hostname) { vm.load(hostname) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.data?.displayName ?: hostname, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.data == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.data == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { vm.load(hostname) }) { Text("Retry") }
                    }
                }
            }
            else -> {
                val host = state.data ?: return@Scaffold
                Column(
                    Modifier.padding(padding).padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status header
                    Card {
                        Row(Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(host.hostname, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Text(host.ip, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(host.isOnline)
                        }
                    }

                    // Stats
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Performance", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            StatBar("CPU",    host.cpu,    Modifier.fillMaxWidth())
                            StatBar("Memory", host.memory, Modifier.fillMaxWidth())
                            StatBar("Disk",   host.disk,   Modifier.fillMaxWidth())
                        }
                    }

                    // Info
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Details", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            InfoRow("Uptime", host.uptimeFormatted())
                            val lastSeenFmt = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())
                                .format(Date(host.lastSeen * 1000))
                            InfoRow("Last Seen", lastSeenFmt)
                            InfoRow("Sort Order", host.sortOrder.toString())
                            if (!host.rdpUrl.isNullOrBlank()) InfoRow("RDP URL", host.rdpUrl)
                            if (host.tagList().isNotEmpty()) InfoRow("Tags", host.tags ?: "")
                        }
                    }

                    // Ping
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Ping", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Button(
                                onClick = { vm.ping(hostname) },
                                enabled = !pingState.isLoading
                            ) {
                                if (pingState.isLoading) {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("Ping Host")
                            }
                            pingState.data?.let { result ->
                                Text(
                                    if (result.online) "Online — %.1f ms".format(result.pingMs ?: 0.0)
                                    else "No response",
                                    color = if (result.online) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.error
                                )
                            }
                            pingState.error?.let {
                                Text(it, color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Sort order controls
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Order", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { vm.moveUp(hostname) }) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Move Up")
                                }
                                OutlinedButton(onClick = { vm.moveDown(hostname) }) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Move Down")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit dialog
    if (showEdit) {
        val host = state.data ?: return
        EditAssetDialog(
            initial = host,
            onDismiss = { showEdit = false },
            onSave = { body ->
                vm.updateAsset(hostname, body) { showEdit = false }
            }
        )
    }

    // Delete confirmation
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete Host") },
            text = { Text("Remove ${state.data?.displayName ?: hostname} from the dashboard?") },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteAsset(hostname) { onBack() } },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EditAssetDialog(
    initial: com.FMDAP.pulsepoint.data.model.Host,
    onDismiss: () -> Unit,
    onSave: (AssetUpdateRequest) -> Unit
) {
    var friendlyName by remember { mutableStateOf(initial.friendlyName ?: "") }
    var ip           by remember { mutableStateOf(initial.ip) }
    var rdpUrl       by remember { mutableStateOf(initial.rdpUrl ?: "") }
    var tags         by remember { mutableStateOf(initial.tags ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${initial.hostname}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = friendlyName,
                    onValueChange = { friendlyName = it },
                    label = { Text("Friendly Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ip, onValueChange = { ip = it },
                    label = { Text("IP Address") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rdpUrl, onValueChange = { rdpUrl = it },
                    label = { Text("RDP URL") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tags, onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(AssetUpdateRequest(
                    friendlyName = friendlyName.takeIf { it.isNotBlank() },
                    ip = ip.takeIf { it.isNotBlank() },
                    rdpUrl = rdpUrl.takeIf { it.isNotBlank() },
                    tags = tags.takeIf { it.isNotBlank() }
                ))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
