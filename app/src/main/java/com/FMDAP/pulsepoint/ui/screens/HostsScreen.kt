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
import com.FMDAP.pulsepoint.ui.components.HostCard
import com.FMDAP.pulsepoint.viewmodel.HostsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(onHostClick: (String) -> Unit, vm: HostsViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hosts", fontWeight = FontWeight.Bold) },
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
                state.data?.isEmpty() == true -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hosts reporting in", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data ?: emptyList()) { host ->
                            HostCard(host, onClick = { onHostClick(host.hostname) })
                        }
                    }
                }
            }
        }
    }
}
