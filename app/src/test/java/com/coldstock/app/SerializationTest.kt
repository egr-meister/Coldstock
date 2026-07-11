package com.coldstock.app

import com.coldstock.app.data.ColdstockJson
import com.coldstock.app.model.AppSettings
import com.coldstock.app.model.FrozenProduct
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SerializationTest {

    @Test fun roundTripsProducts() {
        val list = listOf(
            FrozenProduct(id = "1", name = "Soup", portionCount = 4),
            FrozenProduct(id = "2", name = "Berries", portionCount = 2)
        )
        val json = ColdstockJson.encodeToString(ListSerializer(FrozenProduct.serializer()), list)
        val back = ColdstockJson.decodeFromString(ListSerializer(FrozenProduct.serializer()), json)
        assertEquals(list, back)
    }

    @Test fun toleratesUnknownKeys() {
        val json = """[{"id":"1","name":"X","portionCount":1,"newFutureField":true}]"""
        val back = ColdstockJson.decodeFromString(ListSerializer(FrozenProduct.serializer()), json)
        assertEquals(1, back.size)
        assertEquals("X", back.first().name)
    }

    @Test fun defaultsFillMissingFields() {
        val json = """[{"id":"1","name":"X"}]"""
        val back = ColdstockJson.decodeFromString(ListSerializer(FrozenProduct.serializer()), json)
        assertEquals(0, back.first().portionCount)
        assertTrue(back.first().freezingDate.isEmpty())
    }

    @Test fun settingsMergeWithDefaults() {
        val json = """{"soonThresholdDays":14}"""
        val settings = ColdstockJson.decodeFromString(AppSettings.serializer(), json)
        assertEquals(14, settings.soonThresholdDays)
        // reminderSettings default present
        assertTrue(settings.reminderSettings.enabled)
    }
}
