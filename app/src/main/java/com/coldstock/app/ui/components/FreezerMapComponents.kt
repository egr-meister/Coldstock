package com.coldstock.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.ui.theme.DrawerEdge
import com.coldstock.app.ui.theme.DrawerGlass
import com.coldstock.app.ui.theme.DrawerShadow
import com.coldstock.app.ui.theme.HandleGray
import com.coldstock.app.ui.theme.OkTeal
import com.coldstock.app.ui.theme.ReviewPassedRed
import com.coldstock.app.ui.theme.UseFirstViolet
import com.coldstock.app.ui.theme.UseSoonAmber
import com.coldstock.app.ui.theme.statusColor
import com.coldstock.app.util.DateUtils
import com.coldstock.app.util.PortionUtils

/** A single frozen-product tab shown inside a drawer. */
@Composable
fun FrozenProductTab(
    product: FrozenProduct,
    status: FrozenProductStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = statusColor(status)
    val dateLabel = DateUtils.plannedReviewDateIso(
        product.freezingDate, product.storageDurationValue, product.storageDurationUnit
    )?.let { "Review " + DateUtils.formatFriendly(it) }
        ?: ("Frozen " + DateUtils.formatFriendly(product.freezingDate))

    Column(
        modifier = modifier
            .widthIn(min = 132.dp, max = 168.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, DrawerEdge.copy(alpha = 0.6f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${product.name}, ${status.label}, " +
                    PortionUtils.describe(product.portionCount, product.portionLabel) +
                    if (product.useFirst) ", Use First" else ""
            }
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = product.name.ifBlank { "Unnamed product" },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (product.useFirst) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.labelMedium,
                    color = UseFirstViolet,
                    modifier = Modifier.semantics { contentDescription = "Use First" }
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = PortionUtils.describe(product.portionCount, product.portionLabel),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The pull-out handle of a drawer, showing status dots on the right. */
@Composable
fun DrawerHandle(
    name: String,
    productCount: Int,
    useSoonCount: Int,
    passedCount: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(
                Brush.verticalGradient(listOf(DrawerGlass, DrawerGlass.copy(alpha = 0.75f)))
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Physical handle notch.
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(HandleGray)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (enabled) name else "$name (disabled)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = DrawerShadow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (productCount == 1) "1 product" else "$productCount products",
                style = MaterialTheme.typography.labelSmall,
                color = DrawerShadow.copy(alpha = 0.8f)
            )
        }
        if (useSoonCount > 0) {
            HandleDot(count = useSoonCount, colorHex = UseSoonAmber, label = "Use Soon")
            Spacer(Modifier.width(6.dp))
        }
        if (passedCount > 0) {
            HandleDot(count = passedCount, colorHex = ReviewPassedRed, label = "Review Date Passed")
        }
    }
}

@Composable
private fun HandleDot(count: Int, colorHex: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorHex.copy(alpha = 0.18f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
            .semantics { contentDescription = "$count $label" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(colorHex)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = colorHex
        )
    }
}

/** One drawer compartment: handle + horizontally scrollable product tabs. */
@Composable
fun DrawerSection(
    name: String,
    products: List<FrozenProduct>,
    statusOf: (FrozenProduct) -> FrozenProductStatus,
    enabled: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenProduct: (FrozenProduct) -> Unit,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier,
    maxTabs: Int = 8
) {
    val useSoon = products.count { statusOf(it) == FrozenProductStatus.UseSoon }
    val passed = products.count { statusOf(it) == FrozenProductStatus.Old }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DrawerGlass.copy(alpha = 0.35f))
            .border(1.dp, DrawerEdge.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.clickable(onClick = onOpenDrawer)) {
            DrawerHandle(
                name = name,
                productCount = products.size,
                useSoonCount = useSoon,
                passedCount = passed,
                enabled = enabled
            )
        }

        if (products.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Empty drawer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Add Product",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onAddProduct)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                products.take(maxTabs).forEach { p ->
                    FrozenProductTab(product = p, status = statusOf(p), onClick = { onOpenProduct(p) })
                }
                if (products.size > maxTabs) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 60.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onOpenDrawer)
                            .padding(horizontal = 14.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View All\n(${products.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/** Compact strip summarising OK / Use Soon / Review Date Passed counts. */
@Composable
fun StatusStrip(
    okCount: Int,
    useSoonCount: Int,
    passedCount: Int,
    noDateCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, DrawerEdge.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusStripItem(okCount, "OK", OkTeal)
        StatusStripItem(useSoonCount, "Use Soon", UseSoonAmber)
        StatusStripItem(passedCount, "Passed", ReviewPassedRed)
        StatusStripItem(noDateCount, "No Date", HandleGray)
    }
}

@Composable
private fun StatusStripItem(count: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Narrow Use First rail shown beneath the cabinet, as small tickets. */
@Composable
fun UseFirstRail(
    items: List<FrozenProduct>,
    statusOf: (FrozenProduct) -> FrozenProductStatus,
    onOpenProduct: (FrozenProduct) -> Unit,
    onOpenList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(UseFirstViolet.copy(alpha = 0.10f))
            .border(1.dp, UseFirstViolet.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Use First",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = UseFirstViolet
            )
            Text(
                text = "Open list",
                style = MaterialTheme.typography.labelMedium,
                color = UseFirstViolet,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onOpenList)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        if (items.isEmpty()) {
            Text(
                text = "No items marked to use first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.take(10).forEach { p ->
                    UseFirstTicket(p, statusOf(p)) { onOpenProduct(p) }
                }
            }
        }
    }
}

@Composable
private fun UseFirstTicket(product: FrozenProduct, status: FrozenProductStatus, onClick: () -> Unit) {
    val color = statusColor(status)
    Column(
        modifier = Modifier
            .widthIn(min = 120.dp, max = 150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "${product.name}, ${status.label}, Use First" }
            .padding(8.dp)
    ) {
        Text(
            text = product.name.ifBlank { "Unnamed product" },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
