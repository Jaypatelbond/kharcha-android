package com.kharcha.tracker.util

import android.app.Activity
import android.content.IntentSender
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
 * Uses IMMEDIATE update type — when an update is available, a full-screen
 * UI blocks the app until the user updates. This ensures users always
 * run the latest version.
 *
 * Usage:
 *   1. Call [checkForUpdate] in onCreate
 *   2. Call [onResume] in onResume (handles interrupted updates)
 *   3. Call [onDestroy] in onDestroy (cleanup)
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
     * Check for available updates and start Immediate update flow if found.
     */
    fun checkForUpdate() {
        installStateListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    // For FLEXIBLE type — auto-complete the update
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

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            handleUpdateCheck(appUpdateInfo)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Update check failed: ${e.message}")
        }
    }

    private fun handleUpdateCheck(appUpdateInfo: AppUpdateInfo) {
        when (appUpdateInfo.updateAvailability()) {
            UpdateAvailability.UPDATE_AVAILABLE -> {
                Log.d(TAG, "Update available! Version code: ${appUpdateInfo.availableVersionCode()}")

                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startImmediateUpdate(appUpdateInfo)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    startFlexibleUpdate(appUpdateInfo)
                }
            }
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                // Resume an already in-progress update
                Log.d(TAG, "Update already in progress, resuming...")
                startImmediateUpdate(appUpdateInfo)
            }
            UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                Log.d(TAG, "No update available")
            }
            else -> {
                Log.d(TAG, "Update availability: ${appUpdateInfo.updateAvailability()}")
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

    /**
     * Call from Activity.onResume() to handle cases where the user
     * returned to the app without completing an immediate update.
     */
    fun onResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If an IMMEDIATE update was interrupted, restart it
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(appUpdateInfo)
            }

            // If a FLEXIBLE update was downloaded but not installed
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    /**
     * Call from Activity.onDestroy() to clean up listener.
     */
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
