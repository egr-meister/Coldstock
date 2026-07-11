package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.ConfirmDialog
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.navigation.Routes

@Composable
fun FreezerManagementScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    var deleteId by remember { mutableStateOf<String?>(null) }
    val activeId = appData.settings.activeFreezerId

    ScreenScaffold(
        title = "Freezers",
        nav = nav,
        showBack = true,
        actions = {
            IconButton(onClick = { nav.navigate(Routes.FREEZER_SETUP) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add freezer")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (appData.freezers.isEmpty()) {
                item { EmptyState("No freezers", "Add a freezer to get started.") }
            }
            items(appData.freezers, key = { it.id }) { f ->
                val productCount = appData.products.count { it.freezerId == f.id }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            if (f.id == activeId) 2.dp else 1.dp,
                            if (f.id == activeId) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = f.name.ifBlank { "Freezer" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${f.freezerType.label}  ·  $productCount products",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (f.description.isNotBlank()) {
                                Text(
                                    text = f.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (f.id == activeId) {
                            Text(
                                "Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (f.id != activeId) {
                            OutlinedButton(onClick = { vm.setActiveFreezer(f.id) }) { Text("Select") }
                        }
                        OutlinedButton(onClick = { nav.navigate(Routes.freezerEdit(f.id)) }) { Text("Edit") }
                        TextButton(onClick = { deleteId = f.id }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    deleteId?.let { id ->
        ConfirmDialog(
            title = "Delete this freezer?",
            message = "This will also remove its drawers, frozen products, notes, and local history.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = { vm.deleteFreezer(id); deleteId = null },
            onDismiss = { deleteId = null }
        )
    }
}
