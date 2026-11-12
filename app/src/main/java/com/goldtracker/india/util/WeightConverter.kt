package com.goldtracker.india.util

import com.goldtracker.india.model.JewelleryBillBreakdown
import com.goldtracker.india.model.WeightUnit
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor

object WeightConverter {

    private val indianLocale = Locale("en", "IN")

    fun toGrams(value: Double, unit: WeightUnit): Double {
        if (value <= 0.0) return 0.0
        return value * unit.gramsPerUnit
    }

    fun fromGrams(grams: Double, targetUnit: WeightUnit): Double {
        if (grams <= 0.0) return 0.0
        return grams / targetUnit.gramsPerUnit
    }

    fun convert(value: Double, from: WeightUnit, to: WeightUnit): Double {
        return fromGrams(toGrams(value, from), to)
    }

    /**
     * Converts traditional Bengali/Indian composite weight:
     * 1 Bhori = 16 Anna = 96 Ratti = 11.664 grams
     * 1 Anna = 6 Ratti = 0.729 grams
     * 1 Ratti = 0.1215 grams
     */
    fun traditionalToGrams(bhori: Double, anna: Double, ratti: Double): Double {
        val totalGrams =
            (bhori.coerceAtLeast(0.0) * WeightUnit.BHORI.gramsPerUnit) +
                (anna.coerceAtLeast(0.0) * WeightUnit.ANNA.gramsPerUnit) +
                (ratti.coerceAtLeast(0.0) * WeightUnit.RATTI.gramsPerUnit)
        return totalGrams
    }

    /**
     * Breaks down a weight in grams into (Bhori, Anna, Ratti).
     */
    fun gramsToTraditional(grams: Double): Triple<Int, Int, Double> {
        if (grams <= 0.0) return Triple(0, 0, 0.0)
        val totalRatti = grams / WeightUnit.RATTI.gramsPerUnit
        val bhori = floor(totalRatti / 96.0).toInt()
        val remAfterBhori = totalRatti - (bhori * 96.0)
        val anna = floor(remAfterBhori / 6.0).toInt()
        val ratti = remAfterBhori - (anna * 6.0)
        return Triple(bhori, anna, ratti)
    }

    /**
     * Estimates Indian retail jewellery bill including Making Charges + 3% GST.
     */
    fun calculateJewelleryBill(
        grams: Double,
        ratePerGramInr: Double,
        makingChargePercent: Double,
        includeGst: Boolean
    ): JewelleryBillBreakdown {
        val safeGrams = grams.coerceAtLeast(0.0)
        val safeRate = ratePerGramInr.coerceAtLeast(0.0)
        val safeMakingPct = makingChargePercent.coerceAtLeast(0.0)

        val goldValue = safeGrams * safeRate
        val makingCharges = goldValue * (safeMakingPct / 100.0)
        val subTotal = goldValue + makingCharges
        val gst = if (includeGst) subTotal * 0.03 else 0.0
        val total = subTotal + gst

        return JewelleryBillBreakdown(
            goldValueInr = goldValue,
            makingChargesInr = makingCharges,
            gstInr = gst,
            totalPayableInr = total
        )
    }

    fun formatInr(amount: Double, decimals: Int = 0): String {
        val formatter = NumberFormat.getCurrencyInstance(indianLocale).apply {
            maximumFractionDigits = decimals
            minimumFractionDigits = decimals
        }
        return formatter.format(amount)
    }

    fun formatWeight(value: Double, decimals: Int = 3): String {
        return String.format(Locale.US, "%.${decimals}f", value)
            .trimEnd('0')
            .trimEnd('.')
            .ifEmpty { "0" }
    }
}
