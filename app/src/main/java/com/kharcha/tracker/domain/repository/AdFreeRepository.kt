package com.kharcha.tracker.domain.repository

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class AdFreeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val AD_FREE_EXPIRY_KEY = longPreferencesKey("ad_free_expiry")
        private val ONBOARDING_COMPLETED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
    }

    val adFreeExpiry: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[AD_FREE_EXPIRY_KEY] ?: 0L
    }

    val isAdFree: Flow<Boolean> = adFreeExpiry.map { expiry ->
        System.currentTimeMillis() < expiry
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    suspend fun grantAdFreeAccess() {
        context.dataStore.edit { prefs: MutablePreferences ->
            val currentExpiry = prefs[AD_FREE_EXPIRY_KEY] ?: 0L
            val now = System.currentTimeMillis()
            
            val newExpiry = if (currentExpiry > now) {
                // If already ad-free, extend by 24 hours
                currentExpiry + 86400000L // 24 * 60 * 60 * 1000
            } else {
                // If not ad-free (or expired), start 24 hours from now
                now + 86400000L
            }
            prefs[AD_FREE_EXPIRY_KEY] = newExpiry
        }
    }
}
