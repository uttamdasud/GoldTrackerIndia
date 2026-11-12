package com.goldtracker.india.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.goldtracker.india.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Non-intrusive ad manager:
 * - No forced pop-up interstitials on launch, tab switch, or calculation.
 * - Single adaptive banner docked cleanly above the bottom navigation bar.
 * - Optional user-initiated Rewarded Ad that grants 24 hours of ad-free usage.
 */
class AdPreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_ADS, Context.MODE_PRIVATE)

    fun isAdFreeActive(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val expiry = prefs.getLong(KEY_AD_FREE_UNTIL, 0L)
        return nowMillis < expiry
    }

    fun grantAdFreeFor24Hours(nowMillis: Long = System.currentTimeMillis()) {
        val expiry = nowMillis + TWENTY_FOUR_HOURS_MS
        prefs.edit().putLong(KEY_AD_FREE_UNTIL, expiry).apply()
    }

    fun remainingAdFreeHours(nowMillis: Long = System.currentTimeMillis()): Int {
        val expiry = prefs.getLong(KEY_AD_FREE_UNTIL, 0L)
        val diff = expiry - nowMillis
        if (diff <= 0L) return 0
        return ((diff / (1000L * 60L * 60L)) + 1).toInt()
    }

    companion object {
        private const val PREFS_ADS = "gold_tracker_ads_prefs"
        private const val KEY_AD_FREE_UNTIL = "ad_free_until_millis"
        private const val TWENTY_FOUR_HOURS_MS = 24L * 60L * 60L * 1000L
    }
}

object AdMobHelper {
    private var rewardedAd: RewardedAd? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        MobileAds.initialize(context) {}
        preloadRewardedAd(context)
    }

    fun preloadRewardedAd(context: Context) {
        val unitId = context.getString(R.string.admob_rewarded_unit_id)
        RewardedAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showRewardedAdForAdFree(
        activity: Activity,
        onRewardGranted: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            preloadRewardedAd(activity)
            onUnavailable()
            return
        }
        ad.show(activity) {
            onRewardGranted()
            rewardedAd = null
            preloadRewardedAd(activity)
        }
    }
}

@Composable
fun AdaptiveBannerAd(
    isAdFree: Boolean,
    modifier: Modifier = Modifier
) {
    if (isAdFree) return

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = ctx.getString(R.string.admob_banner_unit_id)
                    setAdSize(
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                            ctx,
                            screenWidthDp
                        )
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isAdLoaded = false
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
