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
import com.FMDAP.pulsepoint.data.model.DockerContainer
import com.FMDAP.pulsepoint.data.model.UnraidArray
import com.FMDAP.pulsepoint.data.model.VmDomain
import com.FMDAP.pulsepoint.viewmodel.UnraidViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnraidScreen(onBack: () -> Unit, vm: UnraidViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Array", "Docker", "VMs", "Shares")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unraid", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
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
                                Text("Not connected to Unraid",
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
                                0 -> ArrayTab(snap.array, snap.disks + snap.parities)
                                1 -> DockerTab(snap.containers, vm::startContainer, vm::stopContainer, vm::restartContainer)
                                2 -> VmTab(snap.vms, vm::startVm, vm::stopVm, vm::restartVm)
                                3 -> SharesTab(snap.shares)
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
private fun ArrayTab(
    array: UnraidArray,
    disks: List<com.FMDAP.pulsepoint.data.model.DiskInfo>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Array Status", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        val stateColor = if (array.state.equals("STARTED", ignoreCase = true))
                            Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        Surface(color = stateColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small) {
                            Text(array.state.ifBlank { "Unknown" },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = stateColor,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    val totalGib = array.totalBytes / (1024.0 * 1024 * 1024)
                    val usedGib  = array.usedBytes  / (1024.0 * 1024 * 1024)
                    val freeGib  = array.freeBytes  / (1024.0 * 1024 * 1024)
                    if (array.totalBytes > 0) {
                        LinearProgressIndicator(
                            progress = { (array.usedBytes.toFloat() / array.totalBytes).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Used: ${"%.1f".format(usedGib)} GiB",
                            style = MaterialTheme.typography.bodySmall)
                        Text("Free: ${"%.1f".format(freeGib)} GiB",
                            style = MaterialTheme.typography.bodySmall)
                        Text("Total: ${"%.1f".format(totalGib)} GiB",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        items(disks) { disk ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Storage, null,
                        tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${disk.name} (${disk.type})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text(disk.device, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${disk.temp.toInt()}°C · ${disk.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${"%.1f".format(disk.size / (1024.0 * 1024 * 1024 * 1024))} TiB",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DockerTab(
    containers: List<DockerContainer>,
    onStart: (String) -> Unit,
    onStop: (String) -> Unit,
    onRestart: (String) -> Unit
) {
    if (containers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No containers", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(containers, key = { it.id }) { c ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val dotColor = if (c.running) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        Surface(color = dotColor, shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.size(8.dp)) {}
                        Text(c.names.trimStart('/'), fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                    }
                    Text(c.image, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(c.status, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!c.running) {
                            OutlinedButton(onClick = { onStart(c.id) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Start", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            OutlinedButton(onClick = { onStop(c.id) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.Stop, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Stop", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(onClick = { onRestart(c.id) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Restart", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VmTab(
    vms: List<VmDomain>,
    onStart: (String) -> Unit,
    onStop: (String) -> Unit,
    onRestart: (String) -> Unit
) {
    if (vms.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No VMs", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(vms, key = { it.name }) { vm ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val dotColor = if (vm.running) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        Surface(color = dotColor, shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.size(8.dp)) {}
                        Text(vm.name, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        Text(vm.state, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!vm.running) {
                            OutlinedButton(onClick = { onStart(vm.name) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Start", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            OutlinedButton(onClick = { onStop(vm.name) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.Stop, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Stop", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(onClick = { onRestart(vm.name) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Restart", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharesTab(shares: List<com.FMDAP.pulsepoint.data.model.ShareInfo>) {
    if (shares.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No shares", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(shares, key = { it.name }) { share ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(share.name, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                    if (share.sizeKb > 0) {
                        LinearProgressIndicator(
                            progress = { (share.usedKb.toFloat() / share.sizeKb).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Used: ${"%.1f".format(share.usedKb / 1048576.0)} GiB",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Free: ${"%.1f".format(share.freeKb / 1048576.0)} GiB",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
