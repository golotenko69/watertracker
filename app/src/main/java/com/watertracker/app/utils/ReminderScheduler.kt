package com.watertracker.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.watertracker.app.R
import java.util.concurrent.TimeUnit

/**
 * Планирует периодические напоминания "выпей воды" через WorkManager,
 * когда пользователь включает переключатель в "Настройках".
 */
object ReminderScheduler {

    private const val WORK_NAME = "water_reminder_work"
    const val CHANNEL_ID = "water_reminders"

    fun schedule(context: Context) {
        ensureChannel(context)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(2, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID, "Напоминания о воде", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager?.createNotificationChannel(channel)
        }
    }
}

/** Worker, показывающий уведомление-напоминание выпить воды. */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        if (!prefs.remindersEnabled) return Result.success()

        val notification = NotificationCompat.Builder(applicationContext, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Время выпить воды 💧")
            .setContentText("Не забудьте отметить приём жидкости в Вода-трекере")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager?.notify(1001, notification)
        return Result.success()
    }
}
