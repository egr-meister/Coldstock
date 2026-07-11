package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.model.FrozenFoodCategory
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.Disclaimers
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.util.Clock
import com.coldstock.app.util.DateUtils
import com.coldstock.app.util.PortionUtils
import com.coldstock.app.util.ValidationUtils

private data class FreezerOption(val id: String, val label: String)
private data class DrawerOption(val id: String, val label: String)

@Composable
fun AddEditProductScreen(
    vm: ColdstockViewModel,
    nav: NavHostController,
    productId: String?,
    presetDrawerId: String?,
    appData: AppData
) {
    val editing = productId?.let { vm.product(it) }

    if (productId != null && editing == null) {
        NotFoundScreen(title = "Edit Product", message = "Product not found", nav = nav)
        return
    }

    val settings = appData.settings
    val defaultFreezerId = editing?.freezerId
        ?: settings.activeFreezerId
        ?: appData.freezers.firstOrNull()?.id
        ?: ""

    var name by remember { mutableStateOf(editing?.name ?: "") }
    var freezerId by remember { mutableStateOf(defaultFreezerId) }
    var drawerId by remember {
        mutableStateOf(editing?.drawerId ?: presetDrawerId ?: "")
    }
    var category by remember { mutableStateOf(editing?.category ?: FrozenFoodCategory.Other) }
    var customCategory by remember { mutableStateOf(editing?.customCategoryName ?: "") }
    var freezingDate by remember { mutableStateOf(editing?.freezingDate?.ifBlank { Clock.todayIso() } ?: Clock.todayIso()) }
    var durationText by remember {
        mutableStateOf(editing?.storageDurationValue?.toString() ?: "")
    }
    var durationUnit by remember {
        mutableStateOf(editing?.storageDurationUnit ?: settings.defaultDurationUnit)
    }
    var portionText by remember { mutableStateOf((editing?.portionCount ?: 1).toString()) }
    var portionLabel by remember { mutableStateOf(editing?.portionLabel ?: "") }
    var useFirst by remember { mutableStateOf(editing?.useFirst ?: false) }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var errors by remember { mutableStateOf(ValidationUtils.ProductFormError()) }

    val freezerOptions = appData.freezers.map { FreezerOption(it.id, it.name.ifBlank { "Freezer" }) }
    val drawerOptions: List<DrawerOption> = buildList {
        add(DrawerOption("", "Unassigned Drawer"))
        appData.drawers.filter { it.freezerId == freezerId && it.enabled }
            .sortedBy { it.sortOrder }
            .forEach { add(DrawerOption(it.id, it.name)) }
    }

    val reviewPreview = DateUtils.plannedReviewDateIso(
        freezingDate, durationText.trim().toIntOrNull(), durationUnit
    )

    val title = if (editing != null) "Edit Product" else "Add Product"

    ScreenScaffold(title = title, nav = nav, showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoBanner(Disclaimers.DURATION_INLINE)

            LabeledTextField(
                label = "Product name",
                value = name,
                onValueChange = {
                    name = it
                    if (vm.previousDurationFor(it) != null && durationText.isBlank()) {
                        vm.previousDurationFor(it)?.let { (v, u) ->
                            durationText = v.toString(); durationUnit = u
                        }
                    }
                },
                isError = errors.name != null,
                supportingText = errors.name
            )

            DropdownField(
                label = "Freezer",
                selected = freezerOptions.firstOrNull { it.id == freezerId }
                    ?: FreezerOption("", "Select a freezer"),
                options = freezerOptions.ifEmpty { listOf(FreezerOption("", "No freezers")) },
                optionLabel = { it.label },
                onSelected = { freezerId = it.id; drawerId = "" },
                isError = errors.freezer != null,
                supportingText = errors.freezer
            )

            DropdownField(
                label = "Drawer",
                selected = drawerOptions.firstOrNull { it.id == drawerId }
                    ?: drawerOptions.first(),
                options = drawerOptions,
                optionLabel = { it.label },
                onSelected = { drawerId = it.id },
                supportingText = errors.drawer
            )

            DropdownField(
                label = "Category",
                selected = category,
                options = FrozenFoodCategory.entries,
                optionLabel = { it.label },
                onSelected = { category = it }
            )

            if (category == FrozenFoodCategory.Other) {
                LabeledTextField(
                    label = "Custom category (optional)",
                    value = customCategory,
                    onValueChange = { customCategory = it }
                )
            }

            SectionHeader("Freezing date")
            LabeledTextField(
                label = "Freezing date (YYYY-MM-DD)",
                value = freezingDate,
                onValueChange = { freezingDate = it },
                isError = errors.freezingDate != null,
                supportingText = errors.freezingDate ?: "Format: 2026-07-10"
            )
            OutlinedButton(onClick = { freezingDate = Clock.todayIso() }) {
                Text("Use today (${DateUtils.formatFriendly(Clock.todayIso())})")
            }

            SectionHeader("Storage duration (entered by you)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LabeledTextField(
                    label = "Duration",
                    value = durationText,
                    onValueChange = { durationText = it.filter { c -> c.isDigit() } },
                    numeric = true,
                    isError = errors.duration != null,
                    supportingText = errors.duration,
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    label = "Unit",
                    selected = durationUnit,
                    options = StorageDurationUnit.entries,
                    optionLabel = { it.label },
                    onSelected = { durationUnit = it },
                    modifier = Modifier.weight(1f)
                )
            }
            ReviewPreviewRow(reviewPreview)

            SectionHeader("Portions")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LabeledTextField(
                    label = "Portion count",
                    value = portionText,
                    onValueChange = { portionText = it.filter { c -> c.isDigit() } },
                    numeric = true,
                    isError = errors.portions != null,
                    supportingText = errors.portions,
                    modifier = Modifier.weight(1f)
                )
                LabeledTextField(
                    label = "Portion label (optional)",
                    value = portionLabel,
                    onValueChange = { portionLabel = it },
                    placeholder = "bags, containers…",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Use First", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Manually prioritize this item on your Use First list.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = useFirst, onCheckedChange = { useFirst = it })
            }

            LabeledTextField(
                label = "Note (optional)",
                value = note,
                onValueChange = { note = it },
                singleLine = false
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val validation = ValidationUtils.validateProduct(
                        name = name,
                        freezerId = freezerId,
                        drawerId = drawerId,
                        allowUnassignedDrawer = true,
                        freezingDate = freezingDate,
                        durationText = durationText,
                        durationUnit = durationUnit,
                        portionText = portionText
                    )
                    errors = validation
                    if (validation.hasError) return@Button

                    val durationValue = durationText.trim().toIntOrNull()
                    val product = (editing ?: FrozenProduct()).copy(
                        name = name.trim(),
                        freezerId = freezerId,
                        drawerId = drawerId,
                        category = category,
                        customCategoryName = if (category == FrozenFoodCategory.Other) customCategory.trim() else "",
                        freezingDate = freezingDate.trim(),
                        storageDurationValue = if (durationValue != null && durationValue > 0) durationValue else null,
                        storageDurationUnit = if (durationValue != null && durationValue > 0) durationUnit else null,
                        portionCount = PortionUtils.parse(portionText) ?: 0,
                        portionLabel = portionLabel.trim(),
                        useFirst = useFirst,
                        note = note.trim()
                    )

                    if (editing != null) {
                        vm.updateProduct(product)
                        nav.popBackStack()
                    } else {
                        vm.addProduct(product) { nav.popBackStack() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (editing != null) "Save Changes" else "Add Product") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReviewPreviewRow(reviewPreview: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Calculated review date",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = reviewPreview?.let { DateUtils.formatFriendly(it) } ?: "No review date",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
