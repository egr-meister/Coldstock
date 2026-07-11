package com.coldstock.app.util

import com.coldstock.app.model.ProductHistoryEvent
import com.coldstock.app.model.ProductHistoryEventType
import java.time.YearMonth

/** Grouping/filtering helpers for product history. Neutral, no judgements. */
object HistoryUtils {

    enum class Filter { All, Used, Discarded, PortionChanges, Moves }

    fun apply(events: List<ProductHistoryEvent>, filter: Filter): List<ProductHistoryEvent> =
        when (filter) {
            Filter.All -> events
            Filter.Used -> events.filter { it.eventType == ProductHistoryEventType.MarkedUsed }
            Filter.Discarded -> events.filter { it.eventType == ProductHistoryEventType.MarkedDiscarded }
            Filter.PortionChanges -> events.filter { it.eventType == ProductHistoryEventType.PortionChanged }
            Filter.Moves -> events.filter { it.eventType == ProductHistoryEventType.Moved }
        }

    /** Reverse chronological order using the ISO createdAt timestamp. */
    fun reverseChronological(events: List<ProductHistoryEvent>): List<ProductHistoryEvent> =
        events.sortedByDescending { it.createdAt }

    fun monthKey(event: ProductHistoryEvent): String {
        val date = DateUtils.parseDate(event.eventDate) ?: return "Unknown"
        return try {
            YearMonth.from(date).toString()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun countThisMonth(
        events: List<ProductHistoryEvent>,
        type: ProductHistoryEventType,
        month: YearMonth = YearMonth.from(Clock.today())
    ): Int = events.count {
        it.eventType == type && run {
            val d = DateUtils.parseDate(it.eventDate)
            d != null && YearMonth.from(d) == month
        }
    }
}
