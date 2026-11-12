package com.goldtracker.india.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.goldtracker.india.model.GoldPurity
import com.goldtracker.india.model.GoldRateSnapshot
import com.goldtracker.india.model.IndianRegion
import com.goldtracker.india.model.WeightUnit
import com.goldtracker.india.ui.GoldUiState
import com.goldtracker.india.ui.theme.CrimsonDown
import com.goldtracker.india.ui.theme.EmeraldUp
import com.goldtracker.india.util.WeightConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    state: GoldUiState,
    onSelectRegion: (IndianRegion) -> Unit,
    onSelectPurity: (GoldPurity) -> Unit,
    onSelectUnit: (WeightUnit) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snapshot = state.snapshot
    val quickUnits = listOf(
        WeightUnit.GRAM,
        WeightUnit.TEN_GRAM,
        WeightUnit.BHORI,
        WeightUnit.TOLA,
        WeightUnit.EIGHT_GRAM
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            RegionSelectorBar(
                selectedRegion = state.selectedRegion,
                isRefreshing = state.isRefreshing,
                onSelectRegion = onSelectRegion,
                onRefresh = onRefresh
            )
        }

        if (state.errorBanner != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorBanner,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        item {
            HeroRateCard(
                snapshot = snapshot,
                selectedPurity = state.selectedPurity,
                selectedUnit = state.selectedUnit
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Purity & Quick Weight",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoldPurity.entries.forEach { purity ->
                        FilterChip(
                            selected = purity == state.selectedPurity,
                            onClick = { onSelectPurity(purity) },
                            label = { Text("${purity.label} (${purity.purityDescription})") }
                        )
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickUnits.forEach { unit ->
                        FilterChip(
                            selected = unit == state.selectedUnit,
                            onClick = { onSelectUnit(unit) },
                            label = { Text("1 ${unit.displayName}") }
                        )
                    }
                }
            }
        }

        item {
            AllWeightsMatrixCard(snapshot = snapshot)
        }

        item {
            Text(
                text = "State & City Comparison (${state.selectedPurity.label})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(state.allRegionSnapshots) { regionSnap ->
            RegionComparisonRow(
                snapshot = regionSnap,
                purity = state.selectedPurity,
                isSelected = regionSnap.region == state.selectedRegion,
                onClick = { onSelectRegion(regionSnap.region) }
            )
        }
    }
}

@Composable
private fun RegionSelectorBar(
    selectedRegion: IndianRegion,
    isRefreshing: Boolean,
    onSelectRegion: (IndianRegion) -> Unit,
    onRefresh: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            AssistChip(
                onClick = { expanded = true },
                label = {
                    Text(
                        text = selectedRegion.displayName,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Region",
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select state or city"
                    )
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                IndianRegion.entries.forEach { region ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(region.displayName, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "Ref 22K: ${WeightConverter.formatInr(region.ref22KPerGram)}/g",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelectRegion(region)
                        }
                    )
                }
            }
        }

        IconButton(onClick = onRefresh, enabled = !isRefreshing) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh live rates"
                )
            }
        }
    }
}

@Composable
private fun HeroRateCard(
    snapshot: GoldRateSnapshot,
    selectedPurity: GoldPurity,
    selectedUnit: WeightUnit
) {
    val activePrice = snapshot.rateForUnit(selectedPurity, selectedUnit)
    val isPositive = snapshot.dailyChangePercent >= 0.0
    val timeFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedPurity.label} • 1 ${selectedUnit.displayName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = if (snapshot.isLiveApi) "LIVE" else "DAILY REF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = WeightConverter.formatInr(activePrice, decimals = 0),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositive) EmeraldUp else CrimsonDown,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = String.format(
                        Locale.US,
                        "%+.2f%% vs prior • %s",
                        snapshot.dailyChangePercent,
                        snapshot.sourceLabel
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniPurityStat("24K / 1g", WeightConverter.formatInr(snapshot.rate24KPerGram))
                MiniPurityStat("22K / 1g", WeightConverter.formatInr(snapshot.rate22KPerGram))
                MiniPurityStat("18K / 1g", WeightConverter.formatInr(snapshot.rate18KPerGram))
            }

            Text(
                text = "Updated: ${timeFormat.format(Date(snapshot.lastUpdatedMillis))} (Excl. 3% GST & making)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun MiniPurityStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun AllWeightsMatrixCard(snapshot: GoldRateSnapshot) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "All Weights Breakdown (${snapshot.region.shortLocation})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Weight",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    "24K",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "22K",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "18K",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            WeightUnit.entries.forEach { unit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = unit.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${WeightConverter.formatWeight(unit.gramsPerUnit, 4)} g",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = WeightConverter.formatInr(snapshot.rateForUnit(GoldPurity.K24, unit)),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = WeightConverter.formatInr(snapshot.rateForUnit(GoldPurity.K22, unit)),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = WeightConverter.formatInr(snapshot.rateForUnit(GoldPurity.K18, unit)),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
private fun RegionComparisonRow(
    snapshot: GoldRateSnapshot,
    purity: GoldPurity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = snapshot.region.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = "1 Bhori: ${WeightConverter.formatInr(snapshot.rateForUnit(purity, WeightUnit.BHORI))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${WeightConverter.formatInr(snapshot.ratePerGram(purity))}/g",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "10g: ${WeightConverter.formatInr(snapshot.rateForUnit(purity, WeightUnit.TEN_GRAM))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
