package com.kharcha.tracker.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.kharcha.experiences.dashboard.api.DashboardFeatureApi
import com.kharcha.experiences.history.api.HistoryFeatureApi
import com.kharcha.experiences.addtransaction.api.AddTransactionFeatureApi
import com.kharcha.tracker.presentation.screens.managecategories.ManageCategoriesScreen
import com.kharcha.tracker.presentation.screens.settings.SettingsScreen
import com.kharcha.tracker.presentation.screens.sms.SmsScanScreen
import com.kharcha.tracker.presentation.screens.split.addexpense.AddSplitExpenseScreen
import com.kharcha.tracker.presentation.screens.split.detail.GroupDetailScreen
import com.kharcha.tracker.presentation.screens.split.groups.GroupsListScreen
import com.kharcha.tracker.presentation.screens.stats.StatsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val STATS = "stats"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ADD_TRANSACTION = "add_transaction"
    const val EDIT_TRANSACTION = "add_transaction/{id}"
    const val SMS_SCAN = "sms_scan"
    
    // Splitwise Routes
    const val GROUPS = "groups"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val ADD_SPLIT_EXPENSE = "add_split_expense/{groupId}"
    
    const val MANAGE_CATEGORIES = "manage_categories"
    const val LOANS = "loans"
    const val ADD_LOAN = "add_loan?bankName={bankName}&emiAmount={emiAmount}"
    fun addLoanWithArgs(bankName: String = "", emiAmount: String = "") = "add_loan?bankName=$bankName&emiAmount=$emiAmount"
    const val RECURRING = "recurring"
    const val DEBT_SIMULATION = "debt_simulation"
    const val BACKUP = "backup"
    const val BUDGET = "budget"
    const val PRIVACY_POLICY = "privacy_policy"
    const val TERMS_CONDITIONS = "terms_conditions"
    const val ONBOARDING = "onboarding"

    fun editTransaction(id: Long) = "add_transaction/$id"
    fun groupDetail(groupId: Long) = "group_detail/$groupId"
    fun addSplitExpense(groupId: Long) = "add_split_expense/$groupId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
        startDestination: String = Routes.DASHBOARD,
    dashboardFeatureApi: DashboardFeatureApi,
    historyFeatureApi: HistoryFeatureApi,
    addTransactionFeatureApi: AddTransactionFeatureApi
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        addTransactionFeatureApi.registerGraph(
            navGraphBuilder = this,
            navController = navController
        )

        historyFeatureApi.registerGraph(
            navGraphBuilder = this,
            navController = navController,
            onNavigateToEditTransaction = { id -> navController.navigate(Routes.editTransaction(id)) }
        )

        dashboardFeatureApi.registerGraph(
            navGraphBuilder = this,
            navController = navController,
            onNavigateToAddTransaction = { navController.navigate(Routes.ADD_TRANSACTION) },
            onNavigateToEditTransaction = { id -> navController.navigate(Routes.editTransaction(id)) },
            onNavigateToAllTransactions = { navController.navigate(Routes.HISTORY) },
            onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
        )

        composable(Routes.STATS) {
            StatsScreen()
        }


        composable(Routes.SETTINGS) {
            SettingsScreen(
                isDarkMode = isDarkMode,
                onDarkModeToggle = onDarkModeToggle,
                onScanSmsClick = { navController.navigate(Routes.SMS_SCAN) },
                onManageCategoriesClick = { navController.navigate(Routes.MANAGE_CATEGORIES) },
                onBudgetClick = { navController.navigate(Routes.BUDGET) },
                onLoansClick = { navController.navigate(Routes.LOANS) },
                onSplitBillsClick = {
                    navController.navigate(Routes.GROUPS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onRecurringClick = { navController.navigate(Routes.RECURRING) },
                onBackupClick = { navController.navigate(Routes.BACKUP) },
                onPrivacyClick = { navController.navigate(Routes.PRIVACY_POLICY) },
                onTermsClick = { navController.navigate(Routes.TERMS_CONDITIONS) }
            )
        }
        
        composable(
            route = Routes.MANAGE_CATEGORIES,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(350)
                )
            }
        ) {
            ManageCategoriesScreen(onBack = { navController.popBackStack() })
        }



        composable(
            route = Routes.LOANS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.loans.LoansScreen(
                onBack = { navController.popBackStack() },
                onAddLoanClick = { navController.navigate(Routes.addLoanWithArgs()) },
                onSimulateClick = { navController.navigate(Routes.DEBT_SIMULATION) }
            )
        }

        composable(
            route = Routes.ADD_LOAN,
            arguments = listOf(
                navArgument("bankName") { type = NavType.StringType; defaultValue = "" },
                navArgument("emiAmount") { type = NavType.StringType; defaultValue = "" }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(350)) }
        ) { backStackEntry ->
            val bankName = backStackEntry.arguments?.getString("bankName") ?: ""
            val emiAmount = backStackEntry.arguments?.getString("emiAmount") ?: ""
            com.kharcha.tracker.presentation.screens.loans.AddLoanScreen(
                onBack = { navController.popBackStack() },
                prefillBankName = bankName,
                prefillEmiAmount = emiAmount
            )
        }

        composable(
            route = Routes.RECURRING,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.recurring.RecurringScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.DEBT_SIMULATION,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.simulation.DebtSimulationScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.BACKUP,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.settings.BackupScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.BUDGET,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.budget.BudgetScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.SMS_SCAN,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(350)
                )
            }
        ) {
            SmsScanScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddLoan = { bankName, emiAmount ->
                    navController.navigate(Routes.addLoanWithArgs(bankName, emiAmount))
                }
            )
        }
        
        // --- Splitwise Screens ---
        
        composable(Routes.GROUPS) {
            GroupsListScreen(
                onGroupClick = { groupId ->
                    navController.navigate(Routes.groupDetail(groupId))
                }
            )
        }
        
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(350)
                )
            }
        ) {
            GroupDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddExpenseClick = { groupId ->
                    navController.navigate(Routes.addSplitExpense(groupId))
                }
            )
        }
        
        composable(
            route = Routes.ADD_SPLIT_EXPENSE,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType }),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(350)
                )
            }
        ) {
            AddSplitExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }




        composable(
            route = Routes.PRIVACY_POLICY,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.legal.LegalScreen(
                title = "Privacy Policy",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TERMS_CONDITIONS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(350)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(350)) }
        ) {
            com.kharcha.tracker.presentation.screens.legal.LegalScreen(
                title = "Terms and Conditions",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ONBOARDING) {
            com.kharcha.tracker.presentation.screens.onboarding.OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
    }
}
