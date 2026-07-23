package com.kharcha.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kharcha.tracker.presentation.navigation.NavGraph
import com.kharcha.tracker.presentation.navigation.Routes
import com.kharcha.core.designsystem.theme.KharchaTheme
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.common.util.InAppUpdateManager
import com.kharcha.experiences.dashboard.api.DashboardFeatureApi
import com.kharcha.experiences.history.api.HistoryFeatureApi
import com.kharcha.experiences.addtransaction.api.AddTransactionFeatureApi
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dashboardFeatureApi: DashboardFeatureApi

    @Inject
    lateinit var historyFeatureApi: HistoryFeatureApi

    @Inject
    lateinit var addTransactionFeatureApi: AddTransactionFeatureApi

    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.kharcha.tracker.util.NotificationHelper.createNotificationChannel(this)

        // In-App Update: check for updates on every launch
        inAppUpdateManager = InAppUpdateManager(this)
        inAppUpdateManager.checkForUpdate()

        enableEdgeToEdge()
        setContent {
            KharchaAppContent(dashboardFeatureApi = dashboardFeatureApi,
                    historyFeatureApi = historyFeatureApi,
                    addTransactionFeatureApi = addTransactionFeatureApi)
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume interrupted updates (e.g., user pressed back during update)
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onDestroy()
        }
    }
}

@Composable
fun KharchaAppContent(
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    dashboardFeatureApi: DashboardFeatureApi,
    historyFeatureApi: HistoryFeatureApi,
    addTransactionFeatureApi: AddTransactionFeatureApi
) {
    val systemDark = isSystemInDarkTheme()
    var isDarkMode by rememberSaveable { mutableStateOf(systemDark) }
    
    LaunchedEffect(systemDark) {
        isDarkMode = systemDark
    }

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState(initial = null)

    if (isOnboardingCompleted == null) {
        // Splash / Loading
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
             // Optional: Add App Logo or Spinner
        }
    } else {
        val startDest = if (isOnboardingCompleted == true) dashboardFeatureApi.route else Routes.ONBOARDING
        val navController = rememberNavController()
    
        val navItems = listOf(
            BottomNavItem("Home", Icons.Rounded.Home, dashboardFeatureApi.route),
            BottomNavItem("Stats", Icons.Rounded.BarChart, Routes.STATS),
            BottomNavItem("History", Icons.Rounded.History, historyFeatureApi.route),
            BottomNavItem("Split", Icons.Rounded.Groups, Routes.GROUPS),
            BottomNavItem("Settings", Icons.Rounded.Settings, Routes.SETTINGS)
        )
    
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
    
        // Hide bottom bar on Onboarding too
        val showBottomBar = currentRoute in navItems.map { it.route } && currentRoute != Routes.ONBOARDING
    
        KharchaTheme(darkTheme = isDarkMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.navigationBars,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                navItems.forEach { item ->
                                    val isSelected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                item.icon,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = TealPrimary,
                                            selectedTextColor = TealPrimary,
                                            indicatorColor = TealPrimary.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (showBottomBar && currentRoute != Routes.GROUPS) {
                            FloatingActionButton(
                                onClick = { navController.navigate(addTransactionFeatureApi.addTransactionRoute) },
                                containerColor = TealPrimary,
                                contentColor = MaterialTheme.colorScheme.background,
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add Transaction",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(
                            navController = navController,
                            isDarkMode = isDarkMode,
                            onDarkModeToggle = { isDarkMode = it },
                            startDestination = startDest,
                            dashboardFeatureApi = dashboardFeatureApi,
                    historyFeatureApi = historyFeatureApi,
                    addTransactionFeatureApi = addTransactionFeatureApi
                        )
                    }
                }
            }
        }
    }
}
