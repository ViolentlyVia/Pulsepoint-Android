package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.FMDAP.pulsepoint.ui.components.ServiceCard
import com.FMDAP.pulsepoint.viewmodel.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(vm: ServicesViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(Icons.Default.Refresh, "Force Refresh")
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
                state.data?.isEmpty() == true -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No services configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    val services = state.data ?: emptyList()
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val online  = services.count { it.online }
                        val offline = services.count { !it.online }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatChip("Online",  online,  true,  Modifier.weight(1f))
                                StatChip("Offline", offline, false, Modifier.weight(1f))
                            }
                        }
                        items(services) { svc -> ServiceCard(svc) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, count: Int, good: Boolean, modifier: Modifier = Modifier) {
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
