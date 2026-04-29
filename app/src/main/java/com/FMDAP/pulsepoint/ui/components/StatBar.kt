package com.FMDAP.pulsepoint.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.FMDAP.pulsepoint.ui.theme.statColor

@Composable
fun StatBar(label: String, value: Double?, modifier: Modifier = Modifier) {
    val color = statColor(value)
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (value != null) "%.1f%%".format(value) else "N/A",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { ((value ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.18f)
        )
    }
}
