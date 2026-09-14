package com.kharcha.tracker.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.FolderSpecial
import com.kharcha.core.designsystem.components.ManageCollectionsDialog
import com.kharcha.core.designsystem.components.CollectionItemData
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Password
import com.kharcha.core.designsystem.components.SetPinDialog
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.common.util.DateUtils

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onScanSmsClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onLoansClick: () -> Unit,
    onSplitBillsClick: () -> Unit,
    onRecurringClick: () -> Unit,
    onBackupClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importCsv(it) }
    }

    var showAdOfferDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showManageCollectionsDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showSetPinDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showChangePinDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val adManager = androidx.compose.runtime.remember { com.kharcha.core.common.util.AdManager(context) }
    // Preload ad
    LaunchedEffect(Unit) {
        adManager.loadRewardedAd()
    }

    if (showAdOfferDialog) {
        com.kharcha.core.designsystem.components.AdFreeOfferDialog(
            onDismiss = { showAdOfferDialog = false },
            onWatchAd = {
                showAdOfferDialog = false
                val activity = context as? android.app.Activity
                if (activity != null) {
                    adManager.showRewardedAd(
                        activity = activity,
                        onUserEarnedReward = {
                            viewModel.grantAdFreeAccess()
                            android.widget.Toast.makeText(context, "Enjoy Ad-Free Access!", android.widget.Toast.LENGTH_LONG).show()
                        },
                        onAdDismissed = {
                             // Ad closed
                        }
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Monetization
        Text(
            text = "Monetization",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Star, // Or better icon
            title = "Remove Ads",
            subtitle = if (viewModel.isAdFree()) "Ad-Free until ${DateUtils.formatDateTime(state.adFreeExpiry)}" else "Watch a video to go Ad-Free for 24h",
            onClick = { showAdOfferDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // General
        Text(
            text = "General",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.FolderSpecial,
            title = "Manage Collections",
            subtitle = "Add, edit, or delete expense books & collections",
            onClick = { showManageCollectionsDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Category,
            title = "Manage Categories",
            subtitle = "Add, edit, or delete custom categories",
            onClick = onManageCategoriesClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Payments,
            title = "Monthly Budgeting",
            subtitle = "Set limits and track monthly spent progress",
            onClick = onBudgetClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.AccountBalance,
            title = "Loans & EMIs",
            subtitle = "Track loans, analyze debt strategy",
            onClick = onLoansClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Group,
            title = "Split Bills",
            subtitle = "Manage group expenses",
            onClick = onSplitBillsClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Repeat,
            title = "Recurring Transactions",
            subtitle = "Manage subscriptions and bills",
            onClick = onRecurringClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.CloudUpload,
            title = "Cloud Backup",
            subtitle = "Sync data with Google Drive",
            onClick = onBackupClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Appearance
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.DarkMode,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("Toggle dark/light theme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Security
        Text(
            text = "Security",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("App Lock", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("Require PIN or fingerprint to unlock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isAppLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            showSetPinDialog = true
                        } else {
                            viewModel.disableAppLock()
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        if (isAppLockEnabled) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Rounded.Password,
                title = "Change PIN",
                subtitle = "Update your 4-digit security PIN",
                onClick = { showChangePinDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Biometric Unlock", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Use fingerprint or face to unlock", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data
        Text(
            text = "Data",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Sms,
            title = "Scan SMS Transactions",
            subtitle = "Auto-detect expenses from bank SMS",
            onClick = onScanSmsClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.UploadFile,
            title = "Import from CSV",
            subtitle = "Import transactions from a CSV file",
            onClick = { importLauncher.launch("text/*") } // text/* or text/csv covers most CSV mime types
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.FileDownload,
            title = "Export to CSV",
            subtitle = "Download all transactions as a CSV file",
            onClick = { viewModel.exportToCsv() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.PictureAsPdf,
            title = "Export to PDF",
            subtitle = "Generate a formatted expense report",
            onClick = { viewModel.exportToPdf() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show export result
        state.exportMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = TealPrimary
            )
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearExportMessage()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About
        Text(
            text = "About",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsItem(
            icon = Icons.Rounded.Info,
            title = "Kharcha",
            subtitle = "Version 1.0.0 • Made with ❤️ in India",
            onClick = {}
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Legal
        Text(
            text = "Legal",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        SettingsItem(
            icon = Icons.Rounded.Description,
            title = "Privacy Policy",
            subtitle = "Data handling and user rights",
            onClick = onPrivacyClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SettingsItem(
            icon = Icons.Rounded.Gavel,
            title = "Terms & Conditions",
            subtitle = "Usage rules and disclaimers",
            onClick = onTermsClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showManageCollectionsDialog) {
        com.kharcha.core.designsystem.components.ManageCollectionsDialog(
            collections = collections.map {
                com.kharcha.core.designsystem.components.CollectionItemData(
                    name = it.name,
                    isDefault = it.isDefault
                )
            },
            onDismiss = { showManageCollectionsDialog = false },
            onCreateCollection = { viewModel.createCollection(it) },
            onRenameCollection = { old, new -> viewModel.renameCollection(old, new) },
            onDeleteCollection = { viewModel.deleteCollection(it) }
        )
    }

    if (showSetPinDialog) {
        SetPinDialog(
            isChangingPin = false,
            onDismiss = { showSetPinDialog = false },
            onPinConfirmed = { pin ->
                viewModel.enableAppLock(pin)
                showSetPinDialog = false
            }
        )
    }

    if (showChangePinDialog) {
        SetPinDialog(
            isChangingPin = true,
            onDismiss = { showChangePinDialog = false },
            onPinConfirmed = { pin ->
                viewModel.changePin(pin)
                showChangePinDialog = false
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
