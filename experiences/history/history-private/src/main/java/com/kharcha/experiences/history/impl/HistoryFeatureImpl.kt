package com.kharcha.experiences.history.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.kharcha.experiences.history.api.HistoryFeatureApi
import com.kharcha.experiences.history.impl.ui.HistoryScreen
import javax.inject.Inject

class HistoryFeatureImpl @Inject constructor() : HistoryFeatureApi {
    override val route: String = "all_transactions"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        onNavigateToEditTransaction: (Long) -> Unit
    ) {
        navGraphBuilder.composable(route) {
            HistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditTransaction = onNavigateToEditTransaction
            )
        }
    }
}
