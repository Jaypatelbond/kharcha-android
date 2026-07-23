package com.kharcha.experiences.addtransaction.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface AddTransactionFeatureApi {
    val addTransactionRoute: String
    fun editTransactionRoute(id: Long): String
    
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    )
}
