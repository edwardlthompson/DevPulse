package dev.foss.goldenpath

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dev.foss.goldenpath.about.AppUpdatePreferences
import dev.foss.goldenpath.inventory.ObtainiumImportLaunch
import dev.foss.goldenpath.inventory.RefreshLaunch
import dev.foss.goldenpath.inventory.SettingsPersistence
import dev.foss.goldenpath.inventory.SignerReplaceLaunch
import dev.foss.goldenpath.network.NetworkStatusMonitor
import dev.foss.goldenpath.ui.GoldenPathApp
import dev.foss.goldenpath.ui.refresh.DisplayRefresh
import dev.foss.goldenpath.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var networkStatusMonitor: NetworkStatusMonitor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DisplayRefresh.apply(this)
        val themePreferences = ThemePreferences(applicationContext)
        val appUpdatePreferences = AppUpdatePreferences(applicationContext)
        networkStatusMonitor = NetworkStatusMonitor(applicationContext).also { it.start() }

        lifecycleScope.launch {
            SettingsPersistence.restoreIfEmpty(applicationContext)
            appUpdatePreferences.clearPendingRestart()
            appUpdatePreferences.ensureInstalledFormat()
        }

        setContent {
            GoldenPathApp(
                context = this,
                scope = lifecycleScope,
                themePreferences = themePreferences,
                appUpdatePreferences = appUpdatePreferences,
                networkStatusMonitor = networkStatusMonitor!!,
            )
        }
        RefreshLaunch.maybeStart(this, intent)
        SignerReplaceLaunch.maybeStart(this, intent)
        ObtainiumImportLaunch.maybeStart(this, intent)
    }

    override fun onResume() {
        super.onResume()
        DisplayRefresh.apply(this)
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch { SettingsPersistence.backup(applicationContext) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        RefreshLaunch.maybeStart(this, intent)
        SignerReplaceLaunch.maybeStart(this, intent)
        ObtainiumImportLaunch.maybeStart(this, intent)
    }

    override fun onDestroy() {
        networkStatusMonitor?.stop()
        super.onDestroy()
    }
}
