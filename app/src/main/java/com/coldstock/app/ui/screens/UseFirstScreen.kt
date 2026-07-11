package com.coldstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coldstock.app.data.AppData
import com.coldstock.app.ui.ColdstockViewModel
import com.coldstock.app.ui.Disclaimers
import com.coldstock.app.ui.components.EmptyState
import com.coldstock.app.ui.components.InfoBanner
import com.coldstock.app.ui.components.ScreenScaffold
import com.coldstock.app.ui.components.SectionHeader
import com.coldstock.app.ui.navigation.Routes
import com.coldstock.app.util.StatusUtils
import com.coldstock.app.util.UseFirstUtils

@Composable
fun UseFirstScreen(vm: ColdstockViewModel, nav: NavHostController, appData: AppData) {
    val soon = appData.settings.soonThresholdDays
    val activeId = appData.settings.activeFreezerId
    val entries = UseFirstUtils.ordered(appData.products, soon, activeId)

    val bySection = entries.groupBy { it.section }
    val sectionOrder = listOf(
        UseFirstUtils.Section.ReviewDatePassed,
        UseFirstUtils.Section.UseSoon,
        UseFirstUtils.Section.ManuallyPrioritized,
        UseFirstUtils.Section.NoReviewDate,
    )

    ScreenScaffold(title = "Use First", nav = nav, showBottomBar = true) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { InfoBanner(Disclaimers.STORAGE_PERIOD) }

            if (entries.isEmpty()) {
                item {
                    EmptyState(
                        title = "Use First list is empty",
                        message = "Items appear here when they reach their review date, " +
                            "enter the Use Soon period, or you mark them Use First."
                    )
                }
            }

            sectionOrder.forEach { section ->
                val items = bySection[section].orEmpty()
                if (items.isNotEmpty()) {
                    item { SectionHeader(UseFirstUtils.sectionLabel(section)) }
                    items(items.map { it.product }, key = { it.id }) { p ->
                        ProductRow(
                            product = p,
                            status = StatusUtils.statusOf(p, soon),
                            drawerName = appData.drawers.firstOrNull { it.id == p.drawerId }?.name,
                            onClick = { nav.navigate(Routes.productDetail(p.id)) }
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
