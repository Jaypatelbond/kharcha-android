package com.kharcha.tracker.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private val REWARDED_AD_UNIT_ID by lazy {
        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            "ca-app-pub-3940256099942544/5224354917" // Test ID
        } else {
            "ca-app-pub-4020407055398275/5243221105" // Production ID
        }
    } 

    fun loadRewardedAd(onAdLoaded: () -> Unit = {}, onAdFailed: (LoadAdError) -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                onAdFailed(adError)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                onAdLoaded()
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null // Invalidate the reference
                loadRewardedAd() // Preload the next one
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                rewardedAd = null
                onAdDismissed()
            }
        }

        if (rewardedAd != null) {
            rewardedAd?.show(activity) { _ ->
                onUserEarnedReward()
            }
        } else {
            // Ad wasn't ready
            onAdDismissed()
            loadRewardedAd()
        }
    }
}
