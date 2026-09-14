@file:Suppress("DEPRECATION")
package com.kharcha.tracker.presentation.screens.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.model.BackupItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA)
            )
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModel.onSignInSuccess(account)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Auto-attempt silent sign in on start
    LaunchedEffect(Unit) {
        val client = GoogleSignIn.getClient(context, gso)
        client.silentSignIn().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                viewModel.onSignInSuccess(task.result)
            } else {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null && GoogleSignIn.hasPermissions(account, com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))) {
                    viewModel.onSignInSuccess(account)
                }
            }
        }
    }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); viewModel.clearMessages() }
        state.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); viewModel.clearMessages() }
    }

    // Delete confirmation dialog
    state.showDeleteDialog?.let { item ->
        val date = try {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
        } catch (e: Exception) { "Unknown Date" }

        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Backup?") },
            text = { Text("This will permanently delete the backup from $date. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteBackup(item) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud & Device Backup", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Google Drive Connection & Quick Action Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isSignedIn) TealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isSignedIn) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                    contentDescription = null,
                                    tint = if (state.isSignedIn) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isSignedIn) "Google Drive Synced" else "Google Drive",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.isSignedIn) (state.accountEmail ?: "Connected") else "Not connected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!state.isSignedIn) {
                            FilledTonalButton(
                                onClick = {
                                    val client = GoogleSignIn.getClient(context, gso)
                                    googleSignInLauncher.launch(client.signInIntent)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Connect", fontSize = 13.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val client = GoogleSignIn.getClient(context, gso)
                                    client.signOut().addOnCompleteListener { viewModel.onSignOut() }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Disconnect", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.backupNow(toCloud = state.isSignedIn) },
                            enabled = !state.isBackingUp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            if (state.isBackingUp) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(if (state.isSignedIn) "Backup to Cloud" else "Backup Now")
                        }

                        if (state.isSignedIn) {
                            OutlinedButton(
                                onClick = { viewModel.backupNow(toCloud = false) },
                                enabled = !state.isBackingUp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Local", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Header for Available Backups
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Backups",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.fetchBackups() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (state.backups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Storage, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No backups found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap 'Backup Now' to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.backups, key = { it.name }) { item ->
                        BackupCard(
                            item = item,
                            isRestoring = state.isRestoring,
                            isDeleting = state.isDeleting,
                            onRestore = { viewModel.restore(item) },
                            onDelete = { viewModel.showDeleteConfirmation(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BackupCard(
    item: BackupItem,
    isRestoring: Boolean,
    isDeleting: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val date = try {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
    } catch (e: Exception) { "Unknown Date" }

    val formattedSize = remember(item.sizeBytes) {
        when {
            item.sizeBytes <= 0 -> "< 1 KB"
            item.sizeBytes < 1024 -> "${item.sizeBytes} B"
            item.sizeBytes < 1024 * 1024 -> "${item.sizeBytes / 1024} KB"
            else -> String.format(Locale.getDefault(), "%.1f MB", item.sizeBytes / (1024.0 * 1024.0))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Storage badge (Cloud & Device vs Cloud vs Device)
                    val badgeColor = when (item.storageType) {
                        com.kharcha.core.model.BackupStorageType.SYNCED -> TealPrimary.copy(alpha = 0.15f)
                        com.kharcha.core.model.BackupStorageType.CLOUD_ONLY -> Color(0xFF4285F4).copy(alpha = 0.15f)
                        com.kharcha.core.model.BackupStorageType.LOCAL_ONLY -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val badgeTint = when (item.storageType) {
                        com.kharcha.core.model.BackupStorageType.SYNCED -> TealPrimary
                        com.kharcha.core.model.BackupStorageType.CLOUD_ONLY -> Color(0xFF4285F4)
                        com.kharcha.core.model.BackupStorageType.LOCAL_ONLY -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val badgeText = when (item.storageType) {
                        com.kharcha.core.model.BackupStorageType.SYNCED -> "Cloud & Device"
                        com.kharcha.core.model.BackupStorageType.CLOUD_ONLY -> "Cloud"
                        com.kharcha.core.model.BackupStorageType.LOCAL_ONLY -> "Device"
                    }
                    val badgeIcon = when (item.storageType) {
                        com.kharcha.core.model.BackupStorageType.SYNCED -> Icons.Rounded.CloudDone
                        com.kharcha.core.model.BackupStorageType.CLOUD_ONLY -> Icons.Rounded.Cloud
                        com.kharcha.core.model.BackupStorageType.LOCAL_ONLY -> Icons.Rounded.PhoneAndroid
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = badgeTint
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeTint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Size: $formattedSize • Kharcha SQLite DB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isRestoring || isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Rounded.Restore, contentDescription = "Restore", tint = TealPrimary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
