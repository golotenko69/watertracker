package com.watertracker.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/** Простые юнит-тесты для критичной для UI логики конвертации единиц. */
class UnitConverterTest {

    @Test
    fun `ml to ml stays the same`() {
        assertEquals(1000.0, UnitConverter.mlToDisplay(1000, Constants.UNIT_ML), 0.001)
    }

    @Test
    fun `ml to cups converts correctly`() {
        val cups = UnitConverter.mlToDisplay(2366, Constants.UNIT_CUPS)
        assertEquals(10.0, cups, 0.05)
    }

    @Test
    fun `display to ml round trips`() {
        val ml = UnitConverter.displayToMl(2.0, Constants.UNIT_CUPS)
        assertEquals(473, ml)
    }

    @Test
    fun `formatAmount renders unit suffix`() {
        val formatted = UnitConverter.formatAmount(1000, Constants.UNIT_ML)
        assertEquals("1000 мл", formatted)
    }
}
