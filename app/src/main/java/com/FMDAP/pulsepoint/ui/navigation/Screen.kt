package com.FMDAP.pulsepoint.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Dashboard           : Screen("dashboard")
    data object Hosts               : Screen("hosts")
    data object HostDetail          : Screen("host_detail/{hostname}") {
        fun route(hostname: String) = "host_detail/$hostname"
    }
    data object Services            : Screen("services")
    data object Settings            : Screen("settings")
    data object Integrations        : Screen("integrations")
    data object Unraid              : Screen("unraid")
    data object Idrac               : Screen("idrac")
    data object Omada               : Screen("omada")
    data object ManageLogin         : Screen("manage_login")
    data object ManageServices      : Screen("manage_services")
    data object ManageAssets        : Screen("manage_assets")
    data object ManageIntegrations  : Screen("manage_integrations")
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,    "Dashboard",    Icons.Default.Home),
    BottomNavItem(Screen.Hosts,        "Hosts",        Icons.Default.Computer),
    BottomNavItem(Screen.Services,     "Services",     Icons.Default.Dns),
    BottomNavItem(Screen.Integrations, "Integrations", Icons.Default.Hub),
    BottomNavItem(Screen.Settings,     "Settings",     Icons.Default.Settings),
)
