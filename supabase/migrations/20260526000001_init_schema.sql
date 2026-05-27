-- ============================================================
-- Migration: 001 — Create bandapa-main schema and all tables
-- ============================================================

CREATE SCHEMA IF NOT EXISTS "bandapa-main";

-- ── Users ──────────────────────────────────────────────────
-- Extends auth.users with profile data
CREATE TABLE "bandapa-main".users (
  id               uuid        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  username         text        UNIQUE NOT NULL,
  full_name        text,
  first_name       text,
  last_name        text,
  contact_number   text,
  display_picture  text,                          -- Supabase Storage URL
  instruments      jsonb       NOT NULL DEFAULT '[]'::jsonb,
  created_at       timestamptz NOT NULL DEFAULT now()
);

-- ── Bands ──────────────────────────────────────────────────
CREATE TABLE "bandapa-main".bands (
  id                 uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  name               text        NOT NULL,
  description        text,
  genres             jsonb       NOT NULL DEFAULT '[]'::jsonb,
  date_formed        date        NOT NULL DEFAULT CURRENT_DATE,
  label              text,
  spotify_artist_id  text,
  invite_code        char(6)     UNIQUE NOT NULL,
  created_by         uuid        REFERENCES "bandapa-main".users(id) ON DELETE SET NULL,
  created_at         timestamptz NOT NULL DEFAULT now()
);

-- ── Band Members ───────────────────────────────────────────
CREATE TABLE "bandapa-main".band_members (
  id         uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  band_id    uuid        NOT NULL REFERENCES "bandapa-main".bands(id) ON DELETE CASCADE,
  user_id    uuid        NOT NULL REFERENCES "bandapa-main".users(id) ON DELETE CASCADE,
  is_admin   boolean     NOT NULL DEFAULT false,
  joined_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE(band_id, user_id)
);

-- ── Albums ─────────────────────────────────────────────────
CREATE TABLE "bandapa-main".albums (
  id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  band_id      uuid        NOT NULL REFERENCES "bandapa-main".bands(id) ON DELETE CASCADE,
  name         text        NOT NULL,
  description  text,
  tracks       jsonb       NOT NULL DEFAULT '[]'::jsonb,  -- [{title, duration, order}]
  cover_url    text,                                       -- Supabase Storage URL
  release_date date,
  created_at   timestamptz NOT NULL DEFAULT now()
);

-- ── Events (personal + band) ───────────────────────────────
CREATE TABLE "bandapa-main".events (
  id               uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  title            text        NOT NULL,
  event_type       text        NOT NULL
                               CHECK (event_type IN (
                                 'personal',
                                 'band_rehearsal',
                                 'studio_recording',
                                 'hangout'
                               )),
  owner_id         uuid        NOT NULL REFERENCES "bandapa-main".users(id) ON DELETE CASCADE,
  band_id          uuid        REFERENCES "bandapa-main".bands(id) ON DELETE CASCADE,
  start_time       timestamptz NOT NULL,
  end_time         timestamptz NOT NULL,
  is_recurring     boolean     NOT NULL DEFAULT false,
  recurrence_rule  text,       -- RRULE string e.g. FREQ=WEEKLY;BYDAY=MO
  created_at       timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT events_end_after_start      CHECK (end_time > start_time),
  CONSTRAINT events_personal_no_band     CHECK (
    (event_type = 'personal' AND band_id IS NULL) OR
    (event_type != 'personal' AND band_id IS NOT NULL)
  )
);

-- ── Conflicts ──────────────────────────────────────────────
CREATE TABLE "bandapa-main".conflicts (
  id                uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  band_event_id     uuid        NOT NULL REFERENCES "bandapa-main".events(id) ON DELETE CASCADE,
  personal_event_id uuid        NOT NULL REFERENCES "bandapa-main".events(id) ON DELETE CASCADE,
  reported_by       uuid        NOT NULL REFERENCES "bandapa-main".users(id) ON DELETE CASCADE,
  status            text        NOT NULL DEFAULT 'pending'
                                CHECK (status IN ('pending', 'cancelled', 'greenlit')),
  created_at        timestamptz NOT NULL DEFAULT now()
);

-- ── Conflict Votes ─────────────────────────────────────────
CREATE TABLE "bandapa-main".conflict_votes (
  id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  conflict_id  uuid        NOT NULL REFERENCES "bandapa-main".conflicts(id) ON DELETE CASCADE,
  user_id      uuid        NOT NULL REFERENCES "bandapa-main".users(id) ON DELETE CASCADE,
  vote         text        NOT NULL CHECK (vote IN ('cancel', 'greenlit')),
  voted_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE(conflict_id, user_id)
);

-- ── Venues ─────────────────────────────────────────────────
CREATE TABLE "bandapa-main".venues (
  id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  name         text        NOT NULL,
  description  text,
  venue_type   text        NOT NULL
               CHECK (venue_type IN ('studio', 'bar', 'hangout_place')),
  address      text        NOT NULL,
  lat          numeric(9,6),
  lng          numeric(9,6),
  added_by     uuid        REFERENCES "bandapa-main".users(id) ON DELETE SET NULL,
  created_at   timestamptz NOT NULL DEFAULT now()
);
