package com.coldstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.coldstock.app.model.FrozenProduct
import com.coldstock.app.model.FrozenProductStatus
import com.coldstock.app.ui.components.StatusChip
import com.coldstock.app.ui.theme.UseFirstViolet
import com.coldstock.app.util.DateUtils
import com.coldstock.app.util.PortionUtils

/** Reusable compact product list row used across list-style screens. */
@Composable
fun ProductRow(
    product: FrozenProduct,
    status: FrozenProductStatus,
    drawerName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (product.useFirst) {
                    Text(
                        "★ ",
                        color = UseFirstViolet,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = product.name.ifBlank { "Unnamed product" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusChip(status)
        }
        Text(
            text = buildString {
                append(PortionUtils.describe(product.portionCount, product.portionLabel))
                append("  ·  ")
                append(product.categoryDisplay)
                if (!drawerName.isNullOrBlank()) { append("  ·  "); append(drawerName) }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val review = DateUtils.plannedReviewDateIso(
            product.freezingDate, product.storageDurationValue, product.storageDurationUnit
        )
        Text(
            text = if (review != null) "Review: ${DateUtils.formatFriendly(review)}"
            else "Frozen: ${DateUtils.formatFriendly(product.freezingDate)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
