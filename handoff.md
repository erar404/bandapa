# Handoff

## Goal

Build **Bandapa** — a Kotlin Multiplatform Mobile (KMM) app for Android + iOS that gives bands a shared calendar with conflict detection, multi-band support, venue management, and a unified profile/band dashboard. The app uses Supabase as its backend (PostgreSQL, Auth, Realtime, Storage).

Acceptance criteria in play:
- App must not crash on login or cold-open when already authenticated.
- All data reads/writes must use the `bandapa-main` Postgres schema (migrated away from `public.*`).
- Username-based login (not just email) must work via the `get_email_by_username` RPC.
- Band display pictures must show in the Bands list, Home screen "My Bands" section, and Band Detail screen.
- Profile page must support display-picture upload and name editing.
- Add Venue form must include a venue-type selector (Studio / Hangout Place / Bar/Live Venue / Others).

---

## Current State

**The app builds cleanly and the latest APK is live at:**
```
https://rrfelwwoypouqcjbdzrb.supabase.co/storage/v1/object/public/releases/bandapa-latest.apk
```

**What is working:**
- Login/signup (email + password; username + password via `get_email_by_username` RPC)
- Navigation (NavGraph double-navigation crash fixed with `isFirstComposition` guard)
- Home screen: today's events, my bands with image thumbnails, announcements (returns empty list gracefully)
- Bands tab: band cards with image thumbnails
- Band Detail: full-width band cover image at top of info section
- Profile page: display picture upload via `avatars` storage bucket, name editing, username shown
- Venues: add venue with type selector (Studio / Hangout Place / Bar/Live Venue / Others + text field), type badge shown in list rows
- All data sources target `bandapa-main` schema (`defaultSchema = "bandapa-main"`)
- `bandapa-main.bands` has 1 row (migrated from `public.bands` — band named "erar", invite code C862E6)
- `bandapa-main.band_members` has 1 row (migrated — user is admin)
- `bandapa-main.users` has 1 row (username: erar-user, full_name: Erwin Roy Arellano)

**What is partial / not yet tested:**
- Calendar event creation: code now sends `event_type` and `owner_id` correctly but has not been confirmed working end-to-end in the new schema.
- Conflicts screen: adapted to `bandapa-main` column names (`band_event_id`, `personal_event_id`, status `"pending"`) but **voting is broken** — the app still passes an event UUID where the DB column `vote` expects `'cancel'` or `'greenlit'`. No conflicts exist yet so nothing is blocking.
- Realtime announcements: subscription targets `bandapa-main.announcements` correctly. Table exists with 0 rows.
- iOS target: not built/tested; only Android has been compiled and uploaded.

**`public.*` tables still have their original data** (1 profile, 1 band, 1 band_member) — no longer read by the app but not yet dropped.

---

## Files Actively Being Edited

All changes are unstaged/uncommitted on branch `master` (25 files, +347/-90 lines vs last commit).

- `composeApp/src/commonMain/kotlin/com/bandapa/core/supabase/SupabaseClient.kt` — Added `install(Postgrest) { defaultSchema = "bandapa-main" }`

- `composeApp/src/commonMain/kotlin/com/bandapa/navigation/NavGraph.kt` — Added `isFirstComposition` guard so `LaunchedEffect(sessionStatus)` skips its first fire; `startDest` handles initial route, preventing double-navigation crash

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/home/ui/HomeViewModel.kt` — Wrapped async calls in `supervisorScope`; announcements deferred uses `runCatching { }.getOrElse { emptyList() }` so a failing table can't crash the load

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/domain/Profile.kt` — Added `@SerialName("full_name") val name`, `username`, `@SerialName("display_picture") val displayPicture`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/domain/Band.kt` — `@SerialName("created_by") val ownerId`, `@SerialName("spotify_artist_id") val spotifyUrl`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/domain/BandMember.kt` — Replaced `role: String` with `@SerialName("is_admin") val isAdmin: Boolean`; `role` is now `@Transient val role = if (isAdmin) "admin" else "member"`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/calendar/domain/Event.kt` — `@SerialName("owner_id") val userId`, added `@SerialName("event_type") val eventType = "personal"`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/conflicts/domain/Conflict.kt` — `@SerialName("band_event_id") val eventAId`, `@SerialName("personal_event_id") val eventBId`, removed `bandId`, status default `"pending"`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/conflicts/domain/ConflictVote.kt` — `@SerialName("vote") val votedFor`, `@SerialName("voted_at") val createdAt`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/domain/Venue.kt` — Added `@SerialName("venue_type") val venueType = "others"`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/profile/data/ProfileRepository.kt` — Added `suspend fun uploadDisplayPicture(bytes: ByteArray): Profile`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/profile/data/ProfileRepositoryImpl.kt` — All queries use `"users"` table (not `"profiles"`); `updateProfile` writes `"full_name"`; `uploadDisplayPicture` uploads to `avatars` bucket then updates `display_picture`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/profile/ui/ProfileViewModel.kt` — Added `isUploadingPhoto` state and `uploadPhoto(bytes: ByteArray)`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/profile/ui/ProfileScreen.kt` — Avatar shows actual photo via `AsyncImage` with initials fallback; `BandImagePicker` reused for photo picking; username displayed as `@handle`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/data/BandRepositoryImpl.kt` — Uses `created_by`, `spotify_artist_id`, `is_admin = false`; `getMembersWithProfiles` queries `"users"`; `getBandByInviteCode` passes `p_code` param; `updateBand` uses `spotify_artist_id`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/calendar/data/CalendarRepositoryImpl.kt` — `createEvent` uses explicit `buildJsonObject` (avoids inserting empty `id`), includes `owner_id` and `event_type`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/calendar/ui/CalendarViewModel.kt` — `createEvent` sets `eventType = if (bandId != null) "band_rehearsal" else "personal"`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/conflicts/data/ConflictsRepositoryImpl.kt` — Filters `status = "pending"`, vote inserts to `"vote"` column, dismiss sets `"cancelled"`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/data/VenueRepository.kt` — `createVenue` signature includes `venueType: String`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/data/VenueRepositoryImpl.kt` — Inserts `added_by` (not `created_by`), inserts `venue_type`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/ui/VenueViewModel.kt` — `VenueUiState` has `selectedVenueType` / `customVenueType`; `setVenueType()`, `setCustomVenueType()`, updated `addVenue()`

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/venues/ui/VenuesScreen.kt` — `AddVenueSheet` has `FilterChip` type selector + "Others" text field; `VenueRow` shows venue type label; `venueTypeLabel()` helper function added

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/home/ui/HomeScreen.kt` — `BandChip` shows `AsyncImage` (40×40 rounded square) with fallback initial letter

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/ui/BandsScreen.kt` — `BandCard` shows `AsyncImage` (46×46) with fallback initial letter

- `composeApp/src/commonMain/kotlin/com/bandapa/feature/band/ui/BandDetailScreen.kt` — `BandInfoSection` shows full-width 180dp cover image when `imageUrl` is set

- `README.md` — Fully regenerated; reflects current architecture, schema, and setup steps

---

## Failed Attempts

- **Setting `defaultSchema = "bandapa-main"` in round 1 and changing all `@SerialName` values** — Failed because the original app tables were in `public.*` and domain models were already correct for them. Changing `@SerialName` values (e.g. `owner_id`→`created_by`) broke all data reads. Everything was reverted except the NavGraph fix, then the proper migration was done later once the DB structure was confirmed via `mcp__supabase__list_tables`.

- **NavGraph fix alone** — The `isFirstComposition` guard was correct and necessary but not sufficient. The app still crashed because the `announcements` table was in `bandapa-main` while the app was querying `public.announcements`. The real crash mechanism: `async {}` inside a non-`supervisorScope` `launch` throws `PostgrestException`, which propagates through the job hierarchy past the `try/catch` to Android's default uncaught exception handler.

- **`get_band_by_invite_code` RPC param name** — The original DB function used parameter `p_code`; the code was passing `code`. Fixed both: the DB function was updated to return `id` (instead of `band_id`) and the code now passes `p_code`.

- **`get_band_by_invite_code` return type conflict** — First migration attempt failed with `ERROR: cannot change return type of existing function`. Fixed by using `DROP FUNCTION IF EXISTS` before `CREATE FUNCTION` in the migration.

- **Gradle daemon OOM** — First rebuild attempt crashed the JVM (`hs_err_pid5168.log`) due to 7 busy background daemons. Resolution: always use `--no-daemon` flag.

- **Duplicate `ContentScale` import in HomeScreen.kt** — Adding the import manually when it was already present caused a compile error. Fixed by removing the duplicate.

---

## Next Step

**Install the current APK and verify the band "erar" loads correctly end-to-end:**
1. Home screen "My Bands" section shows band "erar" with its cover image thumbnail.
2. Bands tab shows the same card with image.
3. Tapping it opens Band Detail with the full-width cover image.
4. Username login with `erar-user` succeeds (tests `get_email_by_username` querying `bandapa-main.users`).

If the band doesn't appear, run this to diagnose:
```sql
-- Check RLS is allowing the user to see their band_member row
SELECT * FROM "bandapa-main".band_members WHERE user_id = '3cb86ca7-be4a-44a6-a9ed-c634e8f5e357';
SELECT * FROM "bandapa-main".bands WHERE id = '92c2994f-4109-47ad-85af-7b437b7a4bb3';
```

If `getBandByInviteCode` fails (JoinBand screen), verify the RPC param name matches:
```sql
SELECT * FROM "bandapa-main".get_band_by_invite_code('C862E6');
```

---

## Context & Gotchas

**Supabase project ref:** `rrfelwwoypouqcjbdzrb`  
**App package:** `com.bandapa` · Min SDK 26 · Target SDK 35 · Kotlin 2.1.21 · Compose Multiplatform 1.7.3  
**supabase-kt version:** 3.1.4

**Build command (always use `--no-daemon`):**
```powershell
$env:JAVA_HOME = "C:\Program Files (x86)\Android\openjdk\jdk-17.0.8.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :composeApp:assembleDebug --no-daemon
```

**APK upload:**
```powershell
$anonKey = ((Get-Content "local.properties") | Where-Object { $_ -match "^supabase\.anon_key=" }) -replace "^supabase\.anon_key=", ""
Invoke-RestMethod -Uri "https://rrfelwwoypouqcjbdzrb.supabase.co/storage/v1/object/releases/bandapa-latest.apk" -Method Post -Headers @{ "Authorization" = "Bearer $anonKey"; "x-upsert" = "true" } -ContentType "application/octet-stream" -InFile "composeApp\build\outputs\apk\debug\composeApp-debug.apk"
```

**Two schemas coexist in the DB:**
| Schema | Status |
|---|---|
| `public` | Legacy — 1 profile, 1 band, 1 band_member still present. Not read by app. Not dropped. |
| `bandapa-main` | Active — all app data lives here. Same 1 user, 1 band, 1 band_member (migrated in). |

**Complete column rename map (public → bandapa-main):**
| public column | bandapa-main column |
|---|---|
| `profiles.name` | `users.full_name` |
| `profiles` (table) | `users` (table) |
| `bands.owner_id` | `bands.created_by` |
| `bands.spotify_url` | `bands.spotify_artist_id` |
| `band_members.role` (text) | `band_members.is_admin` (boolean) |
| `events.user_id` | `events.owner_id` |
| `conflicts.event_a_id` | `conflicts.band_event_id` |
| `conflicts.event_b_id` | `conflicts.personal_event_id` |
| `conflicts.status` values: `"open"/"resolved"` | `"pending"/"cancelled"/"greenlit"` |
| `conflict_votes.voted_for` (UUID FK to events) | `conflict_votes.vote` (text: `'cancel'`/`'greenlit'`) |
| `venues.created_by` | `venues.added_by` |

**Conflict voting is fundamentally broken for `bandapa-main`.** The `ConflictsViewModel.vote(conflictId, eventId)` API passes a UUID event ID, but `bandapa-main.conflict_votes.vote` has a CHECK constraint accepting only `'cancel'` or `'greenlit'`. The `ConflictsScreen` and `ConflictsViewModel` need to be redesigned: replace "vote for one event" UI with "cancel or greenlight" buttons. There are currently 0 conflicts in the DB so this is not user-facing yet.

**`bandapa-main.events.event_type` is NOT NULL** (values: `personal`, `band_rehearsal`, `studio_recording`, `hangout`). The `CalendarViewModel` only sets `"personal"` or `"band_rehearsal"`. If the calendar UI should support `"studio_recording"` or `"hangout"`, extend `createEvent()` in `CalendarViewModel`.

**`BandMember.role` is `@Transient`** — not serialized. The actual admin state flows through `isAdmin`. `role` is only used for display in the UI (`RoleBadge`, `canRemove` check).

**Profile picture bucket:** `avatars` — stored at `{userId}/avatar.jpg` with upsert. Bucket is public.  
**Band image bucket:** `band-images` — stored at `{bandId}/cover.jpg`. Bucket is public.

**`local.properties` is git-ignored.** Required keys: `sdk.dir`, `supabase.url`, `supabase.anon_key`, `google.maps.api_key`. Template at `local.properties.example`.

**The `announcements` table** (`bandapa-main.announcements`) exists and is empty (0 rows). `HomeViewModel` still wraps the fetch in `runCatching { }.getOrElse { emptyList() }` as a safety net. The Realtime subscription (`newAnnouncementsFlow`) targets `bandapa-main` and will work once rows are inserted.

**`bandapa-main.admin_users`** has 1 row (the existing user). This table exists in the DB but is not used by any app code currently.
