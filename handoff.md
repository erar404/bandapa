# Handoff

## Goal
Build **bandapa** — a Kotlin Multiplatform Mobile (KMM) app for musicians to manage shared band calendars with real-time conflict detection. Core features: multi-band event calendars, automated conflict detection when events overlap across bands, voting to resolve conflicts, band management with invite codes.

Acceptance criteria: runs on Android (minimum target), Supabase backend, Compose Multiplatform UI, "Kinetic Sound" Vibrant Noir design system (ElectricPurple `#a020f0`, NeonGreen `#c3f400`, ElectricCyan `#00dbe9`, Background `#131313`, Surface `#1E1E1E`).

---

## Current State
**Phase 7 complete. BUILD SUCCESSFUL (1m 23s).** All screens are feature-complete with polish.

Phases delivered:
- Phase 1: Auth (Login/SignUp with Supabase)
- Phase 2: App skeleton (bottom nav, Koin DI, NavGraph)
- Phase 3: Calendar (month grid, event creation, band filtering)
- Phase 4: Conflicts (real-time detection, voting, dismiss)
- Phase 5: Bands page (BandsScreen, BandDetailScreen, edit sheet, member management, invite code copy)
- Phase 6: Profile tab (editable name, email, band list, sign out) + Venues (DB + venue picker in AddEventSheet)
- Phase 7: Event detail sheet (tap event → full info), Refresh buttons (Bands + Conflicts), Venue management screen (create/delete, accessible from Profile > Settings > Manage Venues)

**Next phase: Phase 8 — Release Prep**

---

## Files Actively Being Edited
No files are mid-change. All Phase 7 files are complete and building.

Key files added/changed in Phase 7:
- `feature/venues/ui/VenueViewModel.kt` — NEW (VenueUiState + CRUD)
- `feature/venues/ui/VenuesScreen.kt` — NEW (venue list, add sheet, refresh button)
- `feature/band/ui/BandsListViewModel.kt` — added `isRefreshing`, `refresh()`
- `feature/band/ui/BandsScreen.kt` — refresh icon button in header
- `feature/conflicts/ui/ConflictsViewModel.kt` — added `isRefreshing`, `refresh()`
- `feature/conflicts/ui/ConflictsScreen.kt` — refresh icon button in header
- `feature/calendar/ui/CalendarScreen.kt` — EventDetailSheet (tap card → detail), EventCard is now clickable
- `feature/profile/ui/ProfileScreen.kt` — "Settings" section with "Manage Venues" row that swaps to VenuesScreen within the tab
- `core/di/Modules.kt` — added VenueViewModel to venueModule

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

---

## Next Step
**Phase 8 — Release Prep**

Suggested work items (pick any order):

**A. App Icon + Splash**
- Custom `ic_launcher` with ElectricPurple logo in `androidMain/res`
- Themed splash screen using `SplashScreen API` (Android 12+)

**B. Sign-up profile creation trigger** (verify/fix):
- Supabase trigger `on_auth_user_created` should insert into `profiles` on new user
- Run: `select * from public.profiles` after a new sign-up to verify a row is created
- If missing: re-apply the trigger migration

**C. Input validation hardening**:
- `AddEventSheet`: validate `HH:mm` format before save (regex `^\d{2}:\d{2}$`)
- `CreateBandScreen`: enforce at least 1 non-empty genre if genres list is shown
- `JoinBandScreen`: uppercase + trim invite code before lookup

**D. APK build + device install**:
- The APK is already built at `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- Install: `adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- No `adb` on PATH by default; full path: `C:\Users\Arellano\AppData\Local\Android\Sdk\platform-tools\adb.exe`

**E. Deep-link test**:
- AndroidManifest already has `https://bandapa.app/invite/{code}` intent filter
- Test: `adb shell am start -W -a android.intent.action.VIEW -d "https://bandapa.app/invite/TESTCODE" com.bandapa`

---

## Context & Gotchas
- **JDK**: `C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot`
- **Android SDK (user-writable)**: `C:\Users\Arellano\AppData\Local\Android\Sdk` — API 35 installed
- **No git repo, no Android Studio.** Build: `.\gradlew.bat :composeApp:assembleDebug`
- **Koin ViewModel with parameters**: `viewModel { params -> VM(params.get(), get(), get()) }` in module
- **Supabase SessionStatus**: `SessionStatus.Initializing` (renamed in supabase-kt 3.x)
- **Icons**: `compose.materialIconsExtended` already in commonMain — do NOT add again
- **Realtime cleanup**: `supabase.realtime.removeChannel(ch)`
- **Clipboard**: `@Suppress("DEPRECATION") LocalClipboardManager`
- **All tables in `public` schema** — no `defaultSchema` override in SupabaseClient
- **`menuAnchor(MenuAnchorType.PrimaryNotEditable)`** required for ExposedDropdownMenu
- **`Icons.Default.ArrowBack` is deprecated** — use `Icons.AutoMirrored.Filled.ArrowBack` (just a warning, not blocking)
- **Supabase project ref**: `rrfelwwoypouqcjbdzrb`
- **Stitch design project**: ID `3705630547659865469`, design system "Kinetic Sound Vibrant Noir"
- **KMM stack**: Compose Multiplatform 1.7.3, AGP 8.7.3, Gradle 8.10.2, Supabase-kt 3.1.4, Koin 4.1.0, kotlinx-datetime 0.6.1
- **Profile.kt lives in `feature/band/domain/`** — reused by ProfileRepository
- **VenuesScreen is shown within the Profile tab** via a local `showVenues` state flag (no NavGraph change needed)
- **APK output path**: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- **`PullToRefreshBox` is NOT available in CMP 1.7.3 commonMain** — don't try to use it; will resolve in CMP 1.8.0+
