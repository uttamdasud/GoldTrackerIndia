# Gold Tracker India (Android — Kotlin & Jetpack Compose)

An Android application for tracking daily live gold rates across India, with dedicated support for **Tripura (Agartala)**, **Kolkata (West Bengal)**, Guwahati, Mumbai, Delhi, Chennai, Bengaluru, and Hyderabad, plus all traditional & metric weights (**Bhori/Vori**, **Anna**, **Ratti**, **Tola**, **Gram**, **8g Pavan**, **10g**, **100g**, **Kg**, **Troy Oz**).

## Features
- **Live Spot + FX Rate Engine**: Fetches live international spot gold (`api.gold-api.com/price/XAU`) and USD/INR FX (`open.er-api.com/v6/latest/USD`) with zero API key required, plus optional `GoldAPI.io` key support in Settings.
- **Offline Daily Reference Fallback**: Pre-loaded with regional reference rates (including Tripura/Agartala and Kolkata) so the app works immediately even offline.
- **All-Weights Price Matrix**: Instant 24K, 22K, and 18K breakdown across Gram, 8g, 10g, Bhori (11.664 g), Tola, Anna (0.729 g), Ratti (0.1215 g), 100g, 1 Kg, and Troy Ounce.
- **Traditional Bhori • Anna • Ratti Calculator**: Convert composite Bengali/Indian jewellery weights (`1 Bhori = 16 Anna = 96 Ratti = 11.664 g`) to grams and INR.
- **Jewellery Bill Estimator**: Calculate total retail cost with adjustable Making Charges (%) and 3% GST.
- **User-Friendly Monetization (Google AdMob)**:
  - **Adaptive Anchored Banner**: Docked cleanly above the bottom navigation bar so it never blocks content or interrupts calculations.
  - **Zero Forced Interstitials**: No pop-up ads on launch, tab switch, or while typing.
  - **Optional 24-Hour Ad-Free Reward**: Users can optionally watch 1 short Rewarded Ad (via the crown icon or Settings) to disable all banner ads for 24 hours.

## How to Build & Run
1. Open the `GoldTrackerIndia` folder in **Android Studio** (Ladybug / Koala or newer).
2. Let Gradle sync the dependencies (`Compose BOM`, `Material 3`, `OkHttp`, `Coroutines`, `play-services-ads`).
3. Run on an Android emulator or physical device (Android 7.0 / API 24+).
4. **Before Play Store Release**: Replace the Google test IDs in `app/src/main/res/values/strings.xml` (`admob_app_id`, `admob_banner_unit_id`, `admob_rewarded_unit_id`) with your own AdMob App ID and Ad Unit IDs.
