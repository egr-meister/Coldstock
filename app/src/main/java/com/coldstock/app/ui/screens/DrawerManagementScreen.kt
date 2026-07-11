package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.coldstock.app.model.DrawerType
import com.coldstock.app.model.FreezerDrawer
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.ConfirmDialog
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.ui.components.ScreenScaffold

private data class DOpt(val id: String, val label: String)

@Composable
fun DrawerManagementScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    val activeId = appData.settings.activeFreezerId
    val freezer = appData.freezers.firstOrNull { it.id == activeId }
    if (freezer == null) {
        NotFoundScreen("Drawers", "Freezer not found", nav)
        return
    }
    val drawers = appData.drawers.filter { it.freezerId == freezer.id }.sortedBy { it.sortOrder }

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FreezerDrawer?>(null) }
    var deleteTarget by remember { mutableStateOf<FreezerDrawer?>(null) }
    var restoreDefaults by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Drawers · ${freezer.name}",
        nav = nav,
        showBack = true
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showAdd = true }) { Text("Add Drawer") }
                    OutlinedButton(onClick = { restoreDefaults = true }) { Text("Restore Defaults") }
                }
            }
            if (drawers.isEmpty()) {
                item { EmptyState("No drawers", "Add a drawer or restore the default layout.") }
            }
            itemsIndexed(drawers, key = { _, d -> d.id }) { index, d ->
                val count = appData.products.count {
                    it.drawerId == d.id && it.lifecycleState == com.coldstock.app.model.ProductLifecycleState.Active
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (d.enabled) d.name else "${d.name} (disabled)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${d.drawerType.label}  ·  $count products",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { vm.moveDrawer(freezer.id, d.id, up = true) },
                        enabled = index > 0
                    ) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up") }
                    IconButton(
                        onClick = { vm.moveDrawer(freezer.id, d.id, up = false) },
                        enabled = index < drawers.lastIndex
                    ) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { editing = d }) { Text("Rename") }
                    TextButton(onClick = {
                        if (count > 0 && d.enabled) deleteTarget = d
                        else vm.setDrawerEnabled(d.id, !d.enabled)
                    }) {
                        Text(if (d.enabled) "Disable" else "Enable")
                    }
                    TextButton(onClick = { deleteTarget = d }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showAdd) {
        DrawerEditDialog(
            title = "Add Drawer",
            initialName = "",
            initialType = DrawerType.Drawer,
            initialNote = "",
            onConfirm = { name, type, note ->
                vm.addDrawer(freezer.id, name, type, note); showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }

    editing?.let { d ->
        DrawerEditDialog(
            title = "Rename / Edit Drawer",
            initialName = d.name,
            initialType = d.drawerType,
            initialNote = d.note,
            onConfirm = { name, type, note ->
                vm.editDrawer(d.id, name, type, note); editing = null
            },
            onDismiss = { editing = null }
        )
    }

    deleteTarget?.let { d ->
        val productsInDrawer = appData.products.filter {
            it.drawerId == d.id && it.lifecycleState == com.coldstock.app.model.ProductLifecycleState.Active
        }
        if (productsInDrawer.isEmpty()) {
            ConfirmDialog(
                title = "Delete drawer?",
                message = "This drawer is empty and will be removed.",
                confirmLabel = "Delete",
                destructive = true,
                onConfirm = { vm.deleteDrawer(d.id, null); deleteTarget = null },
                onDismiss = { deleteTarget = null }
            )
        } else {
            ReassignDeleteDialog(
                drawer = d,
                otherDrawers = drawers.filter { it.id != d.id && it.enabled },
                productCount = productsInDrawer.size,
                onConfirm = { targetId ->
                    vm.deleteDrawer(d.id, targetId); deleteTarget = null
                },
                onDismiss = { deleteTarget = null }
            )
        }
    }

    if (restoreDefaults) {
        ConfirmDialog(
            title = "Restore default drawers?",
            message = "Missing default drawers for this freezer type will be added. " +
                "Existing drawers and products are kept.",
            confirmLabel = "Restore",
            onConfirm = { vm.restoreDefaultDrawers(freezer.id); restoreDefaults = false },
            onDismiss = { restoreDefaults = false }
        )
    }
}

@Composable
private fun DrawerEditDialog(
    title: String,
    initialName: String,
    initialType: DrawerType,
    initialNote: String,
    onConfirm: (String, DrawerType, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var note by remember { mutableStateOf(initialNote) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledTextField(label = "Drawer name", value = name, onValueChange = { name = it })
                DropdownField(
                    label = "Type",
                    selected = type,
                    options = DrawerType.entries,
                    optionLabel = { it.label },
                    onSelected = { type = it }
                )
                LabeledTextField(
                    label = "Note (optional)",
                    value = note,
                    onValueChange = { note = it },
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.trim().isNotEmpty(),
                onClick = { onConfirm(name, type, note) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ReassignDeleteDialog(
    drawer: FreezerDrawer,
    otherDrawers: List<FreezerDrawer>,
    productCount: Int,
    onConfirm: (targetDrawerId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = buildList {
        add(DOpt("", "Unassigned Drawer"))
        otherDrawers.forEach { add(DOpt(it.id, it.name)) }
    }
    var target by remember { mutableStateOf(options.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete drawer with products?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This drawer holds $productCount product(s). Choose where to move " +
                        "them. Products are never deleted."
                )
                DropdownField(
                    label = "Move products to",
                    selected = target,
                    options = options,
                    optionLabel = { it.label },
                    onSelected = { target = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(target.id.ifBlank { null }) }) {
                Text("Move & Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
