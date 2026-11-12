package com.goldtracker.india

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goldtracker.india.ads.AdMobHelper
import com.goldtracker.india.ads.AdPreferenceManager
import com.goldtracker.india.ads.AdaptiveBannerAd
import com.goldtracker.india.ui.GoldTrackerViewModel
import com.goldtracker.india.ui.screens.ConverterScreen
import com.goldtracker.india.ui.screens.DashboardScreen
import com.goldtracker.india.ui.theme.GoldTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdMobHelper.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            GoldTrackerTheme {
                GoldTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldTrackerApp(viewModel: GoldTrackerViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adPrefs = remember { AdPreferenceManager(context) }

    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var isAdFree by remember { mutableStateOf(adPrefs.isAdFreeActive()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gold Tracker India",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isAdFree) {
                                Toast.makeText(
                                    context,
                                    "Ad-Free active (${adPrefs.remainingAdFreeHours()}h left)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (activity != null) {
                                AdMobHelper.showRewardedAdForAdFree(
                                    activity = activity,
                                    onRewardGranted = {
                                        adPrefs.grantAdFreeFor24Hours()
                                        isAdFree = true
                                        Toast.makeText(
                                            context,
                                            "Ads disabled for 24 hours!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onUnavailable = {
                                        Toast.makeText(
                                            context,
                                            "Rewarded ad is loading, try again in a moment.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Go Ad-Free for 24h",
                            tint = if (isAdFree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "API & Premium Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Non-intrusive adaptive banner docked above NavigationBar
                AdaptiveBannerAd(isAdFree = isAdFree)
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                        label = { Text("Live Rates") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                        label = { Text("Weight & Bill") }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            DashboardScreen(
                state = state,
                onSelectRegion = viewModel::selectRegion,
                onSelectPurity = viewModel::selectPurity,
                onSelectUnit = viewModel::selectWeightUnit,
                onRefresh = viewModel::refreshLiveRates,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            ConverterScreen(
                state = state,
                onSelectPurity = viewModel::selectPurity,
                onUpdateInput = viewModel::updateConverterInput,
                onUpdateFromUnit = viewModel::updateConverterFromUnit,
                onUpdateTraditional = viewModel::updateTraditionalInputs,
                onUpdateMakingCharge = viewModel::updateMakingChargePercent,
                onToggleGst = viewModel::toggleIncludeGst,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showSettings) {
        var dutyInput by remember(state.indiaDutyFactor) {
            mutableStateOf(state.indiaDutyFactor.toString())
        }
        var apiKeyInput by remember(state.goldApiKey) {
            mutableStateOf(state.goldApiKey)
        }

        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Live Rate & Ad Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Uses free no-key live spot (gold-api.com) + USD/INR FX (open.er-api.com), or an optional GoldAPI.io key."
                    )
                    OutlinedTextField(
                        value = dutyInput,
                        onValueChange = { dutyInput = it },
                        label = { Text("India Landed Factor (default 1.075)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Optional GoldAPI.io Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    if (isAdFree) {
                        Text(
                            text = "Ad-Free Pass Active (${adPrefs.remainingAdFreeHours()}h remaining)",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (activity != null) {
                        Button(
                            onClick = {
                                AdMobHelper.showRewardedAdForAdFree(
                                    activity = activity,
                                    onRewardGranted = {
                                        adPrefs.grantAdFreeFor24Hours()
                                        isAdFree = true
                                        showSettings = false
                                    },
                                    onUnavailable = {
                                        Toast.makeText(
                                            context,
                                            "Ad still loading, please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Watch 1 Short Ad → 24h Ad-Free")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val factor = dutyInput.toDoubleOrNull()?.coerceIn(0.9, 1.3)
                            ?: state.indiaDutyFactor
                        viewModel.saveSettings(factor, apiKeyInput)
                        showSettings = false
                    }
                ) {
                    Text("Save & Refresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
