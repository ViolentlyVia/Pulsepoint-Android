package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.FMDAP.pulsepoint.data.model.VersionInfo
import com.FMDAP.pulsepoint.ui.components.HostCard
import com.FMDAP.pulsepoint.ui.components.ServiceCard
import com.FMDAP.pulsepoint.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onHostClick: (String) -> Unit, vm: DashboardViewModel = viewModel()) {
    val state        by vm.state.collectAsState()
    val versionState by vm.versionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PulsePoint", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
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
                else -> {
                    val summary = state.data
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (summary != null) {
                            // Host / service count cards
                            item {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SummaryCard("Hosts",
                                        summary.hosts.online, summary.hosts.total,
                                        Modifier.weight(1f))
                                    SummaryCard("Services",
                                        summary.services.online, summary.services.total,
                                        Modifier.weight(1f))
                                }
                            }

                            // Server uptime card
                            item {
                                ServerUptimeCard(versionState.data)
                            }

                            item {
                                Text("Hosts", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold)
                            }
                            items(summary.hosts.list) { host ->
                                HostCard(host, onClick = { onHostClick(host.hostname) })
                            }
                            item {
                                Text("Services", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold)
                            }
                            items(summary.services.list) { svc ->
                                ServiceCard(svc)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerUptimeCard(version: VersionInfo?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Timer, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    "Dashboard Uptime",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (version != null) formatUptime(version.uptimeS) else "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (version != null) {
                Spacer(Modifier.weight(1f))
                Text(
                    "v${version.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatUptime(seconds: Long): String {
    val d = seconds / 86400
    val h = (seconds % 86400) / 3600
    val m = (seconds % 3600) / 60
    return when {
        d > 0L -> "${d}d ${h}h"
        h > 0L -> "${h}h ${m}m"
        else   -> "${m}m"
    }
}

@Composable
private fun SummaryCard(label: String, online: Int, total: Int, modifier: Modifier = Modifier) {
    val allOnline = online == total && total > 0
    val color = if (allOnline) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$online / $total", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("online", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
