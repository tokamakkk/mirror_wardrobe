# Mirror Wardrobe MVP

Mirror Wardrobe is a native Android app (Kotlin + Jetpack Compose) for personal wardrobe management, outfit planning, and weather-aware recommendations.

## Highlights

- Manage clothing items with categories, photos, and warmth values
- Create and edit outfits from saved wardrobe items
- Track daily outfit records on calendar view
- View statistics (total items, category distribution, wear frequency)
- AI fitting flow (image-to-image / text fallback)
- Local weather card with:
  - permission state handling
  - timeout-safe location and geocoding
  - manual city search
  - warmth target calculation and outfit suggestions

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Architecture:** ViewModel + Repository + Room + StateFlow
- **Database:** Room (SQLite)
- **Image:** Coil
- **Navigation:** Navigation Compose
- **Location/Geocoding:** Fused Location + Geocoder
- **Weather API:** Open-Meteo
- **Build:** Gradle Wrapper (`gradlew`)

---

## Requirements

- Android Studio (latest stable recommended)
- Android SDK `platforms;android-36` (`compileSdk = 36`)
- JDK 17+ (Android Studio embedded JDK is fine)
- No global Gradle install required (wrapper included)

---

## Run the App

### Android Studio

1. Open project root: `d:\COMP7506project\MirrorWardrobe(4)\mirrorwardrobe`
2. Wait for Gradle sync
3. Select emulator/device and run

### CLI (PowerShell)

```powershell
.\gradlew :app:assembleDebug
```

Debug APK:

- `app\build\outputs\apk\debug\app-debug.apk`

---

## Project Structure

- `app/src/main/java/com/comp7506/mywardrobe/`
  - `MainActivity.kt` - app entry
  - `MyWardrobeApp.kt` - navigation scaffold
  - `navigation/AppRoutes.kt` - routes
- `app/src/main/java/com/comp7506/mywardrobe/ui/`
  - `screens/` - Compose screens
  - `components/` - reusable UI blocks
  - `viewmodel/` - UI state + business coordination
  - `theme/` - typography/colors
- `app/src/main/java/com/comp7506/mywardrobe/data/`
  - `db/` - Room entities/dao/database
  - `repository/` - data access and API orchestration
  - `location/` - location and geocoding utilities
  - `weather/` - weather models/repository/client
- `app/src/main/res/values/strings.xml` - UI/resource strings

---

## Weather Module (Current Behavior)

### State Flow

`NeedsPermission -> PermissionDenied | Loading -> Success | Error`

### Data Pipeline

`LocationProvider -> Geocoding -> OpenMeteoClient -> WarmthCalculator -> UI`

### Resilience

- Location timeout: 5s (`withTimeoutOrNull`)
- City reverse geocoding timeout: 3s
- Global exception guard in `HomeViewModel.refreshWeather`
- `CancellationException` is rethrown (proper coroutine behavior)

### Manual City Search

- Tap city name in Home weather card to enter edit mode
- Submit via keyboard Search action or search icon
- Flow: city name -> geocode to lat/lon -> fetch weather -> recompute recommendation

### Cache Policy

- TTL: 15 minutes
- Cache hit requires coordinate delta <= `0.02`
- `forceRefresh = true` bypasses cache

---

## Warmth and Recommendation Logic

- Base warmth target: `26 - apparentTemperature` (fallback to actual temperature)
- Adjustments:
  - +1 if wind speed >= 8 m/s
  - +1 if humidity >= 80%
  - +1 if cloud cover >= 70%
  - capped at **+2**
- Recommendation priority:
  1. Use real wardrobe items first
  2. Fallback to built-in template suggestions

---

## Testing

### Unit Test Focus

- `HomeViewModel.refreshWeather` success/error state transitions
- Timeout behavior (mock delayed location > 5s)
- Unexpected exception fallback (state must become `Error`, not stuck in `Loading`)

Run unit tests:

```powershell
.\gradlew :app:testDebugUnitTest
```

---

## Notes

- This project is an MVP for coursework/team delivery.
- Most data is local-first (Room + local URI references).
- Some legacy comments may still be bilingual in non-user-facing code; user-facing strings are managed through `strings.xml`.
