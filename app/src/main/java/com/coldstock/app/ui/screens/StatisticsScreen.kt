package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.model.ProductHistoryEventType
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.CountPill
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.theme.DiscardedCharcoal
import com.coldstock.app.ui.theme.NoReviewGray
import com.coldstock.app.ui.theme.OkTeal
import com.coldstock.app.ui.theme.ReviewPassedRed
import com.coldstock.app.ui.theme.UsedBlueGray
import com.coldstock.app.ui.theme.UseSoonAmber
import com.coldstock.app.util.HistoryUtils
import com.coldstock.app.util.InventoryUtils
import com.coldstock.app.util.StatusUtils

@Composable
fun StatisticsScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    val soon = appData.settings.soonThresholdDays
    val activeId = appData.settings.activeFreezerId
    val active = InventoryUtils.activeProductsFor(appData.products, activeId)
    val drawers = appData.drawers.filter { it.freezerId == activeId }.sortedBy { it.sortOrder }

    val ok = active.count { StatusUtils.statusOf(it, soon) == FrozenProductStatus.Ok }
    val useSoon = active.count { StatusUtils.statusOf(it, soon) == FrozenProductStatus.UseSoon }
    val passed = active.count { StatusUtils.statusOf(it, soon) == FrozenProductStatus.Old }
    val noDate = active.count { StatusUtils.statusOf(it, soon) == FrozenProductStatus.NoReviewDate }
    val totalPortions = InventoryUtils.totalPortions(active)

    val usedThisMonth = HistoryUtils.countThisMonth(
        appData.historyEvents.filter { activeId == null || it.freezerId == activeId },
        ProductHistoryEventType.MarkedUsed
    )
    val discardedThisMonth = HistoryUtils.countThisMonth(
        appData.historyEvents.filter { activeId == null || it.freezerId == activeId },
        ProductHistoryEventType.MarkedDiscarded
    )

    val mostCategory = InventoryUtils.mostUsedCategory(active)?.label ?: "—"
    val mostDrawer = InventoryUtils.mostOccupiedDrawer(appData.products, drawers, activeId)?.name ?: "—"

    val perDrawer = drawers.map { d ->
        d.name to InventoryUtils.productsInDrawer(appData.products, d.id).size
    }
    val maxDrawer = (perDrawer.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)

    ScreenScaffold(title = "Statistics", nav = nav, showBack = true) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountPill("active", active.size, MaterialTheme.colorScheme.primary)
                    CountPill("portions", totalPortions, OkTeal)
                }
            }

            item { SectionHeader("Status distribution") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatBar("OK", ok, active.size, OkTeal)
                    StatBar("Use Soon", useSoon, active.size, UseSoonAmber)
                    StatBar("Review Date Passed", passed, active.size, ReviewPassedRed)
                    StatBar("No Review Date", noDate, active.size, NoReviewGray)
                }
            }

            item { SectionHeader("Products per drawer") }
            if (perDrawer.isEmpty()) {
                item { Text("No drawers.", style = MaterialTheme.typography.bodySmall) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    perDrawer.forEach { (name, count) ->
                        StatBar(name, count, maxDrawer, MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            item { SectionHeader("This month") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MonthColumn("Used", usedThisMonth, UsedBlueGray)
                    MonthColumn("Discarded", discardedThisMonth, DiscardedCharcoal)
                }
            }

            item { SectionHeader("Summary") }
            item {
                Column {
                    Text("Most used category: $mostCategory", style = MaterialTheme.typography.bodyMedium)
                    Text("Most occupied drawer: $mostDrawer", style = MaterialTheme.typography.bodyMedium)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int, max: Int, color: Color) {
    val fraction = if (max <= 0) 0f else (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun MonthColumn(label: String, value: Int, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
