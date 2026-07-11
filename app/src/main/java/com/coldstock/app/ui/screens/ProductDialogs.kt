package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coldstock.app.data.AppData
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.util.PortionUtils

private data class Opt(val id: String, val label: String)

@Composable
fun SetPortionsDialog(current: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current.toString()) }
    val parsed = PortionUtils.parse(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set portion count") },
        text = {
            LabeledTextField(
                label = "Portions",
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                numeric = true,
                isError = parsed == null,
                supportingText = if (parsed == null) "Enter a number 0 or greater." else null
            )
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null,
                onClick = { parsed?.let(onConfirm) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MoveProductDialog(
    appData: AppData,
    currentFreezerId: String,
    currentDrawerId: String,
    onConfirm: (freezerId: String, drawerId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var freezerId by remember { mutableStateOf(currentFreezerId) }
    var drawerId by remember { mutableStateOf(currentDrawerId) }

    val freezerOpts = appData.freezers.map { Opt(it.id, it.name.ifBlank { "Freezer" }) }
    val drawerOpts = buildList {
        add(Opt("", "Unassigned Drawer"))
        appData.drawers.filter { it.freezerId == freezerId && it.enabled }
            .sortedBy { it.sortOrder }.forEach { add(Opt(it.id, it.name)) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField(
                    label = "Freezer",
                    selected = freezerOpts.firstOrNull { it.id == freezerId } ?: Opt("", "Select"),
                    options = freezerOpts.ifEmpty { listOf(Opt("", "No freezers")) },
                    optionLabel = { it.label },
                    onSelected = { freezerId = it.id; drawerId = "" }
                )
                DropdownField(
                    label = "Drawer",
                    selected = drawerOpts.firstOrNull { it.id == drawerId } ?: drawerOpts.first(),
                    options = drawerOpts,
                    optionLabel = { it.label },
                    onSelected = { drawerId = it.id }
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(freezerId, drawerId) }) { Text("Move") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MarkUsedDialog(
    currentPortions: Int,
    onConfirm: (note: String, portionsUsed: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf("") }
    var usedText by remember { mutableStateOf(currentPortions.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark this product as used?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This will remove it from the active freezer map and add it to history.")
                LabeledTextField(
                    label = "Portions used (optional)",
                    value = usedText,
                    onValueChange = { usedText = it.filter { c -> c.isDigit() } },
                    numeric = true
                )
                LabeledTextField(
                    label = "Final note (optional)",
                    value = note,
                    onValueChange = { note = it },
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(note, usedText.toIntOrNull()) }) { Text("Mark Used") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MarkDiscardedDialog(onConfirm: (reason: String) -> Unit, onDismiss: () -> Unit) {
    val reasons = listOf(
        "Storage Cleanup", "Quality Concern", "Review Date Passed", "Not Needed", "Other"
    )
    var reason by remember { mutableStateOf(reasons.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark this product as discarded?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This is stored as a manual record.")
                DropdownField(
                    label = "Reason (optional)",
                    selected = reason,
                    options = reasons,
                    optionLabel = { it },
                    onSelected = { reason = it }
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(reason) }) { Text("Mark Discarded") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RestoreProductDialog(
    appData: AppData,
    product: FrozenProduct,
    onConfirm: (freezerId: String, drawerId: String, portions: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var freezerId by remember {
        mutableStateOf(product.freezerId.ifBlank { appData.freezers.firstOrNull()?.id ?: "" })
    }
    var drawerId by remember { mutableStateOf(product.drawerId) }
    var portionText by remember { mutableStateOf(product.portionCount.coerceAtLeast(1).toString()) }
    val parsed = PortionUtils.parse(portionText)

    val freezerOpts = appData.freezers.map { Opt(it.id, it.name.ifBlank { "Freezer" }) }
    val drawerOpts = buildList {
        add(Opt("", "Unassigned Drawer"))
        appData.drawers.filter { it.freezerId == freezerId && it.enabled }
            .sortedBy { it.sortOrder }.forEach { add(Opt(it.id, it.name)) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Restore this product to your active freezer map.")
                DropdownField(
                    label = "Freezer",
                    selected = freezerOpts.firstOrNull { it.id == freezerId } ?: Opt("", "Select"),
                    options = freezerOpts.ifEmpty { listOf(Opt("", "No freezers")) },
                    optionLabel = { it.label },
                    onSelected = { freezerId = it.id; drawerId = "" }
                )
                DropdownField(
                    label = "Drawer",
                    selected = drawerOpts.firstOrNull { it.id == drawerId } ?: drawerOpts.first(),
                    options = drawerOpts,
                    optionLabel = { it.label },
                    onSelected = { drawerId = it.id }
                )
                LabeledTextField(
                    label = "Portions",
                    value = portionText,
                    onValueChange = { portionText = it.filter { c -> c.isDigit() } },
                    numeric = true,
                    isError = parsed == null
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null && freezerId.isNotBlank(),
                onClick = { onConfirm(freezerId, drawerId, parsed ?: 0) }
            ) { Text("Restore") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
