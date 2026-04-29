package com.FMDAP.pulsepoint.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.FMDAP.pulsepoint.data.model.ServiceStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ServiceCard(service: ServiceStatus, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(service.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(service.url, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!service.online && service.offlineSince != null) {
                    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    Text("Offline since ${fmt.format(Date(service.offlineSince * 1000))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
                if (!service.online && service.error != null) {
                    Text(service.error, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge(service.online)
                if (service.online) {
                    service.statusCode?.let {
                        Text("HTTP $it", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    service.responseMs?.let {
                        Text("%.0f ms".format(it), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
