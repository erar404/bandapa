-- ============================================================
-- Migration: 005 — Web service tables (artists, announcements, admin_users)
-- ============================================================

-- ── Artists ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS "bandapa-main".artists (
  id                uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  name              text        NOT NULL,
  bio               text,
  photo_url         text,
  genres            jsonb       NOT NULL DEFAULT '[]'::jsonb,
  instruments       jsonb       NOT NULL DEFAULT '[]'::jsonb,
  spotify_artist_id text,
  created_at        timestamptz NOT NULL DEFAULT now(),
  updated_at        timestamptz NOT NULL DEFAULT now()
);

-- ── Announcements ──────────────────────────────────────────
-- Admins write here; the mobile app listens via Supabase Realtime
CREATE TABLE IF NOT EXISTS "bandapa-main".announcements (
  id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
  title       text        NOT NULL,
  body        text        NOT NULL,
  is_active   boolean     NOT NULL DEFAULT true,
  created_by  uuid        REFERENCES auth.users(id) ON DELETE SET NULL,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now()
);

-- ── Admin users ────────────────────────────────────────────
-- Rows here grant admin portal access; managed manually or via service role
CREATE TABLE IF NOT EXISTS "bandapa-main".admin_users (
  user_id    uuid        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- ── RLS ────────────────────────────────────────────────────
ALTER TABLE "bandapa-main".artists ENABLE ROW LEVEL SECURITY;

CREATE POLICY "artists_select_all"
  ON "bandapa-main".artists FOR SELECT
  TO authenticated, anon USING (true);

CREATE POLICY "artists_admin_write"
  ON "bandapa-main".artists FOR ALL
  TO authenticated
  USING (EXISTS (SELECT 1 FROM "bandapa-main".admin_users WHERE user_id = auth.uid()))
  WITH CHECK (EXISTS (SELECT 1 FROM "bandapa-main".admin_users WHERE user_id = auth.uid()));

ALTER TABLE "bandapa-main".announcements ENABLE ROW LEVEL SECURITY;

CREATE POLICY "announcements_select_authenticated"
  ON "bandapa-main".announcements FOR SELECT
  TO authenticated USING (true);

CREATE POLICY "announcements_admin_write"
  ON "bandapa-main".announcements FOR ALL
  TO authenticated
  USING (EXISTS (SELECT 1 FROM "bandapa-main".admin_users WHERE user_id = auth.uid()))
  WITH CHECK (EXISTS (SELECT 1 FROM "bandapa-main".admin_users WHERE user_id = auth.uid()));

ALTER TABLE "bandapa-main".admin_users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "admin_users_select_self"
  ON "bandapa-main".admin_users FOR SELECT
  TO authenticated USING (user_id = auth.uid());

-- ── updated_at trigger ─────────────────────────────────────
CREATE OR REPLACE FUNCTION "bandapa-main".set_updated_at()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

CREATE TRIGGER artists_set_updated_at
  BEFORE UPDATE ON "bandapa-main".artists
  FOR EACH ROW EXECUTE FUNCTION "bandapa-main".set_updated_at();

CREATE TRIGGER announcements_set_updated_at
  BEFORE UPDATE ON "bandapa-main".announcements
  FOR EACH ROW EXECUTE FUNCTION "bandapa-main".set_updated_at();
