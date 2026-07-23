package com.kharcha.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdMobBanner(
    isAdFree: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isAdFree) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    adUnitId = if (isDebug) {
                        "ca-app-pub-3940256099942544/6300978111" // Test ID
                    } else {
                        "ca-app-pub-4020407055398275/7925224769" // Production ID
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
