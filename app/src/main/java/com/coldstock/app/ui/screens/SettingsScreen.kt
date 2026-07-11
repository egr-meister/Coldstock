package com.coldstock.app.ui.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.coldstock.app.model.StorageDurationUnit
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.Disclaimers
import com.coldstock.app.ui.components.ConfirmDialog
import com.coldstock.app.ui.components.DropdownField
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.StatusUtils

private data class FreezerChoice(val id: String?, val label: String)

@Composable
fun SettingsScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    val settings = appData.settings
    val reminders = settings.reminderSettings

    var confirm by remember { mutableStateOf<ConfirmAction?>(null) }

    val freezerChoices = appData.freezers.map { FreezerChoice(it.id, it.name.ifBlank { "Freezer" }) }
        .ifEmpty { listOf(FreezerChoice(null, "No freezers")) }

    ScreenScaffold(title = "Settings", nav = nav, showBottomBar = true) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Active freezer & inventory
            item { SectionHeader("Freezer") }
            item {
                DropdownField(
                    label = "Active freezer",
                    selected = freezerChoices.firstOrNull { it.id == settings.activeFreezerId }
                        ?: freezerChoices.first(),
                    options = freezerChoices,
                    optionLabel = { it.label },
                    onSelected = { it.id?.let(vm::setActiveFreezer) }
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { nav.navigate(Routes.FREEZER_MANAGEMENT) }) { Text("Freezers") }
                    OutlinedButton(onClick = { nav.navigate(Routes.DRAWER_MANAGEMENT) }) { Text("Drawers") }
                    OutlinedButton(onClick = { nav.navigate(Routes.STATISTICS) }) { Text("Statistics") }
                }
            }

            // Review behaviour
            item { SectionHeader("Review behaviour") }
            item {
                DropdownField(
                    label = "Use Soon threshold",
                    selected = settings.soonThresholdDays,
                    options = StatusUtils.soonThresholdOptions,
                    optionLabel = { "$it day(s)" },
                    onSelected = { vm.setSoonThreshold(it) }
                )
            }
            item {
                DropdownField(
                    label = "Default duration unit",
                    selected = settings.defaultDurationUnit,
                    options = StorageDurationUnit.entries,
                    optionLabel = { it.label },
                    onSelected = { vm.setDefaultDurationUnit(it) }
                )
            }
            item {
                SettingSwitch(
                    title = "Use previous manual value",
                    subtitle = "Remember the last duration entered for the same product name.",
                    checked = settings.rememberPreviousDurations,
                    onCheckedChange = { vm.setRememberDurations(it) }
                )
            }

            // Reminders
            item { SectionHeader("In-app reminders") }
            item { InfoBanner(Disclaimers.REMINDERS) }
            item {
                SettingSwitch(
                    title = "Enable in-app reminders",
                    subtitle = null,
                    checked = reminders.enabled,
                    onCheckedChange = { on -> vm.setReminders { it.copy(enabled = on) } }
                )
            }
            item {
                SettingSwitch(
                    title = "Use Soon reminder",
                    subtitle = null,
                    checked = reminders.showUseSoon,
                    enabled = reminders.enabled,
                    onCheckedChange = { on -> vm.setReminders { it.copy(showUseSoon = on) } }
                )
            }
            item {
                SettingSwitch(
                    title = "Review date passed reminder",
                    subtitle = null,
                    checked = reminders.showReviewDatePassed,
                    enabled = reminders.enabled,
                    onCheckedChange = { on -> vm.setReminders { it.copy(showReviewDatePassed = on) } }
                )
            }
            item {
                SettingSwitch(
                    title = "No review date reminder",
                    subtitle = null,
                    checked = reminders.showNoReviewDate,
                    enabled = reminders.enabled,
                    onCheckedChange = { on -> vm.setReminders { it.copy(showNoReviewDate = on) } }
                )
            }
            item {
                SettingSwitch(
                    title = "Low portion reminder",
                    subtitle = "When an item has one portion remaining.",
                    checked = reminders.showLowPortions,
                    enabled = reminders.enabled,
                    onCheckedChange = { on -> vm.setReminders { it.copy(showLowPortions = on) } }
                )
            }

            // Onboarding & layout
            item { SectionHeader("App") }
            item {
                OutlinedButton(
                    onClick = { vm.setShowOnboardingAgain(); nav.navigate(Routes.ONBOARDING) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Show onboarding again") }
            }
            item {
                OutlinedButton(
                    onClick = {
                        settings.activeFreezerId?.let { vm.restoreDefaultDrawers(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = settings.activeFreezerId != null
                ) { Text("Restore default drawers") }
            }

            // Data maintenance
            item { SectionHeader("Data") }
            item {
                DangerButton("Delete Used history") { confirm = ConfirmAction.DeleteUsedHistory }
            }
            item {
                DangerButton("Delete Discarded history") { confirm = ConfirmAction.DeleteDiscardedHistory }
            }
            item {
                DangerButton("Delete all active products") { confirm = ConfirmAction.DeleteAllActive }
            }
            item {
                DangerButton("Delete active freezer") { confirm = ConfirmAction.DeleteActiveFreezer }
            }
            item {
                DangerButton("Reset all local data") { confirm = ConfirmAction.ResetAll }
            }

            // Disclaimers & privacy
            item { SectionHeader("About & disclaimers") }
            item { InfoBanner(Disclaimers.MANUAL_TRACKING) }
            item { InfoBanner(Disclaimers.STORAGE_PERIOD) }
            item { InfoBanner(Disclaimers.PRIVACY) }
            item {
                Text(
                    "Coldstock 1.0.0 — a fully offline manual freezer inventory " +
                        "organizer. No account, no cloud, no internet, no ads, no analytics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    confirm?.let { action ->
        val (title, message, destructive) = action.dialog()
        ConfirmDialog(
            title = title,
            message = message,
            confirmLabel = "Confirm",
            destructive = destructive,
            onConfirm = {
                when (action) {
                    ConfirmAction.DeleteUsedHistory -> vm.deleteUsedHistory()
                    ConfirmAction.DeleteDiscardedHistory -> vm.deleteDiscardedHistory()
                    ConfirmAction.DeleteAllActive -> vm.deleteAllActiveProducts()
                    ConfirmAction.DeleteActiveFreezer ->
                        settings.activeFreezerId?.let { vm.deleteFreezer(it) }
                    ConfirmAction.ResetAll -> vm.resetAllData()
                }
                confirm = null
            },
            onDismiss = { confirm = null }
        )
    }
}

private enum class ConfirmAction {
    DeleteUsedHistory, DeleteDiscardedHistory, DeleteAllActive, DeleteActiveFreezer, ResetAll;

    fun dialog(): Triple<String, String, Boolean> = when (this) {
        DeleteUsedHistory -> Triple("Delete Used history?",
            "This removes all 'Marked used' history events. Products are not changed.", true)
        DeleteDiscardedHistory -> Triple("Delete Discarded history?",
            "This removes all 'Marked discarded' history events. Products are not changed.", true)
        DeleteAllActive -> Triple("Delete all active products?",
            "This permanently removes every active product from the current data.", true)
        DeleteActiveFreezer -> Triple("Delete this freezer?",
            "This will also remove its drawers, frozen products, notes, and local history.", true)
        ResetAll -> Triple("Reset all local data?",
            "This will permanently remove every freezer, drawer, frozen product, portion " +
                "count, date, note, Use First record, history event, and setting stored by Coldstock.", true)
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String?,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun DangerButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.error)
    }
}
