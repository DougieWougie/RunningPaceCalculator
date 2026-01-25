package com.pace.calculator

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.pace.calculator.ui.PaceCalculatorScreen
import com.pace.calculator.ui.theme.PaceCalculatorTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "settings")
private val THEME_KEY = booleanPreferencesKey("dark_theme")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                isDarkTheme = dataStore.data.map { it[THEME_KEY] }.first()
                    ?: isSystemInDarkMode()
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
