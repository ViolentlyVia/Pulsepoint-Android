package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsScreen(
    onNavigateUnraid: () -> Unit,
    onNavigateIdrac: () -> Unit,
    onNavigateOmada: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Integrations", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                IntegrationTile(
                    icon = Icons.Default.Storage,
                    title = "Unraid",
                    subtitle = "Array, disks, Docker containers, VMs, and shares",
                    onClick = onNavigateUnraid
                )
            }
            item {
                IntegrationTile(
                    icon = Icons.Default.Memory,
                    title = "iDRAC",
                    subtitle = "Dell server hardware, thermals, fans, PSUs, and drives",
                    onClick = onNavigateIdrac
                )
            }
            item {
                IntegrationTile(
                    icon = Icons.Default.Router,
                    title = "Omada SDN",
                    subtitle = "TP-Link Omada sites, devices, and connected clients",
                    onClick = onNavigateOmada
                )
            }
        }
    }
}

@Composable
private fun IntegrationTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
