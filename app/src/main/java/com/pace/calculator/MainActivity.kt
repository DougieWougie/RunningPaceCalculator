package com.pace.calculator

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.pace.calculator.ui.PaceCalculatorScreen
import com.pace.calculator.ui.theme.PaceCalculatorTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")
private val THEME_KEY = booleanPreferencesKey("dark_theme")
private val PACE_SECONDS_KEY = doublePreferencesKey("pace_seconds")
private val PACE_UNIT_KEY = stringPreferencesKey("pace_unit")

private const val DEFAULT_PACE_SECONDS = 510.0

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            var isDarkTheme by remember { mutableStateOf<Boolean?>(null) }
            var savedPaceSeconds by remember { mutableStateOf(DEFAULT_PACE_SECONDS) }
            var savedUnit by remember { mutableStateOf(PaceUnit.MILE) }

            LaunchedEffect(Unit) {
                val prefs = dataStore.data.first()
                savedPaceSeconds = prefs[PACE_SECONDS_KEY] ?: DEFAULT_PACE_SECONDS
                savedUnit = PaceUnit.entries.find { it.name == prefs[PACE_UNIT_KEY] } ?: PaceUnit.MILE
                isDarkTheme = prefs[THEME_KEY] ?: isSystemInDarkMode()
            }

            isDarkTheme?.let { darkTheme ->
                PaceCalculatorTheme(darkTheme = darkTheme) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PaceCalculatorScreen(
                            isDarkTheme = darkTheme,
                            onToggleTheme = {
                                val newTheme = !darkTheme
                                isDarkTheme = newTheme
                                lifecycleScope.launch {
                                    dataStore.edit { it[THEME_KEY] = newTheme }
                                }
                            },
                            initialPaceSeconds = savedPaceSeconds,
                            initialUnit = savedUnit,
                            onPaceChange = { paceSeconds, unit ->
                                lifecycleScope.launch {
                                    dataStore.edit {
                                        it[PACE_SECONDS_KEY] = paceSeconds
                                        it[PACE_UNIT_KEY] = unit.name
                                    }
                                }
                            },
                            contentPadding = innerPadding
                        )
                    }
                }
            }
        }
    }

    private fun isSystemInDarkMode(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
