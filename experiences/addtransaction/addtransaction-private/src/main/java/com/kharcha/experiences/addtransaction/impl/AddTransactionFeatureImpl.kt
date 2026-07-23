package com.kharcha.experiences.addtransaction.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kharcha.experiences.addtransaction.api.AddTransactionFeatureApi
import com.kharcha.experiences.addtransaction.impl.ui.AddTransactionScreen
import javax.inject.Inject

class AddTransactionFeatureImpl @Inject constructor() : AddTransactionFeatureApi {
    override val addTransactionRoute: String = "add_transaction"
    override fun editTransactionRoute(id: Long) = "add_transaction/\$id"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable(
            route = addTransactionRoute,
            enterTransition = {
                slideIntoContainer(
                    androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Up,
                    androidx.compose.animation.core.tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Down,
                    androidx.compose.animation.core.tween(350)
                )
            }
        ) {
            AddTransactionScreen(onBack = { navController.popBackStack() })
        }
        
        navGraphBuilder.composable(
            route = "add_transaction/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Up,
                    androidx.compose.animation.core.tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Down,
                    androidx.compose.animation.core.tween(350)
                )
            }
        ) {
            AddTransactionScreen(onBack = { navController.popBackStack() })
        }
    }
}
