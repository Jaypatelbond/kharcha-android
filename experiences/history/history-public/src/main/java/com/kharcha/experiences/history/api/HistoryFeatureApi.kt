package com.kharcha.experiences.history.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface HistoryFeatureApi {
    val route: String

    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        onNavigateToEditTransaction: (Long) -> Unit
    )
}
