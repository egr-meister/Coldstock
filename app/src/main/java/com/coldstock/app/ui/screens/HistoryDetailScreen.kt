package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.DetailRow
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.DateUtils

@Composable
fun HistoryDetailScreen(
    vm: ColdstockViewModel,
    nav: NavHostController,
    eventId: String?,
    appData: AppData
) {
    val event = appData.historyEvents.firstOrNull { it.id == eventId }
    if (event == null) {
        NotFoundScreen("History", "History event not found", nav)
        return
    }
    val product = appData.products.firstOrNull { it.id == event.productId }
    val freezer = appData.freezers.firstOrNull { it.id == event.freezerId }

    ScreenScaffold(title = "History Event", nav = nav, showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailRow("Event", event.eventType.label)
            DetailRow("Product", product?.name ?: "Deleted product")
            DetailRow("Freezer", freezer?.name ?: "Freezer not found")
            DetailRow("Date", DateUtils.formatFriendly(event.eventDate))
            DetailRow("Time", event.eventTime.ifBlank { "—" })
            DetailRow("Portions before", event.portionsBefore?.toString() ?: "—")
            DetailRow("Portions after", event.portionsAfter?.toString() ?: "—")
            DetailRow("Description", event.description.ifBlank { "—" })

            if (product != null) {
                androidx.compose.foundation.layout.Spacer(Modifier.padding(6.dp))
                OutlinedButton(onClick = { nav.navigate(Routes.productDetail(product.id)) }) {
                    Text("Open product")
                }
            }
        }
    }
}
