package com.goldtracker.india

import com.goldtracker.india.model.WeightUnit
import com.goldtracker.india.util.WeightConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightConverterTest {

    @Test
    fun bhoriToGrams_isExact() {
        val grams = WeightConverter.toGrams(1.0, WeightUnit.BHORI)
        assertEquals(11.664, grams, 0.0001)
    }

    @Test
    fun traditionalBhoriAnnaRatti_convertsCorrectly() {
        // 1 Bhori + 8 Anna + 0 Ratti = 1.5 Bhori = 17.496 g
        val grams = WeightConverter.traditionalToGrams(bhori = 1.0, anna = 8.0, ratti = 0.0)
        assertEquals(17.496, grams, 0.0001)
    }

    @Test
    fun gramsToTraditional_breaksDownCorrectly() {
        val (bhori, anna, ratti) = WeightConverter.gramsToTraditional(17.496)
        assertEquals(1, bhori)
        assertEquals(8, anna)
        assertEquals(0.0, ratti, 0.01)
    }

    @Test
    fun mashaAndMilligram_convertCorrectly() {
        assertEquals(12.0, WeightConverter.convert(1.0, WeightUnit.BHORI, WeightUnit.MASHA), 0.001)
        assertEquals(1000.0, WeightConverter.convert(1.0, WeightUnit.GRAM, WeightUnit.MILLIGRAM), 0.001)
    }

    @Test
    fun jewelleryBill_calculatesMakingAndGst() {
        val bill = WeightConverter.calculateJewelleryBill(
            grams = 10.0,
            ratePerGramInr = 13000.0,
            makingChargePercent = 10.0,
            includeGst = true
        )
        assertEquals(130000.0, bill.goldValueInr, 0.01)
        assertEquals(13000.0, bill.makingChargesInr, 0.01)
        assertEquals(4290.0, bill.gstInr, 0.01)
        assertEquals(147290.0, bill.totalPayableInr, 0.01)
    }
}
