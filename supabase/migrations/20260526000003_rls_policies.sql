-- ============================================================
-- Migration: 003 — Row Level Security policies
-- ============================================================

-- Enable RLS on every table
ALTER TABLE "bandapa-main".users          ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".bands          ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".band_members   ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".albums         ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".events         ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".conflicts      ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".conflict_votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE "bandapa-main".venues         ENABLE ROW LEVEL SECURITY;

-- ── users ──────────────────────────────────────────────────
-- Any authenticated user can view any profile (needed for directory / band member lookup)
CREATE POLICY "users: read all"
  ON "bandapa-main".users FOR SELECT
  USING (true);

-- Users may only create their own profile row
CREATE POLICY "users: insert own"
  ON "bandapa-main".users FOR INSERT
  WITH CHECK (id = auth.uid());

-- Users may only update their own profile
CREATE POLICY "users: update own"
  ON "bandapa-main".users FOR UPDATE
  USING (id = auth.uid());

-- ── bands ──────────────────────────────────────────────────
-- Any authenticated user can browse bands (needed for invite code preview)
CREATE POLICY "bands: read all"
  ON "bandapa-main".bands FOR SELECT
  USING (true);

-- Any authenticated user can create a band
CREATE POLICY "bands: insert authenticated"
  ON "bandapa-main".bands FOR INSERT
  WITH CHECK (auth.uid() IS NOT NULL);

-- Only band admins can update band details
CREATE POLICY "bands: update admin only"
  ON "bandapa-main".bands FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM "bandapa-main".band_members
      WHERE band_id = bands.id
        AND user_id  = auth.uid()
        AND is_admin = true
    )
  );

-- Only the creator can delete a band
CREATE POLICY "bands: delete creator only"
  ON "bandapa-main".bands FOR DELETE
  USING (created_by = auth.uid());

-- ── band_members ───────────────────────────────────────────
-- A user can see members of any band they belong to
CREATE POLICY "band_members: read own bands"
  ON "bandapa-main".band_members FOR SELECT
  USING (
    user_id = auth.uid()
    OR EXISTS (
      SELECT 1 FROM "bandapa-main".band_members bm
      WHERE bm.band_id = band_members.band_id
        AND bm.user_id = auth.uid()
    )
  );

-- A user may add themselves (join via invite code)
CREATE POLICY "band_members: join self"
  ON "bandapa-main".band_members FOR INSERT
  WITH CHECK (user_id = auth.uid());

-- A band admin may add other users
CREATE POLICY "band_members: admin add others"
  ON "bandapa-main".band_members FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM "bandapa-main".band_members bm
      WHERE bm.band_id = band_members.band_id
        AND bm.user_id  = auth.uid()
        AND bm.is_admin = true
    )
  );

-- A band admin may change roles / details of other members
CREATE POLICY "band_members: admin update"
  ON "bandapa-main".band_members FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM "bandapa-main".band_members bm
      WHERE bm.band_id = band_members.band_id
        AND bm.user_id  = auth.uid()
        AND bm.is_admin = true
    )
  );

-- A member may remove themselves; admins may remove anyone
CREATE POLICY "band_members: leave or admin remove"
  ON "bandapa-main".band_members FOR DELETE
  USING (
    user_id = auth.uid()
    OR EXISTS (
      SELECT 1 FROM "bandapa-main".band_members bm
      WHERE bm.band_id = band_members.band_id
        AND bm.user_id  = auth.uid()
        AND bm.is_admin = true
    )
  );

-- ── albums ─────────────────────────────────────────────────
-- Band members can read albums of their bands
CREATE POLICY "albums: read band members"
  ON "bandapa-main".albums FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM "bandapa-main".band_members bm
      WHERE bm.band_id = albums.band_id
        AND bm.user_id = auth.uid()
    )
  );

-- Only admins can create / modify / delete albums
CREATE POLICY "albums: write band admin"
  ON "bandapa-main".albums FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM "bandapa-main".band_members bm
      WHERE bm.band_id = albums.band_id
        AND bm.user_id  = auth.uid()
        AND bm.is_admin = true
    )
  );

-- ── events ─────────────────────────────────────────────────
-- Personal events: visible to owner only
-- Band events: visible to all band members
CREATE POLICY "events: read own or band"
  ON "bandapa-main".events FOR SELECT
  USING (
    owner_id = auth.uid()
    OR (
      band_id IS NOT NULL
      AND EXISTS (
        SELECT 1 FROM "bandapa-main".band_members bm
        WHERE bm.band_id = events.band_id
          AND bm.user_id = auth.uid()
      )
    )
  );

-- Any authenticated band member may create an event (personal or band)
CREATE POLICY "events: insert authenticated member"
  ON "bandapa-main".events FOR INSERT
  WITH CHECK (
    owner_id = auth.uid()
    AND (
      band_id IS NULL
      OR EXISTS (
        SELECT 1 FROM "bandapa-main".band_members bm
        WHERE bm.band_id = events.band_id
          AND bm.user_id = auth.uid()
      )
    )
  );

-- Owner of a personal event, or any band member, may update band events
CREATE POLICY "events: update owner or band member"
  ON "bandapa-main".events FOR UPDATE
  USING (
    owner_id = auth.uid()
    OR (
      band_id IS NOT NULL
      AND EXISTS (
        SELECT 1 FROM "bandapa-main".band_members bm
        WHERE bm.band_id = events.band_id
          AND bm.user_id = auth.uid()
      )
    )
  );

-- Only the event owner can delete it
CREATE POLICY "events: delete owner only"
  ON "bandapa-main".events FOR DELETE
  USING (owner_id = auth.uid());

-- ── conflicts ──────────────────────────────────────────────
-- Visible to the reporter or any member of the affected band
CREATE POLICY "conflicts: read reporter or band"
  ON "bandapa-main".conflicts FOR SELECT
  USING (
    reported_by = auth.uid()
    OR EXISTS (
      SELECT 1
      FROM "bandapa-main".events      e
      JOIN "bandapa-main".band_members bm ON bm.band_id = e.band_id
      WHERE e.id        = conflicts.band_event_id
        AND bm.user_id  = auth.uid()
    )
  );

-- Any band member can report a conflict
CREATE POLICY "conflicts: insert band member"
  ON "bandapa-main".conflicts FOR INSERT
  WITH CHECK (reported_by = auth.uid());

-- Band members (including reporter) can update status
CREATE POLICY "conflicts: update band member"
  ON "bandapa-main".conflicts FOR UPDATE
  USING (
    EXISTS (
      SELECT 1
      FROM "bandapa-main".events      e
      JOIN "bandapa-main".band_members bm ON bm.band_id = e.band_id
      WHERE e.id       = conflicts.band_event_id
        AND bm.user_id = auth.uid()
    )
  );

-- ── conflict_votes ─────────────────────────────────────────
-- Members of the affected band can read and cast votes
CREATE POLICY "conflict_votes: read band members"
  ON "bandapa-main".conflict_votes FOR SELECT
  USING (
    user_id = auth.uid()
    OR EXISTS (
      SELECT 1
      FROM "bandapa-main".conflicts    c
      JOIN "bandapa-main".events       e  ON e.id = c.band_event_id
      JOIN "bandapa-main".band_members bm ON bm.band_id = e.band_id
      WHERE c.id       = conflict_votes.conflict_id
        AND bm.user_id = auth.uid()
    )
  );

-- A user may only cast their own vote, and only if they're a band member
CREATE POLICY "conflict_votes: insert own vote"
  ON "bandapa-main".conflict_votes FOR INSERT
  WITH CHECK (
    user_id = auth.uid()
    AND EXISTS (
      SELECT 1
      FROM "bandapa-main".conflicts    c
      JOIN "bandapa-main".events       e  ON e.id = c.band_event_id
      JOIN "bandapa-main".band_members bm ON bm.band_id = e.band_id
      WHERE c.id       = conflict_votes.conflict_id
        AND bm.user_id = auth.uid()
    )
  );

-- A user may change their own vote
CREATE POLICY "conflict_votes: update own vote"
  ON "bandapa-main".conflict_votes FOR UPDATE
  USING (user_id = auth.uid());

-- ── venues ─────────────────────────────────────────────────
-- Global: all users can read venues
CREATE POLICY "venues: read all"
  ON "bandapa-main".venues FOR SELECT
  USING (true);

-- Any authenticated user can add a venue
CREATE POLICY "venues: insert authenticated"
  ON "bandapa-main".venues FOR INSERT
  WITH CHECK (auth.uid() IS NOT NULL);

-- Only the creator can edit or delete a venue
CREATE POLICY "venues: update own"
  ON "bandapa-main".venues FOR UPDATE
  USING (added_by = auth.uid());

CREATE POLICY "venues: delete own"
  ON "bandapa-main".venues FOR DELETE
  USING (added_by = auth.uid());
