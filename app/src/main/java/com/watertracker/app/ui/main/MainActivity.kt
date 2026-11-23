package com.watertracker.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.watertracker.app.R

/**
 * Единственная Activity в приложении (single-activity pattern).
 * Навигация между 5 экранами реализована через Navigation Component +
 * BottomNavigationView, как того требует раздел 2.1 ТЗ.
 */
class MainActivity : AppCompatActivity() {

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("profile", (application as com.watertracker.app.WaterTrackerApp).session?.account?.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val account = (application as com.watertracker.app.WaterTrackerApp).session?.account
        super.onCreate(savedInstanceState?.takeIf { account != null && it.getString("profile") == account.id })
        if (account == null) {
            startActivity(android.content.Intent(this, com.watertracker.app.auth.AuthActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
    }
}
