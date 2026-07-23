package com.kharcha.experiences.dashboard.api

interface DashboardFeatureApi {
    val route: String
    
    fun registerGraph(
        navGraphBuilder: androidx.navigation.NavGraphBuilder,
        navController: androidx.navigation.NavHostController,
        onNavigateToAddTransaction: () -> Unit,
        onNavigateToEditTransaction: (Long) -> Unit,
        onNavigateToAllTransactions: () -> Unit,
        onNavigateToSettings: () -> Unit
    )
}
