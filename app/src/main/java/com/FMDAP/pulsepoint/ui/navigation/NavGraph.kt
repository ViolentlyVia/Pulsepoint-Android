package com.FMDAP.pulsepoint.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.FMDAP.pulsepoint.ui.screens.*
import com.FMDAP.pulsepoint.viewmodel.ManageViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val manageVm: ManageViewModel = viewModel()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDest = navBackStackEntry?.destination
            val showBar = bottomNavItems.any { it.screen.route == currentDest?.route }
            if (showBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDest?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(onHostClick = { navController.navigate(Screen.HostDetail.route(it)) })
            }
            composable(Screen.Hosts.route) {
                HostsScreen(onHostClick = { navController.navigate(Screen.HostDetail.route(it)) })
            }
            composable(Screen.HostDetail.route) { back ->
                val hostname = back.arguments?.getString("hostname") ?: return@composable
                HostDetailScreen(hostname = hostname, onBack = { navController.popBackStack() })
            }
            composable(Screen.Services.route) {
                ServicesScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToManage = { navController.navigate(Screen.ManageLogin.route) }
                )
            }
            composable(Screen.ManageLogin.route) {
                ManageLoginScreen(
                    vm = manageVm,
                    onLoginSuccess = {
                        navController.navigate(Screen.ManageServices.route) {
                            popUpTo(Screen.ManageLogin.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ManageServices.route) {
                ManageServicesScreen(
                    vm = manageVm,
                    onNavigateAssets = { navController.navigate(Screen.ManageAssets.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ManageAssets.route) {
                ManageAssetsScreen(
                    vm = manageVm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
