package com.kharcha.experiences.dashboard.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.kharcha.experiences.dashboard.api.DashboardFeatureApi
import com.kharcha.experiences.dashboard.impl.ui.DashboardScreen
import javax.inject.Inject

class DashboardFeatureImpl @Inject constructor() : DashboardFeatureApi {
    override val route: String = "dashboard"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        onNavigateToAddTransaction: () -> Unit,
        onNavigateToEditTransaction: (Long) -> Unit,
        onNavigateToAllTransactions: () -> Unit,
        onNavigateToSettings: () -> Unit
    ) {
        navGraphBuilder.composable(route) {
            DashboardScreen(
                onNavigateToAddTransaction = onNavigateToAddTransaction,
                onNavigateToEditTransaction = onNavigateToEditTransaction,
                onNavigateToAllTransactions = onNavigateToAllTransactions,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}
