package com.goldtracker.india.network

import android.content.Context
import com.goldtracker.india.model.GoldPurity
import com.goldtracker.india.model.GoldRateSnapshot
import com.goldtracker.india.model.IndianRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LiveGoldRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    fun getSavedRegion(): IndianRegion {
        val name = prefs.getString(KEY_SELECTED_REGION, IndianRegion.TRIPURA_AGARTALA.name)
        return IndianRegion.entries.firstOrNull { it.name == name } ?: IndianRegion.TRIPURA_AGARTALA
    }

    fun saveSelectedRegion(region: IndianRegion) {
        prefs.edit().putString(KEY_SELECTED_REGION, region.name).apply()
    }

    fun getCustomIndiaDutyFactor(): Double {
        return prefs.getFloat(KEY_DUTY_FACTOR, DEFAULT_INDIA_LANDED_FACTOR.toFloat()).toDouble()
    }

    fun saveCustomIndiaDutyFactor(factor: Double) {
        prefs.edit().putFloat(KEY_DUTY_FACTOR, factor.toFloat()).apply()
    }

    fun getGoldApiKey(): String {
        return prefs.getString(KEY_GOLDAPI_TOKEN, "") ?: ""
    }

    fun saveGoldApiKey(token: String) {
        prefs.edit().putString(KEY_GOLDAPI_TOKEN, token.trim()).apply()
    }

    /**
     * Fetches live spot gold price in INR:
     * 1. If user configured a GoldAPI.io key, fetches XAU/INR directly from goldapi.io.
     * 2. Otherwise, fetches free no-key XAU/USD from api.gold-api.com + USD/INR from open.er-api.com.
     * 3. Applies India landed bullion premium (customs/IGST/local spread) and regional spread
     *    (e.g., Tripura/Agartala vs Kolkata).
     * 4. Falls back gracefully to cached live rate or latest daily reference rate if offline.
     */
    suspend fun fetchRatesForRegion(region: IndianRegion): Result<GoldRateSnapshot> =
        withContext(Dispatchers.IO) {
            try {
                val customKey = getGoldApiKey()
                val dutyFactor = getCustomIndiaDutyFactor()

                val liveBase24KPerGram: Double
                val spotUsdOz: Double?
                val usdInr: Double?
                val sourceLabel: String

                if (customKey.isNotBlank()) {
                    val json = fetchJson(
                        url = "https://www.goldapi.io/api/XAU/INR",
                        headers = mapOf("x-access-token" to customKey)
                    )
                    val pricePerGram24KSpot = json.optDouble("price_gram_24k", 0.0)
                    if (pricePerGram24KSpot <= 0.0) {
                        throw IllegalStateException("Invalid GoldAPI.io response")
                    }
                    liveBase24KPerGram = pricePerGram24KSpot * dutyFactor
                    spotUsdOz = null
                    usdInr = null
                    sourceLabel = "Live (GoldAPI.io + Regional Spread)"
                } else {
                    val goldJson = fetchJson("https://api.gold-api.com/price/XAU")
                    val xauUsdPerOz = goldJson.optDouble("price", 0.0)
                    if (xauUsdPerOz <= 0.0) {
                        throw IllegalStateException("Invalid XAU spot response")
                    }

                    val fxJson = fetchJson("https://open.er-api.com/v6/latest/USD")
                    val ratesObj = fxJson.getJSONObject("rates")
                    val inrRate = ratesObj.optDouble("INR", 0.0)
                    if (inrRate <= 0.0) {
                        throw IllegalStateException("Invalid USD/INR response")
                    }

                    val spotInrPerGram = (xauUsdPerOz * inrRate) / GRAMS_PER_TROY_OUNCE
                    liveBase24KPerGram = spotInrPerGram * dutyFactor
                    spotUsdOz = xauUsdPerOz
                    usdInr = inrRate
                    sourceLabel = "Live Spot (Gold-API + FX)"
                }

                val regional24K = liveBase24KPerGram * region.regionalSpreadMultiplier
                val regional22K = regional24K * GoldPurity.K22.ratioTo24K
                val regional18K = regional24K * GoldPurity.K18.ratioTo24K
                val regional14K = regional24K * GoldPurity.K14.ratioTo24K

                val previous24K = prefs.getFloat(KEY_LAST_24K_PREFIX + region.name, region.ref24KPerGram.toFloat()).toDouble()
                val changePct = if (previous24K > 0.0) {
                    ((regional24K - previous24K) / previous24K) * 100.0
                } else {
                    0.0
                }

                val now = System.currentTimeMillis()
                prefs.edit()
                    .putFloat(KEY_LAST_24K_PREFIX + region.name, regional24K.toFloat())
                    .putLong(KEY_LAST_UPDATED, now)
                    .apply()

                Result.success(
                    GoldRateSnapshot(
                        region = region,
                        rate24KPerGram = regional24K,
                        rate22KPerGram = regional22K,
                        rate18KPerGram = regional18K,
                        rate14KPerGram = regional14K,
                        dailyChangePercent = changePct,
                        internationalSpotUsdPerOz = spotUsdOz,
                        usdToInrRate = usdInr,
                        sourceLabel = sourceLabel,
                        isLiveApi = true,
                        lastUpdatedMillis = now
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getFallbackSnapshot(region: IndianRegion): GoldRateSnapshot {
        val r24 = region.ref24KPerGram
        val r22 = region.ref22KPerGram
        val r18 = region.ref18KPerGram
        val r14 = r24 * GoldPurity.K14.ratioTo24K

        return GoldRateSnapshot(
            region = region,
            rate24KPerGram = r24,
            rate22KPerGram = r22,
            rate18KPerGram = r18,
            rate14KPerGram = r14,
            dailyChangePercent = 0.0,
            internationalSpotUsdPerOz = null,
            usdToInrRate = null,
            sourceLabel = "Daily Reference Rate (Offline)",
            isLiveApi = false,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    private fun fetchJson(url: String, headers: Map<String, String> = emptyMap()): JSONObject {
        val builder = Request.Builder().url(url).get()
        headers.forEach { (k, v) -> builder.addHeader(k, v) }
        client.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code} from $url")
            }
            val body = response.body?.string() ?: throw IllegalStateException("Empty body")
            return JSONObject(body)
        }
    }

    companion object {
        private const val PREFS_NAME = "gold_tracker_prefs"
        private const val KEY_SELECTED_REGION = "selected_region"
        private const val KEY_DUTY_FACTOR = "india_duty_factor"
        private const val KEY_GOLDAPI_TOKEN = "goldapi_token"
        private const val KEY_LAST_24K_PREFIX = "last_24k_"
        private const val KEY_LAST_UPDATED = "last_updated"

        private const val GRAMS_PER_TROY_OUNCE = 31.1034768
        // Standard Indian landed bullion premium over international spot (customs + local market spread)
        const val DEFAULT_INDIA_LANDED_FACTOR = 1.075
    }
}
