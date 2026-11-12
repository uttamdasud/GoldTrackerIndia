package com.goldtracker.india.model

enum class GoldPurity(
    val karat: Int,
    val label: String,
    val purityDescription: String,
    val ratioTo24K: Double
) {
    K24(24, "24K", "99.9% Pure", 1.0),
    K22(22, "22K", "91.6% Hallmark", 22.0 / 24.0),
    K18(18, "18K", "75.0% Jewellery", 18.0 / 24.0),
    K14(14, "14K", "58.5% Light", 14.0 / 24.0)
}

enum class WeightUnit(
    val displayName: String,
    val shortSymbol: String,
    val gramsPerUnit: Double,
    val description: String
) {
    GRAM("Gram", "g", 1.0, "Metric standard (1 g)"),
    MILLIGRAM("Milligram", "mg", 0.001, "1/1000 Gram (0.001 g)"),
    EIGHT_GRAM("8 Gram (Pavan)", "8g", 8.0, "1 Sovereign / Pavan (8 g)"),
    TEN_GRAM("10 Gram", "10g", 10.0, "Standard Indian bullion quote (10 g)"),
    BHORI("Bhori / Vori", "bhori", 11.664, "Traditional WB, Tripura & Assam unit (11.664 g)"),
    TOLA("Tola", "tola", 11.6638, "Traditional Indian bullion unit (11.6638 g)"),
    ANNA("Anna", "anna", 0.729, "1/16 Bhori (0.729 g)"),
    MASHA("Masha", "masha", 0.972, "1/12 Bhori / 8 Ratti (0.972 g)"),
    RATTI("Ratti / Rotti", "ratti", 0.1215, "1/96 Bhori / 1/6 Anna (0.1215 g)"),
    HUNDRED_GRAM("100 Gram", "100g", 100.0, "100 g bar"),
    KILOGRAM("Kilogram", "kg", 1000.0, "1,000 g bullion bar"),
    TROY_OUNCE("Troy Ounce", "oz t", 31.1034768, "International spot unit (31.1035 g)")
}

enum class IndianRegion(
    val displayName: String,
    val shortLocation: String,
    val ref24KPerGram: Double,
    val ref22KPerGram: Double,
    val ref18KPerGram: Double,
    val regionalSpreadMultiplier: Double
) {
    TRIPURA_AGARTALA(
        displayName = "Tripura (Agartala)",
        shortLocation = "Agartala, TR",
        ref24KPerGram = 14466.0,
        ref22KPerGram = 13261.0,
        ref18KPerGram = 10850.0,
        regionalSpreadMultiplier = 1.0348
    ),
    KOLKATA(
        displayName = "Kolkata (West Bengal)",
        shortLocation = "Kolkata, WB",
        ref24KPerGram = 13980.0,
        ref22KPerGram = 12815.0,
        ref18KPerGram = 10485.0,
        regionalSpreadMultiplier = 1.0000
    ),
    INDIA_AVG(
        displayName = "India Average",
        shortLocation = "National",
        ref24KPerGram = 14020.0,
        ref22KPerGram = 12852.0,
        ref18KPerGram = 10515.0,
        regionalSpreadMultiplier = 1.0029
    ),
    GUWAHATI(
        displayName = "Guwahati (Assam)",
        shortLocation = "Guwahati, AS",
        ref24KPerGram = 14180.0,
        ref22KPerGram = 12998.0,
        ref18KPerGram = 10635.0,
        regionalSpreadMultiplier = 1.0143
    ),
    MUMBAI(
        displayName = "Mumbai (Maharashtra)",
        shortLocation = "Mumbai, MH",
        ref24KPerGram = 13970.0,
        ref22KPerGram = 12805.0,
        ref18KPerGram = 10478.0,
        regionalSpreadMultiplier = 0.9993
    ),
    DELHI(
        displayName = "Delhi (NCR)",
        shortLocation = "New Delhi, DL",
        ref24KPerGram = 13995.0,
        ref22KPerGram = 12828.0,
        ref18KPerGram = 10496.0,
        regionalSpreadMultiplier = 1.0011
    ),
    CHENNAI(
        displayName = "Chennai (Tamil Nadu)",
        shortLocation = "Chennai, TN",
        ref24KPerGram = 14090.0,
        ref22KPerGram = 12916.0,
        ref18KPerGram = 10568.0,
        regionalSpreadMultiplier = 1.0079
    ),
    BENGALURU(
        displayName = "Bengaluru (Karnataka)",
        shortLocation = "Bengaluru, KA",
        ref24KPerGram = 13985.0,
        ref22KPerGram = 12820.0,
        ref18KPerGram = 10489.0,
        regionalSpreadMultiplier = 1.0004
    ),
    HYDERABAD(
        displayName = "Hyderabad (Telangana)",
        shortLocation = "Hyderabad, TS",
        ref24KPerGram = 13990.0,
        ref22KPerGram = 12824.0,
        ref18KPerGram = 10493.0,
        regionalSpreadMultiplier = 1.0007
    )
}

data class GoldRateSnapshot(
    val region: IndianRegion,
    val rate24KPerGram: Double,
    val rate22KPerGram: Double,
    val rate18KPerGram: Double,
    val rate14KPerGram: Double,
    val dailyChangePercent: Double,
    val internationalSpotUsdPerOz: Double?,
    val usdToInrRate: Double?,
    val sourceLabel: String,
    val isLiveApi: Boolean,
    val lastUpdatedMillis: Long
) {
    fun ratePerGram(purity: GoldPurity): Double = when (purity) {
        GoldPurity.K24 -> rate24KPerGram
        GoldPurity.K22 -> rate22KPerGram
        GoldPurity.K18 -> rate18KPerGram
        GoldPurity.K14 -> rate14KPerGram
    }

    fun rateForUnit(purity: GoldPurity, unit: WeightUnit): Double =
        ratePerGram(purity) * unit.gramsPerUnit
}

data class JewelleryBillBreakdown(
    val goldValueInr: Double,
    val makingChargesInr: Double,
    val gstInr: Double,
    val totalPayableInr: Double
)
