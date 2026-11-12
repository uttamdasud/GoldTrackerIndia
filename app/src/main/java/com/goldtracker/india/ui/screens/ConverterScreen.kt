package com.goldtracker.india.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.goldtracker.india.model.GoldPurity
import com.goldtracker.india.model.WeightUnit
import com.goldtracker.india.ui.GoldUiState
import com.goldtracker.india.util.WeightConverter

@Composable
fun ConverterScreen(
    state: GoldUiState,
    onSelectPurity: (GoldPurity) -> Unit,
    onUpdateInput: (String) -> Unit,
    onUpdateFromUnit: (WeightUnit) -> Unit,
    onUpdateTraditional: (String, String, String) -> Unit,
    onUpdateMakingCharge: (Float) -> Unit,
    onToggleGst: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val inputValue = state.converterInputText.toDoubleOrNull() ?: 0.0
    val totalGrams = WeightConverter.toGrams(inputValue, state.converterFromUnit)
    val (revBhori, revAnna, revRatti) = WeightConverter.gramsToTraditional(totalGrams)
    val ratePerGram = state.snapshot.ratePerGram(state.selectedPurity)

    val tradBhori = state.bhoriInput.toDoubleOrNull() ?: 0.0
    val tradAnna = state.annaInput.toDoubleOrNull() ?: 0.0
    val tradRatti = state.rattiInput.toDoubleOrNull() ?: 0.0
    val tradGrams = WeightConverter.traditionalToGrams(tradBhori, tradAnna, tradRatti)

    val bill = WeightConverter.calculateJewelleryBill(
        grams = totalGrams,
        ratePerGramInr = ratePerGram,
        makingChargePercent = state.makingChargePercent.toDouble(),
        includeGst = state.includeGst
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Weight & Jewellery Bill Calculator",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Region: ${state.selectedRegion.displayName} • ${state.selectedPurity.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoldPurity.entries.forEach { purity ->
                    FilterChip(
                        selected = purity == state.selectedPurity,
                        onClick = { onSelectPurity(purity) },
                        label = { Text(purity.label) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Universal Weight Converter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = state.converterInputText,
                        onValueChange = onUpdateInput,
                        label = { Text("Enter Weight (${state.converterFromUnit.displayName})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeightUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = unit == state.converterFromUnit,
                                onClick = { onUpdateFromUnit(unit) },
                                label = { Text(unit.displayName) }
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Traditional Breakdown",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$revBhori Bhori • $revAnna Anna • ${WeightConverter.formatWeight(revRatti, 2)} Ratti",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider()

                    WeightUnit.entries.forEach { target ->
                        val converted = WeightConverter.fromGrams(totalGrams, target)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = target.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${WeightConverter.formatWeight(converted, 4)} ${target.shortSymbol}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Traditional Bhori • Anna • Ratti (Rotti)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "1 Bhori = 16 Anna = 96 Ratti = 11.664 g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.bhoriInput,
                            onValueChange = { onUpdateTraditional(it, state.annaInput, state.rattiInput) },
                            label = { Text("Bhori") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.annaInput,
                            onValueChange = { onUpdateTraditional(state.bhoriInput, it, state.rattiInput) },
                            label = { Text("Anna") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.rattiInput,
                            onValueChange = { onUpdateTraditional(state.bhoriInput, state.annaInput, it) },
                            label = { Text("Ratti") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Exact Metric Weight:", fontWeight = FontWeight.Medium)
                        Text(
                            text = "${WeightConverter.formatWeight(tradGrams, 4)} g",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Gold Value (${state.selectedPurity.label}):", fontWeight = FontWeight.Medium)
                        Text(
                            text = WeightConverter.formatInr(tradGrams * ratePerGram),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Jewellery Bill Estimator (${WeightConverter.formatWeight(totalGrams, 3)} g)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Making Charges: ${state.makingChargePercent.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.makingChargePercent,
                        onValueChange = onUpdateMakingCharge,
                        valueRange = 0f..30f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Include 3% GST (India Standard)")
                        Switch(
                            checked = state.includeGst,
                            onCheckedChange = onToggleGst
                        )
                    }

                    HorizontalDivider()

                    BillRow("Gold Value", WeightConverter.formatInr(bill.goldValueInr))
                    BillRow("Making Charges", WeightConverter.formatInr(bill.makingChargesInr))
                    BillRow("GST (3%)", WeightConverter.formatInr(bill.gstInr))
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total Estimated Bill",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            WeightConverter.formatInr(bill.totalPayableInr),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
