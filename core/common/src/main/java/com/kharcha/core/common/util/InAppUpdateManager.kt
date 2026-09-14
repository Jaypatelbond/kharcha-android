package com.kharcha.core.common.util

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Handles Google Play In-App Updates.
 *
 * Supports both automatic checks on app startup and manual checks from Settings.
 */
class InAppUpdateManager(private val activity: Activity) {

    companion object {
        private const val TAG = "InAppUpdate"
        const val UPDATE_REQUEST_CODE = 1001
    }

    private val appUpdateManager: AppUpdateManager =
        AppUpdateManagerFactory.create(activity)

    private var installStateListener: InstallStateUpdatedListener? = null

    /**
     * Check for available updates.
     * @param manual true when triggered by user tapping "Check for Updates"
     * @param onLatestVersion callback invoked when user is already on the latest version
     * @param onFailure callback invoked if the update check fails
     */
    fun checkForUpdate(
        manual: Boolean = false,
        onLatestVersion: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        if (installStateListener == null) {
            installStateListener = InstallStateUpdatedListener { state ->
                when (state.installStatus()) {
                    InstallStatus.DOWNLOADED -> {
                        Log.d(TAG, "Update downloaded, completing update...")
                        appUpdateManager.completeUpdate()
                    }
                    InstallStatus.INSTALLED -> {
                        Log.d(TAG, "Update installed!")
                        unregisterListener()
                    }
                    InstallStatus.FAILED -> {
                        Log.e(TAG, "Update failed with error code: ${state.installErrorCode()}")
                    }
                    else -> {
                        Log.d(TAG, "Install state: ${state.installStatus()}")
                    }
                }
            }
            appUpdateManager.registerListener(installStateListener!!)
        }

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    Log.d(TAG, "Update available! Version code: ${appUpdateInfo.availableVersionCode()}")
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        startFlexibleUpdate(appUpdateInfo)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        startImmediateUpdate(appUpdateInfo)
                    } else {
                        if (manual) openPlayStore()
                    }
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    Log.d(TAG, "Update already in progress, resuming...")
                    startImmediateUpdate(appUpdateInfo)
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    Log.d(TAG, "No update available")
                    if (manual) {
                        onLatestVersion?.invoke()
                    }
                }
                else -> {
                    Log.d(TAG, "Update availability: ${appUpdateInfo.updateAvailability()}")
                    if (manual) {
                        onLatestVersion?.invoke()
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Update check failed: ${e.message}")
            if (manual) {
                if (onFailure != null) {
                    onFailure(e)
                } else {
                    openPlayStore()
                }
            }
        }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                UPDATE_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Failed to start immediate update: ${e.message}")
        }
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                UPDATE_REQUEST_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Failed to start flexible update: ${e.message}")
        }
    }

    fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(appUpdateInfo)
            }
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    fun onDestroy() {
        unregisterListener()
    }

    private fun unregisterListener() {
        installStateListener?.let {
            appUpdateManager.unregisterListener(it)
            installStateListener = null
        }
    }
}
