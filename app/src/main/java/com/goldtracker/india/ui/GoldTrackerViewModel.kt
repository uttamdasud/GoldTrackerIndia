package com.goldtracker.india.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.goldtracker.india.model.GoldPurity
import com.goldtracker.india.model.GoldRateSnapshot
import com.goldtracker.india.model.IndianRegion
import com.goldtracker.india.model.WeightUnit
import com.goldtracker.india.network.LiveGoldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoldUiState(
    val selectedRegion: IndianRegion = IndianRegion.TRIPURA_AGARTALA,
    val selectedPurity: GoldPurity = GoldPurity.K22,
    val selectedUnit: WeightUnit = WeightUnit.BHORI,
    val snapshot: GoldRateSnapshot,
    val allRegionSnapshots: List<GoldRateSnapshot> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorBanner: String? = null,
    // Converter state
    val converterInputText: String = "1",
    val converterFromUnit: WeightUnit = WeightUnit.BHORI,
    // Composite Bhori-Anna-Ratti state
    val bhoriInput: String = "1",
    val annaInput: String = "0",
    val rattiInput: String = "0",
    // Jewellery bill estimator
    val makingChargePercent: Float = 10f,
    val includeGst: Boolean = true,
    // Settings
    val indiaDutyFactor: Double = LiveGoldRepository.DEFAULT_INDIA_LANDED_FACTOR,
    val goldApiKey: String = ""
)

class GoldTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LiveGoldRepository(application.applicationContext)

    private val initialRegion = repository.getSavedRegion()
    private val initialSnapshot = repository.getFallbackSnapshot(initialRegion)

    private val _uiState = MutableStateFlow(
        GoldUiState(
            selectedRegion = initialRegion,
            snapshot = initialSnapshot,
            allRegionSnapshots = IndianRegion.entries.map { repository.getFallbackSnapshot(it) },
            indiaDutyFactor = repository.getCustomIndiaDutyFactor(),
            goldApiKey = repository.getGoldApiKey()
        )
    )
    val uiState: StateFlow<GoldUiState> = _uiState.asStateFlow()

    init {
        refreshLiveRates()
    }

    fun refreshLiveRates() {
        val currentRegion = _uiState.value.selectedRegion
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorBanner = null) }
            val result = repository.fetchRatesForRegion(currentRegion)
            result.onSuccess { liveSnapshot ->
                val allSnapshots = IndianRegion.entries.map { region ->
                    if (region == currentRegion) {
                        liveSnapshot
                    } else {
                        val ratio = region.regionalSpreadMultiplier / currentRegion.regionalSpreadMultiplier
                        val r24 = liveSnapshot.rate24KPerGram * ratio
                        liveSnapshot.copy(
                            region = region,
                            rate24KPerGram = r24,
                            rate22KPerGram = r24 * GoldPurity.K22.ratioTo24K,
                            rate18KPerGram = r24 * GoldPurity.K18.ratioTo24K,
                            rate14KPerGram = r24 * GoldPurity.K14.ratioTo24K
                        )
                    }
                }
                _uiState.update {
                    it.copy(
                        snapshot = liveSnapshot,
                        allRegionSnapshots = allSnapshots,
                        isRefreshing = false,
                        errorBanner = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorBanner = "Showing daily reference rates (${err.localizedMessage ?: "offline"})"
                    )
                }
            }
        }
    }

    fun selectRegion(region: IndianRegion) {
        repository.saveSelectedRegion(region)
        val existing = _uiState.value.allRegionSnapshots.firstOrNull { it.region == region }
            ?: repository.getFallbackSnapshot(region)
        _uiState.update {
            it.copy(
                selectedRegion = region,
                snapshot = existing
            )
        }
        refreshLiveRates()
    }

    fun selectPurity(purity: GoldPurity) {
        _uiState.update { it.copy(selectedPurity = purity) }
    }

    fun selectWeightUnit(unit: WeightUnit) {
        _uiState.update { it.copy(selectedUnit = unit) }
    }

    fun updateConverterInput(text: String) {
        _uiState.update { it.copy(converterInputText = text) }
    }

    fun updateConverterFromUnit(unit: WeightUnit) {
        _uiState.update { it.copy(converterFromUnit = unit) }
    }

    fun updateTraditionalInputs(bhori: String, anna: String, ratti: String) {
        _uiState.update {
            it.copy(bhoriInput = bhori, annaInput = anna, rattiInput = ratti)
        }
    }

    fun updateMakingChargePercent(percent: Float) {
        _uiState.update { it.copy(makingChargePercent = percent) }
    }

    fun toggleIncludeGst(include: Boolean) {
        _uiState.update { it.copy(includeGst = include) }
    }

    fun saveSettings(dutyFactor: Double, apiKey: String) {
        repository.saveCustomIndiaDutyFactor(dutyFactor)
        repository.saveGoldApiKey(apiKey)
        _uiState.update {
            it.copy(indiaDutyFactor = dutyFactor, goldApiKey = apiKey)
        }
        refreshLiveRates()
    }
}
