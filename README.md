# Pace Calculator - Android App

A native Android implementation of the running pace calculator, built with Kotlin and Jetpack Compose.

## Features

- Convert between min/mile and min/km pace
- Display speed in mph and km/h
- Light and dark theme support with system theme detection
- Animated background pulse effect
- Persistent theme preference using DataStore

## Project Setup

### Option 1: Open in Android Studio (Recommended)

1. Open Android Studio
2. Select "Open" and navigate to the `android/` directory
3. Android Studio will automatically set up Gradle and download dependencies
4. Click "Run" to build and deploy to an emulator or device

### Option 2: Command Line Build

If you don't have the Gradle wrapper JAR, generate it first:

```bash
cd android
gradle wrapper
```

Then build the project:

```bash
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Minimum SDK: 26 (Android 8.0)

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/pace/calculator/
│   │   │   ├── MainActivity.kt          # Entry point with theme persistence
│   │   │   ├── PaceCalculator.kt         # Conversion logic
│   │   │   └── ui/
│   │   │       ├── PaceCalculatorScreen.kt  # Main UI
│   │   │       └── theme/
│   │   │           ├── Color.kt          # Color palette
│   │   │           ├── Theme.kt          # Material3 theme
│   │   │           └── Type.kt           # Typography
│   │   ├── res/
│   │   │   ├── values/                   # Strings, colors, themes
│   │   │   └── drawable/                 # Icons
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Custom Fonts (Optional)

The app uses system fonts by default. To use Bebas Neue and JetBrains Mono:

1. Download fonts from Google Fonts
2. Place in `app/src/main/res/font/`:
   - `bebas_neue.ttf`
   - `jetbrains_mono_regular.ttf`
   - `jetbrains_mono_semibold.ttf`
3. Uncomment the font definitions in `Type.kt`

## Testing

1. Enter pace values using minutes and seconds inputs
2. Toggle between min/mile and min/km input units
3. Verify all four result cards update correctly
4. Test theme toggle (light/dark)
5. Rotate device to verify layout adapts
6. Kill and restart app to verify theme persists
