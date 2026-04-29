package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.clickable
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
import com.FMDAP.pulsepoint.data.model.IdracSnapshot
import com.FMDAP.pulsepoint.data.model.StorageDrive
import com.FMDAP.pulsepoint.viewmodel.IdracViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdracScreen(onBack: () -> Unit, vm: IdracViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("System", "Thermal", "Power", "Storage")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("iDRAC", fontWeight = FontWeight.Bold) },
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
                                Text("Not connected to iDRAC",
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
                                0 -> SystemTab(snap)
                                1 -> ThermalTab(snap)
                                2 -> PowerTab(snap)
                                3 -> StorageTab(snap)
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
private fun SystemTab(snap: IdracSnapshot) {
    val sys = snap.system
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("System", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    InfoRow("Model", "${sys.manufacturer} ${sys.model}")
                    InfoRow("Service Tag", sys.serviceTag)
                    InfoRow("BIOS", sys.biosVersion)
                    InfoRow("iDRAC Firmware", sys.idracFirmware)
                    InfoRow("Power State", sys.powerState)
                    val healthColor = if (sys.healthStatus.equals("OK", ignoreCase = true))
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Health", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sys.healthStatus, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold, color = healthColor)
                    }
                    InfoRow("CPUs", sys.processorCount.toString())
                    InfoRow("Memory", "${"%.0f".format(sys.totalMemoryGiB)} GiB")
                }
            }
        }
    }
}

@Composable
private fun ThermalTab(snap: IdracSnapshot) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (snap.temperatures.isNotEmpty()) {
            item {
                Text("Temperatures", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp))
            }
            items(snap.temperatures) { sensor ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sensor.name, style = MaterialTheme.typography.bodyMedium)
                            val critical = sensor.upperThresholdCritical
                            if (critical != null) {
                                Text("Critical: ${critical.toInt()}°C",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        val tempColor = when {
                            sensor.upperThresholdCritical != null &&
                                    sensor.readingCelsius >= sensor.upperThresholdCritical ->
                                MaterialTheme.colorScheme.error
                            sensor.readingCelsius >= 70 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Text("${sensor.readingCelsius.toInt()}°C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = tempColor)
                    }
                }
            }
        }
        if (snap.fans.isNotEmpty()) {
            item {
                Text("Fans", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(snap.fans) { fan ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(fan.name, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        Text("${fan.rpm} RPM", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (snap.temperatures.isEmpty() && snap.fans.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No thermal data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PowerTab(snap: IdracSnapshot) {
    if (snap.powerSupplies.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No power supply data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(snap.powerSupplies) { psu ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(psu.name, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                    if (psu.model.isNotBlank()) {
                        Text(psu.model, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        psu.lastOutputWatts?.let {
                            Text("Output: ${"%.0f".format(it)} W",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        psu.powerCapacityWatts?.let {
                            Text("Capacity: ${"%.0f".format(it)} W",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    val statusColor = if (psu.status.equals("OK", ignoreCase = true))
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    Text(psu.status, style = MaterialTheme.typography.bodySmall,
                        color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StorageTab(snap: IdracSnapshot) {
    var selectedDrive by remember { mutableStateOf<StorageDrive?>(null) }

    if (snap.drives.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No drives found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(snap.drives, key = { it.serialNumber.ifBlank { it.name } }) { drive ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { selectedDrive = drive }
            ) {
                Column(modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(drive.name, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium)
                    Text("${drive.manufacturer} ${drive.model}".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${drive.mediaType} · ${drive.protocol}",
                            style = MaterialTheme.typography.bodySmall)
                        Text("${"%.1f".format(drive.capacityBytes / (1024.0 * 1024 * 1024 * 1024))} TiB",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    val healthColor = if (drive.health.equals("OK", ignoreCase = true))
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    Text("${drive.health} · ${drive.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = healthColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    selectedDrive?.let { drive ->
        DriveDetailDialog(drive = drive, onDismiss = { selectedDrive = null })
    }
}

@Composable
private fun DriveDetailDialog(drive: StorageDrive, onDismiss: () -> Unit) {
    val healthColor = if (drive.health.equals("OK", ignoreCase = true))
        Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(drive.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Manufacturer", drive.manufacturer)
                InfoRow("Model", drive.model)
                InfoRow("Serial Number", drive.serialNumber)
                InfoRow("Media Type", drive.mediaType)
                InfoRow("Protocol", drive.protocol)
                InfoRow("Capacity", "${"%.2f".format(drive.capacityBytes / (1024.0 * 1024 * 1024 * 1024))} TiB")
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Health", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(drive.health.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold, color = healthColor)
                }
                InfoRow("State", drive.state)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold)
    }
}
