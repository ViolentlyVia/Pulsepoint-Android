package com.FMDAP.pulsepoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.FMDAP.pulsepoint.viewmodel.ManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAppearanceScreen(vm: ManageViewModel, onBack: () -> Unit) {
    val state by vm.appearanceState.collectAsState()

    LaunchedEffect(Unit) { vm.loadAppearance() }

    var accentColor       by remember { mutableStateOf("#7c3aed") }
    var siteName          by remember { mutableStateOf("PulsePoint") }
    var cardColumns       by remember { mutableStateOf("auto") }
    var refreshInterval   by remember { mutableStateOf("15") }
    var onlineThreshold   by remember { mutableStateOf("120") }
    var hideServicesWidget by remember { mutableStateOf(false) }
    var navHiddenSet      by remember { mutableStateOf(emptySet<String>()) }
    var hiddenMetricsSet  by remember { mutableStateOf(emptySet<String>()) }

    val navHiddenOptions = listOf("assets", "services", "unraid", "idrac", "omada", "grow")
    val metricOptions    = listOf("cpu", "memory", "disk", "ping", "uptime")
    val columnOptions    = listOf("auto", "2", "3", "4", "5")

    LaunchedEffect(state.data) {
        state.data?.let { d ->
            accentColor        = d.accentColor
            siteName           = d.siteName
            cardColumns        = d.cardColumns
            refreshInterval    = d.refreshInterval.toString()
            onlineThreshold    = d.onlineThreshold.toString()
            hideServicesWidget = d.hideServicesWidget
            navHiddenSet       = d.navHidden.split(",").filter { it.isNotBlank() }.toSet()
            hiddenMetricsSet   = d.hiddenMetrics.split(",").filter { it.isNotBlank() }.toSet()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    vm.appearanceSaveError?.let {
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(it, modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    if (vm.appearanceSaveSuccess) {
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Text("Saved successfully", modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    AppearanceSectionHeader("General")
                    OutlinedTextField(value = siteName, onValueChange = { siteName = it },
                        label = { Text("Site Name") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = accentColor, onValueChange = { accentColor = it },
                        label = { Text("Accent Color (hex, e.g. #7c3aed)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)

                    HorizontalDivider()

                    AppearanceSectionHeader("Layout")
                    Text("Card Columns", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        columnOptions.forEach { opt ->
                            FilterChip(
                                selected = cardColumns == opt,
                                onClick = { cardColumns = opt },
                                label = { Text(opt) }
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hide Services Widget", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = hideServicesWidget,
                            onCheckedChange = { hideServicesWidget = it })
                    }

                    HorizontalDivider()

                    AppearanceSectionHeader("Timing")
                    OutlinedTextField(
                        value = refreshInterval,
                        onValueChange = { refreshInterval = it.filter { c -> c.isDigit() } },
                        label = { Text("Refresh Interval (seconds)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = onlineThreshold,
                        onValueChange = { onlineThreshold = it.filter { c -> c.isDigit() } },
                        label = { Text("Online Threshold (seconds)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        supportingText = { Text("Hosts not seen within this window are shown offline") }
                    )

                    HorizontalDivider()

                    AppearanceSectionHeader("Hidden Nav Pages")
                    navHiddenOptions.forEach { page ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(page.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium)
                            Checkbox(
                                checked = page in navHiddenSet,
                                onCheckedChange = { checked ->
                                    navHiddenSet = if (checked) navHiddenSet + page
                                                   else         navHiddenSet - page
                                }
                            )
                        }
                    }

                    HorizontalDivider()

                    AppearanceSectionHeader("Hidden Host Card Metrics")
                    metricOptions.forEach { metric ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(metric.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium)
                            Checkbox(
                                checked = metric in hiddenMetricsSet,
                                onCheckedChange = { checked ->
                                    hiddenMetricsSet = if (checked) hiddenMetricsSet + metric
                                                       else          hiddenMetricsSet - metric
                                }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            vm.clearAppearanceStatus()
                            vm.saveAppearance(
                                accentColor       = accentColor,
                                siteName          = siteName,
                                navHidden         = navHiddenSet.joinToString(","),
                                cardColumns       = cardColumns,
                                hiddenMetrics     = hiddenMetricsSet.joinToString(","),
                                refreshInterval   = refreshInterval.toIntOrNull() ?: 15,
                                onlineThreshold   = onlineThreshold.toIntOrNull() ?: 120,
                                hideServicesWidget = hideServicesWidget
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Appearance") }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AppearanceSectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
