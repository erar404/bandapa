# Handoff

## Goal

Full redesign + feature buildout of **bandapa** — a Kotlin Multiplatform (Android + iOS) band calendar app using Compose Multiplatform, Supabase, and Koin. The end state is a production-quality app with:
- "Kinetic Sound" design language (midnight-green surfaces, electric lime `#6ee304` accents, `OnSurfaceVariant` for secondary text, transparent-container text fields, ExtraBold typography hierarchy)
- Conflict detection with voting
- Multi-band calendar (personal + band events)
- Supabase-backed auth, profiles, bands, events, venues
- Design following `design-taste-frontend` skill: DESIGN_VARIANCE=8 (asymmetric), MOTION_INTENSITY=6 (spring physics), VISUAL_DENSITY=4 (balanced)

## Current State

**Working / Complete:**
- BUILD SUCCESSFUL — `composeApp-debug.apk` built at `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- All changes committed to branch `master`, commit `43d55a09` ("design and functions modifications")
- Branch is 1 commit ahead of `origin/master` (not pushed)
- Kinetic Sound palette fully applied: `Color.kt`, `Theme.kt`, `Type.kt`
- All core screens redesigned: LoginScreen, SignUpScreen, HomeScreen, BandsScreen, ConflictsScreen, CalendarScreen, ProfileScreen, CreateBandScreen
- RLS infinite-recursion fix live on Supabase (SECURITY DEFINER function `get_my_band_ids()`)
- Profile tab moved to top-bar avatar+dropdown in `MainScreen.kt`
- HomeScreen with today's events + bands greeting
- NoBands placeholder in ConflictsScreen with "Go to Bands" redirect
- App logo (`static/app-logo.png`) applied to splash screen and Compose resources
- `bandapa-apk-patch` Claude Code skill at `.claude/skills/bandapa-apk-patch/SKILL.md`
- Supabase Storage RLS policies allow anon APK upload to `releases` bucket

**Partial / Not done:**
- Sora font not integrated — `composeApp/src/commonMain/composeResources/font/` directory does not exist; needs `sora_regular.ttf` + `sora_bold.ttf` (font files must be sourced externally). Currently falls back to system sans-serif.
- APK not pushed to remote (`origin/master` is 1 commit behind)
- No device/emulator UI testing was done — redesign verified by build only
- Several secondary screens still use `CircularProgressIndicator` (not yet replaced with `LinearProgressIndicator`): `BandDetailScreen`, `CreateBandScreen`, `JoinBandScreen`, `ProfileScreen`, `VenuesScreen`

**Known warnings (non-blocking):**
- `VenuesScreen.kt:99` uses deprecated `Icons.Default.ArrowBack` — should be `Icons.AutoMirrored.Filled.ArrowBack`

## Files Actively Being Edited

All changes are committed. For reference, the files modified across both the prior session and this one:

**Auth layer:**
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/auth/ui/components/AuthTextField.kt` — Transparent container colors, `small` shape; removes filled container backgrounds
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/auth/ui/LoginScreen.kt` — Full redesign: left 4dp×52dp lime accent stripe + `displaySmall` ExtraBold wordmark, HorizontalDivider, tactile scale button (97% on press via `animateFloatAsState`+`collectIsPressedAsState`), text-based loading ("Logging in...")
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/auth/ui/SignUpScreen.kt` — Full redesign: `IconButton`+`ArrowBack` back nav, 3dp×44dp accent stripe header, "ACCOUNT"/"PROFILE" uppercase section labels with letterSpacing, email confirmation state with envelope icon in rounded box, same tactile button

**Main app:**
- `composeApp/src/commonMain/kotlin/com/bandapa/ui/MainScreen.kt` — Profile removed from nav tabs; added HOME tab; TopAppBar with avatar-initials circle; dropdown: name, email, Manage Venues, Sign Out; `ProfileViewModel` injected via `koinViewModel()`
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/home/ui/HomeScreen.kt` — Created from scratch: two-line greeting (headlineMedium + headlineLarge in ElectricPurple), stagger fade+slide-in animations (70ms/60ms per item), `LinearProgressIndicator` for loading, `OnSurfaceVariant` secondary text
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/home/ui/HomeViewModel.kt` — Created from scratch: parallel async load of profile + today's events + bands
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/ui/BandsScreen.kt` — Band cards now use 3dp ElectricCyan left accent stripe; `LinearProgressIndicator` replaces spinners; rounded icon-box empty state; `OnSurfaceVariant` secondary text; removed manual refresh button
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/ui/CreateBandScreen.kt` — Transparent text field containers
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/conflicts/domain/ConflictsUiState.kt` — Added `NoBands` sealed state
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/conflicts/ui/ConflictsScreen.kt` — Full redesign: 2dp lime top border on conflict cards replaces warning icon header; `LinearProgressIndicator` for loading/refresh; rounded icon-box empty/no-bands states
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/conflicts/ui/ConflictsViewModel.kt` — Checks `bandRepo.getMyBands().isEmpty()` before loading; emits `NoBands` if empty
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/calendar/ui/CalendarScreen.kt` — Targeted: transparent text field containers, ExtraBold month/day/sheet headers, `OnSurfaceVariant` for event secondary text
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/profile/ui/ProfileScreen.kt` — Transparent text field containers; minor secondary text cleanup
- `composeApp/src/commonMain/kotlin/com/bandapa/navigation/NavGraph.kt` — Splash screen shows `app_logo` Compose resource image

**Theme:**
- `composeApp/src/commonMain/kotlin/com/bandapa/ui/theme/Color.kt` — Full Kinetic Sound palette; `ElectricPurple = ElectricLime` alias so all existing refs compile
- `composeApp/src/commonMain/kotlin/com/bandapa/ui/theme/Theme.kt` — Full M3 `darkColorScheme` with all semantic tokens
- `composeApp/src/commonMain/kotlin/com/bandapa/ui/theme/Type.kt` — Headings 1.2× line-height, body 1.5×; Sora font integration documented in comments

**Assets:**
- `composeApp/src/androidMain/res/drawable/app_logo.png` — Copied from `static/app-logo.png`
- `composeApp/src/commonMain/composeResources/drawable/app_logo.png` — Copied from `static/app-logo.png`
- `composeApp/src/androidMain/res/values/themes.xml` — Splash screen colors updated to `#0f1509`, icon to `@drawable/app_logo`

**Infrastructure:**
- `composeApp/src/commonMain/kotlin/com/bandapa/core/di/Modules.kt` — Added `homeModule` with `HomeViewModel`; ConflictsViewModel now gets `BandRepository`
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/calendar/data/CalendarRepository.kt` — Added `getTodayEvents(): List<Event>`
- `composeApp/src/commonMain/kotlin/com/bandapa/feature/calendar/data/CalendarRepositoryImpl.kt` — Implemented `getTodayEvents()`
- `.claude/skills/bandapa-apk-patch/SKILL.md` — Skill that builds APK + uploads to Supabase Storage
- `DESIGN.md` — Stitch "Kinetic Sound" design system file

## Failed Attempts

- **What was tried**: Build without setting `JAVA_HOME` — **Why it failed**: Gradle couldn't locate the JDK. Must set `$env:JAVA_HOME = "C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot"` before every `gradlew.bat` call.
- **What was tried**: Uploading APK to Supabase Storage with only SELECT policy — **Why it failed**: Storage needs INSERT + UPDATE + SELECT RLS policies on `storage.objects` for the `releases` bucket. All three migrations have been applied and are live.
- **What was tried**: Spawning Explore/Agent sub-agents for codebase search — **Why it failed**: User rejected agent tool calls. Use direct tools (Read, Grep, Glob) for all codebase exploration.
- **What was tried**: Initial RLS policies for `band_members` that queried `band_members` in their own `USING` clause — **Why it failed**: Caused infinite recursion. Fixed by creating `SECURITY DEFINER` function `get_my_band_ids()` that bypasses RLS, then using it in policies.

## Next Step

**Push the commit to remote, then install the APK on a device for visual verification:**

```powershell
git push origin master
```

Then install `composeApp/build/outputs/apk/debug/composeApp-debug.apk` on an Android device and verify:
1. Login screen shows the asymmetric lime accent stripe + "bandapa" wordmark
2. Button scales to 97% on press (tactile feedback)
3. Home screen shows two-line greeting with display name in lime
4. Band cards have ElectricCyan left accent stripe
5. Conflict cards have 2dp lime top border

**Or rebuild + upload APK to Supabase Storage for OTA distribution:**
```
/bandapa-apk-patch
```

**Secondary cleanup** — replace remaining `CircularProgressIndicator` usages in `BandDetailScreen.kt`, `JoinBandScreen.kt`, `VenuesScreen.kt`, `ProfileScreen.kt` with `LinearProgressIndicator`. Pattern to follow:
```kotlin
// Replace:
Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = ElectricPurple)
}
// With:
LinearProgressIndicator(
    modifier   = Modifier.fillMaxWidth(),
    color      = ElectricPurple,
    trackColor = SurfaceVariant,
)
```

## Context & Gotchas

**Build environment:**
- Must set `$env:JAVA_HOME = "C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot"` before any `gradlew.bat` call
- Build command: `.\gradlew.bat :composeApp:assembleDebug --no-daemon`
- Build takes ~2-3 minutes cold, ~30s with configuration cache

**Design system:**
- `ElectricPurple` is a Kotlin alias for `ElectricLime` (`#6ee304`). All existing code that references `ElectricPurple` gets the lime color — this is intentional for backwards compatibility without a mass rename.
- `OnAccent` = `OnPrimary` = `#083900` (dark green text on lime buttons)
- `NeonGreen` = `OnPrimaryContainer` = `#89ff4c` (personal event accent, brighter lime)
- `ElectricCyan` = `#a0cfd2` (band event accent, muted teal)
- `OnSurfaceVariant` = `#c2c9b7` (secondary text — prefer over `OnSurface.copy(alpha = ...)`)
- Transparent containers: all text fields should use `focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent`

**Supabase:**
- Project ref: `rrfelwwoypouqcjbdzrb`
- APK upload URL: `https://rrfelwwoypouqcjbdzrb.supabase.co/storage/v1/object/public/releases/bandapa-latest.apk`
- Anon key is in `local.properties` as `supabase.anon.key`
- `get_my_band_ids()` SECURITY DEFINER function is the RLS recursion fix — do not revert or replace with inline subqueries

**Sora font:**
- To activate: add `sora_regular.ttf` + `sora_bold.ttf` to `composeApp/src/commonMain/composeResources/font/`
- Then declare font family in `Type.kt` (instructions are in comments at the top of that file)
- Source fonts from Google Fonts: https://fonts.google.com/specimen/Sora

**Stitch design:**
- "Kinetic Sound" project ID: `3705630547659865469`
- `DESIGN.md` in project root is the source of truth for Stitch screen generation

**VenuesScreen deprecation warning:**
- `VenuesScreen.kt:99` — change `Icons.Default.ArrowBack` to `Icons.AutoMirrored.Filled.ArrowBack` and add `import androidx.compose.material.icons.automirrored.filled.ArrowBack`
- This is cosmetic (compiler warning only, does not break the build)
