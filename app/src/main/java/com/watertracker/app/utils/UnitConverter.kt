package com.watertracker.app.utils

import kotlin.math.roundToInt

/**
 * Переводит миллилитры (единица хранения в БД) в единицы, выбранные
 * пользователем в настройках, и обратно. 1 cup = 236.588 мл (US cup).
 */
object UnitConverter {

    private const val ML_PER_OZ = 29.5735
    private const val ML_PER_CUP = 236.588

    fun mlToDisplay(ml: Int, unit: String): Double = when (unit) {
        Constants.UNIT_OZ -> ml / ML_PER_OZ
        Constants.UNIT_CUPS -> ml / ML_PER_CUP
        else -> ml.toDouble()
    }

    fun displayToMl(value: Double, unit: String): Int = when (unit) {
        Constants.UNIT_OZ -> (value * ML_PER_OZ).roundToInt()
        Constants.UNIT_CUPS -> (value * ML_PER_CUP).roundToInt()
        else -> value.roundToInt()
    }

    fun formatAmount(ml: Int, unit: String): String {
        val value = mlToDisplay(ml, unit)
        val unitLabel = when (unit) {
            Constants.UNIT_OZ -> "oz"
            Constants.UNIT_CUPS -> "cups"
            else -> "мл"
        }
        val numberPart = if (unit == Constants.UNIT_ML) {
            value.roundToInt().toString()
        } else {
            String.format("%.2f", value)
        }
        return "$numberPart $unitLabel"
    }
}
