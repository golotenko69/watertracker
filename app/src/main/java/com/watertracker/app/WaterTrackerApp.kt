package com.watertracker.app

import android.app.Application
import com.watertracker.app.auth.Account
import com.watertracker.app.auth.AccountStore
import com.watertracker.app.database.AppDatabase
import com.watertracker.app.repository.WaterRepository
import com.watertracker.app.utils.PreferencesManager
import com.watertracker.app.utils.ReminderScheduler
import com.watertracker.app.widget.WaterWidgetProvider
import com.watertracker.app.widget.WidgetCacheUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class WaterTrackerApp : Application() {
    val accounts by lazy { AccountStore(this) }

    class Session(val account: Account, val repository: WaterRepository) {
        val scope = CoroutineScope(SupervisorJob())
    }

    @Volatile var session: Session? = null
        private set
    val repository get() = checkNotNull(session).repository

    override fun onCreate() {
        super.onCreate()
        accounts.current()?.let { openSession(it, restoreReminders = false) }
    }

    fun openSession(account: Account, restoreReminders: Boolean = true) {
        if (session?.account == account) return
        session?.scope?.cancel()
        val db = AppDatabase.open(this, account.id)
        val repository = WaterRepository(db.waterEntryDao(), db.drinkTypeDao(), db.tagDao(), db.entryTagCrossRefDao())
        val next = Session(account, repository)
        accounts.remember(account)
        session = next
        WidgetCacheUpdater.start(this, repository, next.scope)
        if (restoreReminders && PreferencesManager(this, account.id).remindersEnabled) {
            ReminderScheduler.schedule(this)
        }
    }

    fun logout() {
        accounts.logout()
        val old = session
        session = null
        old?.scope?.cancel()
        ReminderScheduler.cancel(this)
        WaterWidgetProvider.refreshAllWidgets(this)
    }
}
