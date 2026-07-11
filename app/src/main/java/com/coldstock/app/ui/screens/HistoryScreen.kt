package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.model.ProductHistoryEvent
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.DateUtils
import com.coldstock.app.util.HistoryUtils

private fun filterLabel(f: HistoryUtils.Filter): String = when (f) {
    HistoryUtils.Filter.All -> "All events"
    HistoryUtils.Filter.Used -> "Used"
    HistoryUtils.Filter.Discarded -> "Discarded"
    HistoryUtils.Filter.PortionChanges -> "Portion changes"
    HistoryUtils.Filter.Moves -> "Moves"
}

@Composable
fun HistoryScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    var filter by remember { mutableStateOf(HistoryUtils.Filter.All) }

    val activeId = appData.settings.activeFreezerId
    val events = HistoryUtils.reverseChronological(
        HistoryUtils.apply(
            appData.historyEvents.filter { activeId == null || it.freezerId == activeId },
            filter
        )
    )

    ScreenScaffold(title = "History", nav = nav, showBottomBar = true) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                DropdownField(
                    label = "Filter",
                    selected = filter,
                    options = HistoryUtils.Filter.entries,
                    optionLabel = { filterLabel(it) },
                    onSelected = { filter = it }
                )
            }
            if (events.isEmpty()) {
                item { EmptyState("No history yet", "Product actions are recorded here as you use Coldstock.") }
            }
            items(events, key = { it.id }) { e ->
                HistoryRow(
                    event = e,
                    productName = appData.products.firstOrNull { it.id == e.productId }?.name
                        ?: "Deleted product",
                    onClick = { nav.navigate(Routes.historyDetail(e.id)) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HistoryRow(event: ProductHistoryEvent, productName: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = event.eventType.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${DateUtils.formatFriendly(event.eventDate)} ${event.eventTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        if (event.description.isNotBlank()) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
