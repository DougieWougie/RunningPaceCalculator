# Pace Calculator ProGuard Rules

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep model classes
-keep class com.pace.calculator.PaceCalculator** { *; }
-keep class com.pace.calculator.PaceUnit { *; }
