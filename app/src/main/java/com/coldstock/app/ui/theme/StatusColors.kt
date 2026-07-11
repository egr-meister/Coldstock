package com.coldstock.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.coldstock.app.model.FrozenProductStatus

/** Maps a derived product status to its palette color. Never color-only in UI. */
fun statusColor(status: FrozenProductStatus): Color = when (status) {
    FrozenProductStatus.Ok -> OkTeal
    FrozenProductStatus.UseSoon -> UseSoonAmber
    FrozenProductStatus.Old -> ReviewPassedRed
    FrozenProductStatus.NoReviewDate -> NoReviewGray
    FrozenProductStatus.Used -> UsedBlueGray
    FrozenProductStatus.Discarded -> DiscardedCharcoal
}
