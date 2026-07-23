package com.kharcha.core.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logTransactionAdded(amount: Double, category: String, type: String) {
        val bundle = Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, amount)
            putString(FirebaseAnalytics.Param.CURRENCY, "INR")
            putString("category", category)
            putString("transaction_type", type)
        }
        firebaseAnalytics.logEvent("transaction_added", bundle)
    }

    fun logCategoryCreated(categoryName: String, type: String) {
        val bundle = Bundle().apply {
            putString("category_name", categoryName)
            putString("category_type", type)
        }
        firebaseAnalytics.logEvent("category_created", bundle)
    }

    fun logFeatureUsed(featureName: String) {
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
        }
        firebaseAnalytics.logEvent("feature_used", bundle)
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}
