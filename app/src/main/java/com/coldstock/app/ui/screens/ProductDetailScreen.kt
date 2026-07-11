package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.model.ProductLifecycleState
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.Disclaimers
import com.coldstock.app.ui.components.ConfirmDialog
import com.coldstock.app.ui.components.DetailRow
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.ui.components.StatusChip
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.DateUtils
import com.coldstock.app.util.PortionUtils
import com.coldstock.app.util.StatusUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    vm: ColdstockViewModel,
    nav: NavHostController,
    productId: String?,
    appData: AppData
) {
    val product = appData.products.firstOrNull { it.id == productId }
    if (product == null) {
        NotFoundScreen("Product", "Product not found", nav)
        return
    }

    val soon = appData.settings.soonThresholdDays
    val status = StatusUtils.statusOf(product, soon)
    val freezer = appData.freezers.firstOrNull { it.id == product.freezerId }
    val drawer = appData.drawers.firstOrNull { it.id == product.drawerId }
    val review = DateUtils.plannedReviewDateIso(
        product.freezingDate, product.storageDurationValue, product.storageDurationUnit
    )

    var showSetPortions by remember { mutableStateOf(false) }
    var showMove by remember { mutableStateOf(false) }
    var showMarkUsed by remember { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }
    var showRestore by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showZeroPortion by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = product.name.ifBlank { "Product" },
        nav = nav,
        showBack = true,
        actions = {
            if (product.lifecycleState == ProductLifecycleState.Active) {
                IconButton(onClick = { nav.navigate(Routes.editProduct(product.id)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                StatusChip(status)
                if (product.useFirst) {
                    Spacer(Modifier.width(8.dp))
                    Text("★ Use First", color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(6.dp))

            DetailRow("Freezer", freezer?.name ?: "Freezer not found")
            DetailRow("Drawer", drawer?.name ?: "Unassigned Drawer")
            DetailRow("Category", product.categoryDisplay)
            DetailRow("Freezing date", DateUtils.formatFriendly(product.freezingDate))
            DetailRow(
                "Storage duration",
                if (product.storageDurationValue != null && product.storageDurationUnit != null)
                    "${product.storageDurationValue} ${product.storageDurationUnit.label}"
                else "Not entered"
            )
            DetailRow("Planned review date", review?.let { DateUtils.formatFriendly(it) } ?: "No review date")
            DetailRow("Status", status.label)
            DetailRow("Portions", PortionUtils.describe(product.portionCount, product.portionLabel))
            DetailRow("Use First", if (product.useFirst) "Yes" else "No")
            DetailRow("Note", product.note.ifBlank { "—" })
            DetailRow("Created", DateUtils.formatFriendly(product.createdAt.take(10)))
            DetailRow("Updated", DateUtils.formatFriendly(product.updatedAt.take(10)))

            Spacer(Modifier.height(6.dp))
            InfoBanner(Disclaimers.DATES_INLINE)
            Spacer(Modifier.height(10.dp))

            if (product.lifecycleState == ProductLifecycleState.Active) {
                SectionHeader("Portions")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (product.portionCount <= 1) {
                                vm.adjustPortions(product.id, -1)
                                if (product.portionCount - 1 <= 0) showZeroPortion = true
                            } else vm.adjustPortions(product.id, -1)
                        }
                    ) { Text("Use One") }
                    OutlinedButton(onClick = { vm.adjustPortions(product.id, +1) }) { Text("Add One") }
                    OutlinedButton(onClick = { showSetPortions = true }) { Text("Set Quantity") }
                }

                SectionHeader("Actions")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.toggleUseFirst(product.id, !product.useFirst) }) {
                        Text(if (product.useFirst) "Remove Use First" else "Mark Use First")
                    }
                    OutlinedButton(onClick = { showMove = true }) { Text("Move") }
                    OutlinedButton(onClick = { showMarkUsed = true }) { Text("Mark Used") }
                    OutlinedButton(onClick = { showDiscard = true }) { Text("Mark Discarded") }
                }
            } else {
                SectionHeader("Actions")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showRestore = true }) { Text("Restore Product") }
                }
            }

            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { showDelete = true }) {
                Text("Delete product", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showSetPortions) {
        SetPortionsDialog(
            current = product.portionCount,
            onConfirm = { vm.setPortionCount(product.id, it); showSetPortions = false },
            onDismiss = { showSetPortions = false }
        )
    }

    if (showZeroPortion) {
        ConfirmDialog(
            title = "No portions remain",
            message = "This product now has no portions. Keep it active, or mark it used?",
            confirmLabel = "Mark Used",
            dismissLabel = "Keep Active",
            onConfirm = { vm.markUsed(product.id); showZeroPortion = false; nav.popBackStack() },
            onDismiss = { showZeroPortion = false }
        )
    }

    if (showMove) {
        MoveProductDialog(
            appData = appData,
            currentFreezerId = product.freezerId,
            currentDrawerId = product.drawerId,
            onConfirm = { fId, dId -> vm.moveProduct(product.id, fId, dId); showMove = false },
            onDismiss = { showMove = false }
        )
    }

    if (showMarkUsed) {
        MarkUsedDialog(
            currentPortions = product.portionCount,
            onConfirm = { note, used -> vm.markUsed(product.id, note, used); showMarkUsed = false; nav.popBackStack() },
            onDismiss = { showMarkUsed = false }
        )
    }

    if (showDiscard) {
        MarkDiscardedDialog(
            onConfirm = { reason -> vm.markDiscarded(product.id, reason); showDiscard = false; nav.popBackStack() },
            onDismiss = { showDiscard = false }
        )
    }

    if (showRestore) {
        RestoreProductDialog(
            appData = appData,
            product = product,
            onConfirm = { fId, dId, portions ->
                vm.restoreProduct(product.id, fId, dId, portions); showRestore = false
            },
            onDismiss = { showRestore = false }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete product?",
            message = "This permanently removes the product from Coldstock.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = { vm.deleteProduct(product.id); showDelete = false; nav.popBackStack() },
            onDismiss = { showDelete = false }
        )
    }
}

