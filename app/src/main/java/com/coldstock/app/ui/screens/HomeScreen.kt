package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.StatusStrip
import com.coldstock.app.ui.components.DrawerSection
import com.coldstock.app.ui.components.UseFirstRail
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.Clock
import com.coldstock.app.util.DateUtils
import com.coldstock.app.util.InventoryUtils
import com.coldstock.app.util.ReminderUtils
import com.coldstock.app.util.StatusUtils
import com.coldstock.app.util.UseFirstUtils

@Composable
fun HomeScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    val settings = appData.settings
    val activeId = settings.activeFreezerId
    val activeFreezer = appData.freezers.firstOrNull { it.id == activeId }
        ?: appData.freezers.firstOrNull()
    val soon = settings.soonThresholdDays

    val statusOf: (FrozenProduct) -> FrozenProductStatus = { p ->
        StatusUtils.statusOf(p, soon)
    }

    ScreenScaffold(
        title = "Coldstock",
        nav = nav,
        showBottomBar = true,
        floatingActionButton = {
            if (activeFreezer != null) {
                ExtendedFloatingActionButton(
                    onClick = { nav.navigate(Routes.addProduct(null)) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add Product") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (activeFreezer == null) {
                NoFreezerContent(nav)
                return@Column
            }

            val drawers = appData.drawers.filter { it.freezerId == activeFreezer.id }
                .sortedBy { it.sortOrder }
            val enabledDrawers = drawers.filter { it.enabled }
            val grouped = InventoryUtils.groupByDrawer(appData.products, drawers, activeFreezer.id)
            val activeProducts = InventoryUtils.activeProductsFor(appData.products, activeFreezer.id)

            val okCount = activeProducts.count { statusOf(it) == FrozenProductStatus.Ok }
            val soonCount = activeProducts.count { statusOf(it) == FrozenProductStatus.UseSoon }
            val passedCount = activeProducts.count { statusOf(it) == FrozenProductStatus.Old }
            val noDateCount = activeProducts.count { statusOf(it) == FrozenProductStatus.NoReviewDate }

            val useFirstItems = UseFirstUtils.ordered(appData.products, soon, activeFreezer.id)
                .map { it.product }

            val reminders = ReminderUtils.evaluate(
                appData.products, settings.reminderSettings, soon, activeFreezer.id
            ).filterNot { vm.isReminderDismissed(it.kind.name) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FreezerHeader(
                        appData = appData,
                        activeFreezerName = activeFreezer.name,
                        onSelectFreezer = { vm.setActiveFreezer(it) },
                        onManage = { nav.navigate(Routes.FREEZER_MANAGEMENT) }
                    )
                }

                if (reminders.isNotEmpty()) {
                    item {
                        ReminderCard(
                            messages = reminders.map { it.message },
                            onView = { nav.navigate(Routes.USE_FIRST) },
                            onDismiss = { reminders.forEach { vm.dismissReminder(it.kind.name) } }
                        )
                    }
                }

                item {
                    StatusStrip(okCount, soonCount, passedCount, noDateCount)
                }

                if (activeProducts.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No frozen products recorded.",
                            message = "Add a product manually and place it in a freezer drawer."
                        )
                    }
                }

                items(enabledDrawers, key = { it.id }) { drawer ->
                    val products = grouped[drawer.id].orEmpty()
                    DrawerSection(
                        name = drawer.name,
                        products = products,
                        statusOf = statusOf,
                        enabled = drawer.enabled,
                        onOpenDrawer = { nav.navigate(Routes.drawerDetail(drawer.id)) },
                        onOpenProduct = { nav.navigate(Routes.productDetail(it.id)) },
                        onAddProduct = { nav.navigate(Routes.addProduct(drawer.id)) }
                    )
                }

                val unassigned = grouped[InventoryUtils.UNASSIGNED_ID].orEmpty()
                if (unassigned.isNotEmpty()) {
                    item {
                        DrawerSection(
                            name = InventoryUtils.UNASSIGNED_LABEL,
                            products = unassigned,
                            statusOf = statusOf,
                            enabled = true,
                            onOpenDrawer = { nav.navigate(Routes.ALL_PRODUCTS) },
                            onOpenProduct = { nav.navigate(Routes.productDetail(it.id)) },
                            onAddProduct = { nav.navigate(Routes.addProduct(null)) }
                        )
                    }
                }

                item {
                    UseFirstRail(
                        items = useFirstItems,
                        statusOf = statusOf,
                        onOpenProduct = { nav.navigate(Routes.productDetail(it.id)) },
                        onOpenList = { nav.navigate(Routes.USE_FIRST) }
                    )
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun FreezerHeader(
    appData: AppData,
    activeFreezerName: String,
    onSelectFreezer: (String) -> Unit,
    onManage: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeFreezerName.ifBlank { "Freezer" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = "Choose freezer")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                appData.freezers.forEach { f ->
                    DropdownMenuItem(
                        text = { Text(f.name.ifBlank { "Freezer" } + "  ·  " + f.freezerType.label) },
                        onClick = {
                            onSelectFreezer(f.id)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Manage freezers…") },
                    onClick = {
                        onManage()
                        expanded = false
                    }
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = DateUtils.formatFriendly(Clock.todayIso()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReminderCard(messages: List<String>, onView: () -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f))
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "In-app reminders",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.height(4.dp))
        messages.forEach {
            Text("• $it", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "View Use First",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onView)
            )
            Text(
                text = "Not Now",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        }
    }
}

@Composable
private fun NoFreezerContent(nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No freezer set up yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Create a freezer profile to start recording frozen products " +
                "in drawers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Routes.FREEZER_SETUP) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Set Up Freezer") }
        )
    }
}
