package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.FMDAP.pulsepoint.data.model.OmadaClient
import com.FMDAP.pulsepoint.data.model.OmadaDevice
import com.FMDAP.pulsepoint.data.model.OmadaSite
import com.FMDAP.pulsepoint.viewmodel.OmadaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmadaScreen(onBack: () -> Unit, vm: OmadaViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Devices", "Clients")
    var showSiteMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Omada SDN", fontWeight = FontWeight.Bold)
                        state.data?.selectedSite?.name?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    val sites = state.data?.sites ?: emptyList()
                    if (sites.size > 1) {
                        Box {
                            IconButton(onClick = { showSiteMenu = true }) {
                                Icon(Icons.Default.Language, "Select Site")
                            }
                            DropdownMenu(
                                expanded = showSiteMenu,
                                onDismissRequest = { showSiteMenu = false }
                            ) {
                                sites.forEach { site ->
                                    DropdownMenuItem(
                                        text = { Text(site.name) },
                                        onClick = {
                                            vm.selectSite(site.siteId)
                                            vm.setPreferredSite(site.siteId)
                                            showSiteMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = vm::refresh) { Icon(Icons.Default.Refresh, "Refresh") }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = vm::refresh,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                state.error != null && state.data == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::refresh) { Text("Retry") }
                        }
                    }
                }
                state.data != null -> {
                    val snap = state.data!!
                    if (!snap.connected) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LinkOff, null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error)
                                Text("Not connected to Omada",
                                    style = MaterialTheme.typography.titleMedium)
                                snap.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                Button(onClick = vm::refresh) { Text("Retry") }
                            }
                        }
                    } else {
                        Column {
                            TabRow(selectedTabIndex = selectedTab) {
                                tabs.forEachIndexed { i, title ->
                                    Tab(selected = selectedTab == i,
                                        onClick = { selectedTab = i },
                                        text = { Text(title) })
                                }
                            }
                            when (selectedTab) {
                                0 -> DevicesTab(snap.devices)
                                1 -> ClientsTab(snap.clients)
                            }
                        }
                    }
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesTab(devices: List<OmadaDevice>) {
    if (devices.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No devices", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val onlineCount  = devices.count { it.online }
        val offlineCount = devices.count { !it.online }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryChip("Online", onlineCount, true, Modifier.weight(1f))
                SummaryChip("Offline", offlineCount, false, Modifier.weight(1f))
            }
        }
        items(devices, key = { it.mac }) { device ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val dotColor = if (device.online) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        Surface(color = dotColor, shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.size(8.dp)) {}
                        Text(device.name.ifBlank { device.mac }, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(device.type.uppercase(), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Text("${device.ip} · ${device.model}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (device.online) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Clients: ${device.clientCount}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Up: ${formatUptime(device.uptime)}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        if (device.download > 0 || device.upload > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("↓ ${formatBps(device.download)}",
                                    style = MaterialTheme.typography.bodySmall)
                                Text("↑ ${formatBps(device.upload)}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientsTab(clients: List<OmadaClient>) {
    if (clients.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No clients", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val wirelessCount = clients.count { it.wireless }
        val wiredCount    = clients.count { !it.wireless }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryChip("Wireless", wirelessCount, true, Modifier.weight(1f))
                SummaryChip("Wired", wiredCount, true, Modifier.weight(1f))
            }
        }
        items(clients, key = { it.mac }) { client ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (client.wireless) Icons.Default.Wifi else Icons.Default.Cable,
                            null, Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(client.name.ifBlank { client.mac }, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    Text("${client.ip} · ${client.networkName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (client.wireless && client.ssid.isNotBlank()) {
                        Text("SSID: ${client.ssid} · ${client.signalLevel} dBm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (client.trafficTotal > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("↓ ${formatBytes(client.trafficDown)}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("↑ ${formatBytes(client.trafficUp)}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (client.rxRate > 0 || client.txRate > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("↓ ${formatBps(client.rxRate)}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("↑ ${formatBps(client.txRate)}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Up: ${formatUptime(client.uptime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, count: Int, good: Boolean, modifier: Modifier = Modifier) {
    val color = if (good) MaterialTheme.colorScheme.primaryContainer
                else if (count > 0) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatUptime(seconds: Long): String {
    if (seconds <= 0) return "—"
    val d = seconds / 86400
    val h = (seconds % 86400) / 3600
    val m = (seconds % 3600) / 60
    return when {
        d > 0 -> "${d}d ${h}h"
        h > 0 -> "${h}h ${m}m"
        else  -> "${m}m"
    }
}

private fun formatBps(bps: Long): String = when {
    bps >= 1_000_000 -> "${"%.1f".format(bps / 1_000_000.0)} Mbps"
    bps >= 1_000     -> "${"%.0f".format(bps / 1_000.0)} Kbps"
    else             -> "$bps bps"
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> "${"%.1f".format(bytes / 1_073_741_824.0)} GB"
    bytes >= 1_048_576     -> "${"%.1f".format(bytes / 1_048_576.0)} MB"
    bytes >= 1_024         -> "${"%.0f".format(bytes / 1_024.0)} KB"
    else                   -> "$bytes B"
}
