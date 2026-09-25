package com.watertracker.app.utils

import com.watertracker.app.R

object IconMapper {
    fun forKey(key: String): Int = when (key) {
        "water" -> R.drawable.ic_water_drop
        "juice" -> R.drawable.ic_juice
        "tea" -> R.drawable.ic_tea
        "coffee" -> R.drawable.ic_coffee
        "milk" -> R.drawable.ic_milk
        "sport" -> R.drawable.ic_sport
        "soda" -> R.drawable.ic_soda
        else -> R.drawable.ic_other
    }
}
