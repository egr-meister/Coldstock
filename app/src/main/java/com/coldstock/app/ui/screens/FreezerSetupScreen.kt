package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.coldstock.app.data.DefaultDrawers
import com.coldstock.app.model.FreezerType
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.LabeledTextField
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.ui.navigation.Routes

@Composable
fun FreezerSetupScreen(
    vm: ColdstockViewModel,
    nav: NavHostController,
    freezerId: String?
) {
    val editing = freezerId?.let { vm.freezer(it) }
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var type by remember { mutableStateOf(editing?.freezerType ?: FreezerType.Upright) }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var nameError by remember { mutableStateOf(false) }

    val title = if (editing != null) "Edit Freezer" else "Set Up Freezer"

    ScreenScaffold(title = title, nav = nav, showBack = true) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabeledTextField(
                label = "Freezer name",
                value = name,
                onValueChange = { name = it; nameError = false },
                isError = nameError,
                supportingText = if (nameError) "Freezer name is required." else null,
                placeholder = "e.g. Kitchen Freezer"
            )

            DropdownField(
                label = "Freezer type",
                selected = type,
                options = FreezerType.entries,
                optionLabel = { it.label },
                onSelected = { type = it }
            )

            LabeledTextField(
                label = "Description (optional)",
                value = description,
                onValueChange = { description = it },
                singleLine = false
            )

            SectionHeader("Default drawer layout")
            DefaultLayoutPreview(type)

            InfoBanner(
                "Default drawer names are organizational only. You can rename, " +
                    "reorder, add, or disable drawers at any time."
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        nameError = true
                        return@Button
                    }
                    if (editing != null) {
                        vm.editFreezer(editing.id, name, type, description)
                        nav.popBackStack()
                    } else {
                        vm.addFreezer(name, type, description) {
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (editing != null) "Save Changes" else "Create Freezer") }
        }
    }
}

@Composable
private fun DefaultLayoutPreview(type: FreezerType) {
    // Preview default names without persisting anything.
    val names = DefaultDrawers.buildFor("preview", type).map { it.name }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        names.forEach { n ->
            Text(
                text = "▤  $n",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
