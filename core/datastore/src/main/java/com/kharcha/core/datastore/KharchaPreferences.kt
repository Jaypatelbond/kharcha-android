package com.kharcha.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class KharchaPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val AD_FREE_EXPIRY_KEY = longPreferencesKey("ad_free_expiry")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        private val APP_LOCK_PIN_HASH_KEY = stringPreferencesKey("app_lock_pin_hash")
        private val BIOMETRIC_UNLOCK_ENABLED_KEY = booleanPreferencesKey("biometric_unlock_enabled")

        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
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

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[APP_LOCK_ENABLED_KEY] ?: false
    }

    val appLockPinHash: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[APP_LOCK_PIN_HASH_KEY] ?: ""
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BIOMETRIC_UNLOCK_ENABLED_KEY] ?: true
    }

    fun verifyPin(enteredPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || enteredPin.isBlank()) return false
        return hashPin(enteredPin) == storedHash
    }

    suspend fun enableAppLock(pin: String, biometricEnabled: Boolean = true) {
        val pinHash = hashPin(pin)
        context.dataStore.edit { prefs ->
            prefs[APP_LOCK_ENABLED_KEY] = true
            prefs[APP_LOCK_PIN_HASH_KEY] = pinHash
            prefs[BIOMETRIC_UNLOCK_ENABLED_KEY] = biometricEnabled
        }
    }

    suspend fun updatePin(newPin: String) {
        val pinHash = hashPin(newPin)
        context.dataStore.edit { prefs ->
            prefs[APP_LOCK_PIN_HASH_KEY] = pinHash
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_UNLOCK_ENABLED_KEY] = enabled
        }
    }

    suspend fun disableAppLock() {
        context.dataStore.edit { prefs ->
            prefs[APP_LOCK_ENABLED_KEY] = false
            prefs[APP_LOCK_PIN_HASH_KEY] = ""
        }
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
