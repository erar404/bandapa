# Handoff

## Goal

Build and maintain the **bandapa** Kotlin Multiplatform (KMP) / Compose Multiplatform app — a band calendar and management tool targeting Android (and iOS structure in place). The app uses Supabase as its backend (project ref: `rrfelwwoypouqcjbdzrb`).

This session focused on feature additions and bug fixes. The outstanding unfinished item is the **Google Maps Places autocomplete + geocoding + static map preview on the Add Venue sheet** — all the code was written and compiles, but the APK containing those changes has **never been successfully built** due to JVM OOM crashes during the DEX phase. The venues feature still works (minus Places integration) in the current live APK.

---

## Current State

### Live APK (26.01 MB, uploaded to Supabase Storage)
`https://rrfelwwoypouqcjbdzrb.supabase.co/storage/v1/object/public/releases/bandapa-latest.apk`

Working and confirmed:
- **Announcements on Home screen** — fetches `announcements` table, shows section with Campaign icon, title/body/image cards. Supabase Realtime subscription listens for new inserts while app is open.
- **Device notifications for new announcements** — Android local notification (channel `bandapa_announcements`) with BigPictureStyle when image URL present; iOS uses `UNUserNotificationCenter`.
- **Announcement image column** — `image_url` on `Announcement` model + Coil `AsyncImage` in card.
- **New app icon** — bandapa guitar+drum logo (dark green PNG from `static/AppIcons/android/`) as adaptive icon foreground; old soundwave vector replaced. Notification small icon is `ic_notification.xml` (white guitar vector).
- **app_logo.png** (splash + nav screen) replaced with `static/AppIcons/playstore.png` (512x512 tight crop).
- **Login crash fix** — explicit Koin type parameters in `homeModule` + null-safe `createChannel()` in `AndroidNotificationService`.

### Code written but NOT yet in APK (builds keep OOMing)
- **Venues — Google Places autocomplete** (`GooglePlacesClient.kt`)
- **Venues — address → lat/lng geocoding** (auto-fills on Places selection)
- **Venues — static Google Maps preview** (Coil `AsyncImage` with Static Maps API URL)
- **`latitude`/`longitude` columns** on `venues` table (migration 008 already applied to live DB; model + repo + ViewModel updated in code)
- **`VenueViewModel` rewrite** with `addressQuery`, `suggestions`, `geocoded` state + 350ms debounce

### Database (live Supabase — all migrations applied)
- Migration 006: `announcements` in `supabase_realtime` publication
- Migration 007: `image_url text` column on `announcements` + `announcement-images` storage bucket
- Migration 008: `latitude double precision`, `longitude double precision` on `venues`

---

## Files Actively Being Edited

### New files (untracked — NOT yet committed or in live APK)
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/data/GooglePlacesClient.kt` — Places Autocomplete + Geocoding REST client via Ktor HttpClient; `staticMapUrl()` helper for dark-themed Static Maps API URL. Complete, compiles, never built into APK.
- `supabase/migrations/20260530000008_venues_lat_lng.sql` — local migration file (migration already applied to live DB).

### Modified (unstaged — contain venues Places changes, NOT in live APK)
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/data/VenueRepository.kt` — `createVenue` signature adds `latitude: Double?, longitude: Double?`
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/data/VenueRepositoryImpl.kt` — passes lat/lng to Supabase insert
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/domain/Venue.kt` — added `latitude: Double?`, `longitude: Double?` fields
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/ui/VenueViewModel.kt` — completely rewritten: owns `addressQuery`, `suggestions`, `geocoded`, `isGeocoding` state; `onAddressQueryChanged()` with 350ms debounce; `onPlaceSelected()` triggers geocoding; `addVenue(name, city)` — no address param (comes from ViewModel state now)
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/ui/VenuesScreen.kt` — `AddVenueSheet` rebuilt with autocomplete dropdown (plain `if`, not `AnimatedVisibility`), lat/lng read-only teal fields, static map `AsyncImage`; venues list rows show lat/lng in small teal text
- `composeApp/src/commonMain/kotlin/com/bandapa/core/di/Modules.kt` — `venueModule` registers `GooglePlacesClient` as singleton; `homeModule` uses explicit `get<T>()` calls (crash fix); imports `NotificationService`
- `composeApp/src/androidMain/kotlin/com/bandapa/core/notifications/NotificationServiceImpl.kt` — `createChannel()` null-safe (`?: return`) + wrapped in `try-catch` in `init` (crash fix); `showNotification` takes `imageUrl: String?`
- `composeApp/build.gradle.kts` — added `GOOGLE_MAPS_API_KEY` to `buildkonfig`; added `coil-compose` + `coil-network-ktor` to commonMain deps

### Modified (staged — in live APK)
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/home/ui/HomeScreen.kt` — Announcements section with `AnnouncementCard` (Campaign icon, optional image at top)
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/home/ui/HomeViewModel.kt` — 5 deps; `announcements` in state; `listenForNewAnnouncements()` Realtime; notification trigger
- `composeApp/src/commonMain/kotlin/com/bandapa/core/notifications/NotificationService.kt` — interface `showNotification(title, body, imageUrl?)`
- `composeApp/src/commonMain/kotlin/com/bandapa/core/di/PlatformModule.kt` — `expect fun platformModule(): Module`
- `composeApp/src/androidMain/kotlin/com/bandapa/core/di/PlatformModule.android.kt` — actual providing `AndroidNotificationService`
- `composeApp/src/iosMain/kotlin/com/bandapa/core/di/PlatformModule.ios.kt` — actual providing `IosNotificationService`
- `composeApp/src/iosMain/kotlin/com/bandapa/core/notifications/NotificationServiceImpl.kt` — iOS `UNUserNotificationCenter` impl
- `composeApp/src/androidMain/AndroidManifest.xml` — `POST_NOTIFICATIONS` permission
- `composeApp/src/androidMain/kotlin/com/bandapa/MainActivity.kt` — requests `POST_NOTIFICATIONS` on Android 13+
- `gradle/libs.versions.toml` — added `coil = "3.1.0"` + two coil library entries
- `gradle.properties` — `Xmx1536m -XX:MetaspaceSize=256m -XX:+UseSerialGC`
- `local.properties` — added `google.maps.api_key=AIzaSyD0-QPiD49hA7aCdm2L76aBdz9WB8x0a3E`

---

## Failed Attempts

- **`AnimatedVisibility` inside `Box` within `Column` for suggestions dropdown** — compile error: `ColumnScope.AnimatedVisibility cannot be called in this context with an implicit receiver.` Kotlin picks the `ColumnScope` extension overload even inside a nested `Box` because the outer `Column` scope leaks in. **Fix**: replaced with plain `if` block — suggestions column appears inline below the text field.

- **`callbackFlow` in `AnnouncementRepositoryImpl`** — `callbackFlow` doesn't allow `suspend` calls (`subscribe()`, `collect`) inside its block. Compile errors: `Unresolved reference 'launch'`, `Suspension functions can only be called within coroutine body`. **Fix**: `channelFlow` which is a `ProducerScope` and allows suspend calls.

- **`action.decodeRecord<Announcement>()`** — `Unresolved reference 'decodeRecord'` at compile time. The extension isn't accessible/exported in supabase-kt 3.1.4 without knowing the exact import. **Fix**: `Json.decodeFromJsonElement<Announcement>(action.record)` using raw `record: JsonObject` property.

- **JVM OOM during builds** — Gradle daemon crashes during DEX phase (`dexBuilderDebug` / `mergeExtDexDebug`) with native memory OOM (`mmap` / `malloc` failed). Tried heaps: 4g (crashed at DEX) → 2g (crashed at DEX) → 1.5g (crashed at DEX for Places build) → 1.25g (crashed before task graph) → 1g (crashed before task graph). SerialGC helped marginally. Root cause: system running out of free RAM/virtual address space after multiple builds in the same session. **Current config**: 1536m + SerialGC works when config cache is warm. The Places + Coil combination requires more DEX memory than the system has available during an active dev session.

- **`homeModule` with un-typed `get()` calls** — `viewModel { HomeViewModel(get(), get(), get(), get(), get()) }` caused runtime `NoBeanDefFoundException`. Koin 4.1.0 can't infer `NotificationService` type (5th param) via type erasure when spanning multiple packages. **Fix**: `get<NotificationService>()` and explicit types for all 5 params.

- **`AndroidNotificationService.createChannel()` NPE** — `context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)` crashed the Koin factory with NPE. The Java method returns a platform type (`T!`) and Kotlin doesn't insert a null check. Exception propagated uncaught through Koin's factory lambda, crashing the app on login before HomeScreen could render. **Fix**: null guard (`?: return`) + `try { createChannel() } catch (_: Exception) {}` in `init`.

---

## Next Step

**Build the Places-enabled APK.** First close Android Studio and any heavy apps to free RAM. The Gradle config cache should already be valid (no build file changes needed). Then run:

```powershell
$env:JAVA_HOME = "C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Set-Location "C:\Users\Arellano\ERA\Coding\bandapa"
.\gradlew.bat --no-daemon :composeApp:assembleDebug
```

If it still OOMs at `dexBuilderDebug`, try enabling R8 shrinking in debug builds to reduce DEX work — add this to `composeApp/build.gradle.kts` inside the `debug` buildType block:
```kotlin
buildTypes {
    getByName("debug") {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
}
```

After a successful build, upload with the `/bandapa-apk-patch` skill.

---

## Context & Gotchas

### Build system
- **Always use `--no-daemon`** — daemon accumulates memory over builds and eventually crashes. Use the flag every time.
- **Do NOT change `gradle.properties` repeatedly** — every change invalidates the configuration cache, which forces a full re-evaluation that uses more memory and is more likely to OOM. The current `Xmx1536m + SerialGC` setting is the result of many trials. Leave it alone.
- **Config cache invalidation**: changes to `gradle.properties` or `build.gradle.kts` force full config recalculation. When cache is warm (no build file changes), `--no-daemon` builds complete in ~24s instead of 2+ minutes.
- **JVM crash logs** (`hs_err_pid*.log`, `replay_pid*.log`) in project root are noise from failed OOM builds — safe to delete.

### Supabase schema
- All tables are in `"bandapa-main"` schema (not `public`). `supabase.from("bands")` works because the Supabase project's PostgREST exposes `bandapa-main` as the default schema.
- The `announcements` Realtime subscription needs the table in the `supabase_realtime` publication (applied via migration 006).
- `venues` has `latitude`/`longitude` columns in the live DB (migration 008 applied) — the code changes that use these columns are in the unstaged files but haven't been built into an APK yet.

### Google Maps API key
- Key: `AIzaSyD0-QPiD49hA7aCdm2L76aBdz9WB8x0a3E` (in `local.properties`)
- Used for: Places Autocomplete REST, Geocoding REST, Static Maps API (image preview).
- The user described it as "Google Maps JavaScript API" key. If Places autocomplete returns empty results, check that the "Places API (New)" or "Places API" is enabled in GCP for this key.

### Venues Places implementation details
- `GooglePlacesClient` creates its own `HttpClient()` without specifying an engine (uses Ktor ServiceLoader to auto-detect OkHttp on Android). Multiple `HttpClient` instances are fine in Ktor — they don't conflict with Supabase's internal Ktor client.
- The suggestions dropdown uses plain `if` NOT `AnimatedVisibility` — Compose `ColumnScope.AnimatedVisibility` extension causes a compiler error inside a `Box` that's nested inside a `Column`. This is a known Kotlin scope receiver ambiguity.
- `VenueViewModel.addVenue(name, city)` — **NOTE**: signature changed from `addVenue(name, address, city)` to `addVenue(name, city)`. The address now lives in `uiState.addressQuery` (ViewModel state). If anything else in the codebase calls the old 3-param signature, it will fail to compile.

### Koin DI rules learned this session
- Use explicit `get<Type>()` in `viewModel` lambdas whenever there are 4+ dependencies or when dependency types span multiple feature packages. The implicit `get()` fails via type erasure in Koin 4.1.0 for these cases.
- `platformModule()` is `expect/actual`. Android provides `AndroidNotificationService`, iOS provides `IosNotificationService`. Both are registered as `single<NotificationService>`.

### Announcement Realtime
- Exceptions in `listenForNewAnnouncements()` are silently swallowed — if Realtime fails, home screen just won't receive live updates (no crash, no error shown). Intentional: Realtime is best-effort.
- `CoroutineScope(Dispatchers.IO)` in `AndroidNotificationService` is an unstructured root scope (not cancelled when ViewModel is cleared). Minor memory leak — acceptable for fire-and-forget notification posting.

### iOS notifications
- `IosNotificationService` is text-only — no image attachments. iOS notification image attachments require downloading the image to a temp file and creating `UNNotificationAttachment`, which was not implemented.
