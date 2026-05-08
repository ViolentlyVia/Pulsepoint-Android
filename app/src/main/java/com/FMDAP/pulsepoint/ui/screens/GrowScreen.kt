package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.FMDAP.pulsepoint.data.model.GrowData
import com.FMDAP.pulsepoint.viewmodel.GrowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowScreen(onBack: () -> Unit, vm: GrowViewModel = viewModel()) {
    val state      by vm.statusState.collectAsState()
    val cameraUrl  by vm.cameraUrl.collectAsState()
    val rtspUrl    by vm.rtspUrl.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Monitor", "History", "Camera")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grow", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = vm::load) { Icon(Icons.Default.Refresh, "Refresh") }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = vm::load,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            when {
                state.isLoading && state.data == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null && state.data == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = vm::load) { Text("Retry") }
                        }
                    }
                }
                state.data != null -> {
                    val resp = state.data!!
                    when {
                        !resp.configured -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(Icons.Default.Warning, null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Grow device not configured",
                                        style = MaterialTheme.typography.titleMedium)
                                    Text("Configure it in Manage → Integration Settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        !resp.connected -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.LinkOff, null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.error)
                                    Text("Not connected to Grow device",
                                        style = MaterialTheme.typography.titleMedium)
                                    resp.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                    Button(onClick = vm::load) { Text("Retry") }
                                }
                            }
                        }
                        else -> {
                            val data = resp.data!!
                            Column {
                                vm.actionError?.let { err ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        Row(Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(err, color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.weight(1f))
                                            IconButton(onClick = vm::clearStatus) {
                                                Icon(Icons.Default.Close, null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer)
                                            }
                                        }
                                    }
                                }
                                vm.actionSuccess?.let { msg ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        Row(Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(msg, color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.weight(1f))
                                            IconButton(onClick = vm::clearStatus) {
                                                Icon(Icons.Default.Close, null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    }
                                }
                                TabRow(selectedTabIndex = selectedTab) {
                                    tabs.forEachIndexed { i, title ->
                                        Tab(selected = selectedTab == i,
                                            onClick = { selectedTab = i },
                                            text = { Text(title) })
                                    }
                                }
                                when (selectedTab) {
                                    0 -> MonitorTab(data, vm)
                                    1 -> HistoryTab(data)
                                    2 -> CameraTab(cameraUrl, rtspUrl, vm.streamLoadAttempted)
                                }
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
private fun MonitorTab(data: GrowData, vm: GrowViewModel) {
    var thresholdText by remember(data.threshold) { mutableStateOf(data.threshold.toString()) }
    var pumpDurText   by remember(data.pumpDurationS) { mutableStateOf(data.pumpDurationS.toString()) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GrowSensorCard("Moisture", "${data.moisture}%",   Icons.Default.Opacity,     Modifier.weight(1f))
            GrowSensorCard("Temp",    "${"%.1f".format(data.temperature)}°C", Icons.Default.Thermostat, Modifier.weight(1f))
            GrowSensorCard("Humidity","${data.humidity}%",   Icons.Default.WaterDrop,   Modifier.weight(1f))
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pumpColor = if (data.pumpOn) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(Icons.Default.Water, null, tint = pumpColor)
                    Text("Pump", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Surface(
                        color = if (data.pumpOn) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            if (data.pumpOn) "RUNNING" else "IDLE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (data.pumpOn) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.controlPump(true) },
                        modifier = Modifier.weight(1f), enabled = !data.pumpOn) {
                        Text("Start Pump")
                    }
                    OutlinedButton(onClick = { vm.controlPump(false) },
                        modifier = Modifier.weight(1f), enabled = data.pumpOn) {
                        Text("Stop Pump")
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                    label = { Text("Moisture Threshold (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Pump activates when moisture drops below this level") }
                )
                OutlinedTextField(
                    value = pumpDurText,
                    onValueChange = { pumpDurText = it.filter { c -> c.isDigit() } },
                    label = { Text("Pump Duration (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val t = thresholdText.toIntOrNull() ?: return@Button
                        val d = pumpDurText.toIntOrNull() ?: return@Button
                        vm.saveSettings(t, d)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Settings") }
                OutlinedButton(
                    onClick = vm::clearHistory,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear Sensor History") }
            }
        }
    }
}

@Composable
private fun GrowSensorCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class HistoryMetric(val label: String) {
    Moisture("Moisture"), Temp("Temp"), Humidity("Humidity")
}
private enum class TimeRange(val label: String) {
    H12("12h"), D1("1d"), W1("1w")
}

@Composable
private fun HistoryTab(data: GrowData) {
    var metric by remember { mutableStateOf(HistoryMetric.Moisture) }
    var range  by remember { mutableStateOf(TimeRange.H12) }

    val series = when (metric) {
        HistoryMetric.Moisture -> when (range) {
            TimeRange.H12 -> data.histMoisture12h
            TimeRange.D1  -> data.histMoisture1d
            TimeRange.W1  -> data.histMoisture1w
        }
        HistoryMetric.Temp -> when (range) {
            TimeRange.H12 -> data.histTemp12h
            TimeRange.D1  -> data.histTemp1d
            TimeRange.W1  -> data.histTemp1w
        }
        HistoryMetric.Humidity -> when (range) {
            TimeRange.H12 -> data.histHum12h
            TimeRange.D1  -> data.histHum1d
            TimeRange.W1  -> data.histHum1w
        }
    }

    val chartColor = when (metric) {
        HistoryMetric.Moisture -> Color(0xFF2196F3)
        HistoryMetric.Temp     -> Color(0xFFFF5722)
        HistoryMetric.Humidity -> Color(0xFF4CAF50)
    }
    val unit = when (metric) {
        HistoryMetric.Moisture -> "%"
        HistoryMetric.Temp     -> "°C"
        HistoryMetric.Humidity -> "%"
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryMetric.entries.forEach { m ->
                FilterChip(
                    selected = metric == m,
                    onClick = { metric = m },
                    label = { Text(m.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeRange.entries.forEach { r ->
                FilterChip(
                    selected = range == r,
                    onClick = { range = r },
                    label = { Text(r.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            if (series.size < 2) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text("No history data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val minVal = series.min()
                val maxVal = series.max()
                val avgVal = series.average().toFloat()
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth().height(160.dp)) {
                        Column(
                            modifier = Modifier.width(36.dp).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("%.0f".format(maxVal), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.0f".format(avgVal), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.0f".format(minVal), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        GrowLineChart(
                            data = series,
                            lineColor = chartColor,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        GrowStatLabel("Min", "%.1f".format(minVal) + unit)
                        GrowStatLabel("Avg", "%.1f".format(avgVal) + unit)
                        GrowStatLabel("Max", "%.1f".format(maxVal) + unit)
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowStatLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GrowLineChart(data: List<Float>, lineColor: Color, modifier: Modifier = Modifier) {
    val fillColor = lineColor.copy(alpha = 0.15f)
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val min   = data.min()
        val max   = data.max().coerceAtLeast(min + 0.01f)
        val range = max - min

        fun xOf(i: Int) = i / (data.size - 1f) * size.width
        fun yOf(v: Float) = (1f - (v - min) / range) * size.height

        val fillPath = Path()
        fillPath.moveTo(xOf(0), size.height)
        data.forEachIndexed { i, v -> fillPath.lineTo(xOf(i), yOf(v)) }
        fillPath.lineTo(xOf(data.size - 1), size.height)
        fillPath.close()
        drawPath(fillPath, color = fillColor)

        val linePath = Path()
        data.forEachIndexed { i, v ->
            if (i == 0) linePath.moveTo(xOf(i), yOf(v))
            else        linePath.lineTo(xOf(i), yOf(v))
        }
        drawPath(linePath, color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        drawCircle(color = lineColor, radius = 4.dp.toPx(),
            center = Offset(xOf(data.size - 1), yOf(data.last())))
    }
}

@Composable
private fun CameraTab(cameraUrl: String?, rtspUrl: String?, streamLoadAttempted: Boolean) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        when {
            !streamLoadAttempted -> {
                CircularProgressIndicator()
            }
            cameraUrl != null -> {
                HlsPlayerView(
                    hlsUrl = cameraUrl,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                )
            }
            rtspUrl != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.Videocam, null, modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Text("RTSP Stream", style = MaterialTheme.typography.titleMedium)
                    Text(rtspUrl, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "An HLS proxy URL could not be determined. Configure an HLS URL in Manage → Integration Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VideocamOff, null, modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No stream configured", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Configure the Grow device URL in Manage → Integration Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun HlsPlayerView(hlsUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player  = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(hlsUrl))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = true
            }
        },
        modifier = modifier
    )
}
