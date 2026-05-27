# Handoff

## Goal
Build **bandapa** — a Kotlin Multiplatform Mobile (KMM) app for musicians to manage shared band calendars with real-time conflict detection. Core features: multi-band event calendars, automated conflict detection when events overlap across bands, voting to resolve conflicts, band management with invite codes.

Acceptance criteria: runs on Android (minimum target), Supabase backend, Compose Multiplatform UI, "Kinetic Sound" Vibrant Noir design system (ElectricPurple `#a020f0`, NeonGreen `#c3f400`, ElectricCyan `#00dbe9`, Background `#131313`, Surface `#1E1E1E`).

---

## Current State
**Phase 8 complete. BUILD SUCCESSFUL (9m 18s, 39 tasks executed).**

Phases delivered:
- Phase 1: Auth (Login/SignUp with Supabase)
- Phase 2: App skeleton (bottom nav, Koin DI, NavGraph)
- Phase 3: Calendar (month grid, event creation, band filtering)
- Phase 4: Conflicts (real-time detection, voting, dismiss)
- Phase 5: Bands page (BandsScreen, BandDetailScreen, edit sheet, member management, invite code copy)
- Phase 6: Profile tab (editable name, email, band list, sign out) + Venues (DB + venue picker in AddEventSheet)
- Phase 7: Event detail sheet (tap event → full info), Refresh buttons (Bands + Conflicts), Venue management screen (create/delete, accessible from Profile > Settings > Manage Venues)
- Phase 8: Input validation hardening, profile trigger fix + backfill, app icon + splash screen

**APK ready at:** `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

---

## Files Actively Being Edited
No files are mid-change. All Phase 8 files are complete and building.

Key files added/changed in Phase 8:
- `feature/calendar/ui/CalendarScreen.kt` — AddEventSheet: Save disabled until title non-blank; HH:mm regex validation on start/end times with inline error labels
- `feature/band/ui/CreateBandScreen.kt` — Create button disabled until name non-blank; YYYY-MM-DD regex validation on dateFormed with inline error
- `feature/band/ui/JoinBandScreen.kt` — `.trim()` added to all `lookUpBand()` call sites
- `androidMain/res/drawable/ic_launcher_background.xml` — NEW: solid #131313 vector background
- `androidMain/res/drawable/ic_launcher_foreground.xml` — NEW: 5-bar soundwave (4× ElectricPurple + NeonGreen center), rounded caps, 108×108dp
- `androidMain/res/mipmap-anydpi-v26/ic_launcher.xml` — NEW: adaptive icon
- `androidMain/res/mipmap-anydpi-v26/ic_launcher_round.xml` — NEW: adaptive icon (round)
- `androidMain/res/values/themes.xml` — NEW: Theme.Bandapa + Theme.Bandapa.Splash (core-splashscreen)
- `androidMain/AndroidManifest.xml` — added android:icon, android:roundIcon; application theme → Theme.Bandapa; activity theme → Theme.Bandapa.Splash
- `androidMain/kotlin/com/bandapa/MainActivity.kt` — added `installSplashScreen()` before `super.onCreate()`
- `gradle/libs.versions.toml` — added core-splashscreen 1.0.1
- `composeApp/build.gradle.kts` — added `androidx.core.splashscreen` to androidMain deps
- **Supabase migration** `fix_handle_new_user_and_backfill`: rebuilt `handle_new_user()` with `SET search_path = public` + `public.profiles` schema-qualified ref; backfilled the orphaned profile row for it.arellanoerwin@gmail.com

---

## Failed Attempts
- **`Route.Bands.path` as post-create/join nav target**: Fixed: `Route.Dashboard.path`.
- **Core icon set for CalendarMonth, Groups, ChevronLeft/Right**: Fixed: `compose.materialIconsExtended`.
- **`put("field", if (x.isNullOrBlank()) JsonNull else x.trim())`**: Fixed: `JsonPrimitive(x.trim())`.
- **`supabase.removeChannel(ch)`**: Fixed: `supabase.realtime.removeChannel(ch)`.
- **New `LocalClipboard` API**: Fixed: `@Suppress("DEPRECATION") LocalClipboardManager`.
- **Single migration with cross-table RLS**: Fixed: split into separate ordered migrations.
- **`defaultSchema = "bandapa-main"`**: Fixed: removed from SupabaseClient.kt.
- **`on_auth_user_created` trigger without DROP IF EXISTS**: Fixed: prepend drop.
- **Adding methods with Edit tool (appended outside class)**: Fixed: rewrite entire file with Write.
- **`PullToRefreshBox` in commonMain (Compose Multiplatform 1.7.3)**: NOT available until CMP 1.8.0. Fixed: replaced with inline refresh IconButton + CircularProgressIndicator in each screen header.
- **Profile trigger silent failure**: `handle_new_user()` originally lacked `SET search_path` and used unqualified `profiles` ref — caused silent INSERT failure when `name` column didn't exist at sign-up time. Fixed in migration `fix_handle_new_user_and_backfill`.

---

## Remaining Work

**D. APK install + smoke test** (device needed):
- Install: `C:\Users\Arellano\AppData\Local\Android\Sdk\platform-tools\adb.exe install composeApp\build\outputs\apk\debug\composeApp-debug.apk`
- Check: splash screen shows (dark bg + soundwave bars), icon appears on launcher, sign-in works, profile tab shows "Erwin Roy Arellano"

**E. Deep-link test** (device needed):
- `adb shell am start -W -a android.intent.action.VIEW -d "https://bandapa.app/invite/TESTCODE" com.bandapa`
- Should open JoinBandScreen with the code pre-filled

**F. Fix VenuesScreen ArrowBack deprecation warning** (cosmetic, non-blocking):
- `VenuesScreen.kt:99` — `Icons.Filled.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack`

**G. Release / Play Store prep** (if going further):
- `isMinifyEnabled = true` + ProGuard rules for release build
- Signing config (`keystore` + `build.gradle.kts` signingConfigs block)
- App bundle: `.\gradlew.bat :composeApp:bundleRelease`

---

## Context & Gotchas
- **JDK**: `C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot`
- **Android SDK (user-writable)**: `C:\Users\Arellano\AppData\Local\Android\Sdk` — API 35 installed
- **adb full path**: `C:\Users\Arellano\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- **No git repo, no Android Studio.** Build: `.\gradlew.bat :composeApp:assembleDebug`
- **Koin ViewModel with parameters**: `viewModel { params -> VM(params.get(), get(), get()) }` in module
- **Supabase SessionStatus**: `SessionStatus.Initializing` (renamed in supabase-kt 3.x)
- **Icons**: `compose.materialIconsExtended` already in commonMain — do NOT add again
- **Realtime cleanup**: `supabase.realtime.removeChannel(ch)`
- **Clipboard**: `@Suppress("DEPRECATION") LocalClipboardManager`
- **All tables in `public` schema** — no `defaultSchema` override in SupabaseClient
- **`menuAnchor(MenuAnchorType.PrimaryNotEditable)`** required for ExposedDropdownMenu
- **`Icons.Default.ArrowBack` is deprecated** — use `Icons.AutoMirrored.Filled.ArrowBack` (warning, not blocking)
- **Supabase project ref**: `rrfelwwoypouqcjbdzrb`
- **Stitch design project**: ID `3705630547659865469`, design system "Kinetic Sound Vibrant Noir"
- **KMM stack**: Compose Multiplatform 1.7.3, AGP 8.7.3, Gradle 8.10.2, Supabase-kt 3.1.4, Koin 4.1.0, kotlinx-datetime 0.6.1, core-splashscreen 1.0.1
- **Profile.kt lives in `feature/band/domain/`** — reused by ProfileRepository
- **VenuesScreen is shown within the Profile tab** via a local `showVenues` state flag (no NavGraph change needed)
- **APK output path**: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- **`PullToRefreshBox` is NOT available in CMP 1.7.3 commonMain** — don't try to use it; will resolve in CMP 1.8.0+
- **Splash screen theme parent**: `Theme.SplashScreen` (from core-splashscreen lib) — activity uses `Theme.Bandapa.Splash`, application uses `Theme.Bandapa`
- **Icon design**: 5-bar soundwave equalizer, 108×108dp viewport, bars at x=29/40/51/62/73 (6dp wide, 5dp gap), center bar NeonGreen, outer bars ElectricPurple, all bars have r=2 rounded corners
- **Supabase `handle_new_user` function**: must have `SET search_path = public` and `public.profiles` (schema-qualified) — bare `profiles` silently fails in SECURITY DEFINER context
