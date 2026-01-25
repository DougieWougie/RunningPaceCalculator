package com.pace.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.pace.calculator.ui.PaceCalculatorScreen
import com.pace.calculator.ui.theme.PaceCalculatorTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private val themeKey = booleanPreferencesKey("dark_theme")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf<Boolean?>(null) }

            // Load saved theme preference
            LaunchedEffect(Unit) {
                val savedTheme = dataStore.data.map { preferences ->
                    preferences[themeKey]
                }.first()

                isDarkTheme = savedTheme ?: resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
            }

            // Only show UI once theme is loaded
            isDarkTheme?.let { darkTheme ->
                PaceCalculatorTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        PaceCalculatorScreen(
                            isDarkTheme = darkTheme,
                            onToggleTheme = {
                                val newTheme = !darkTheme
                                isDarkTheme = newTheme
                                lifecycleScope.launch {
                                    dataStore.edit { preferences ->
                                        preferences[themeKey] = newTheme
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
