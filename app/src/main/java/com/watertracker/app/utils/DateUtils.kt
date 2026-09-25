package com.watertracker.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun daysAgoMillis(days: Int): Long =
        startOfTodayMillis() - days.toLong() * 24 * 60 * 60 * 1000

    fun formatTime(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

    fun formatDayShort(millis: Long): String =
        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))

    fun formatDateHuman(millis: Long): String {
        return if (millis >= startOfTodayMillis()) {
            "Сегодня"
        } else if (millis >= startOfTodayMillis() - 24 * 60 * 60 * 1000) {
            "Вчера"
        } else {
            SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(millis))
        }
    }
}
