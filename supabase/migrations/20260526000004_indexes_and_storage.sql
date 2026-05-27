-- ============================================================
-- Migration: 004 — Performance indexes and Storage buckets
-- ============================================================

-- ── Indexes ────────────────────────────────────────────────

-- band_members — frequent joins on both sides
CREATE INDEX idx_band_members_user_id  ON "bandapa-main".band_members(user_id);
CREATE INDEX idx_band_members_band_id  ON "bandapa-main".band_members(band_id);
CREATE INDEX idx_band_members_admin    ON "bandapa-main".band_members(band_id, is_admin);

-- albums
CREATE INDEX idx_albums_band_id  ON "bandapa-main".albums(band_id);

-- events — calendar queries always filter on time range
CREATE INDEX idx_events_owner_id   ON "bandapa-main".events(owner_id);
CREATE INDEX idx_events_band_id    ON "bandapa-main".events(band_id);
CREATE INDEX idx_events_time_range ON "bandapa-main".events(start_time, end_time);

-- conflicts — checked when displaying open items
CREATE INDEX idx_conflicts_band_event ON "bandapa-main".conflicts(band_event_id);
CREATE INDEX idx_conflicts_status     ON "bandapa-main".conflicts(status)
  WHERE status = 'pending';

-- conflict_votes
CREATE INDEX idx_conflict_votes_conflict ON "bandapa-main".conflict_votes(conflict_id);

-- venues — name search for duplicate detection
CREATE INDEX idx_venues_name ON "bandapa-main".venues(lower(name));

-- bands — invite code lookup
CREATE INDEX idx_bands_invite_code ON "bandapa-main".bands(invite_code);

-- ── Supabase Storage buckets ───────────────────────────────

-- Profile pictures
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'avatars',
  'avatars',
  true,
  5242880,  -- 5 MB
  ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO NOTHING;

-- Album cover art
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'album-covers',
  'album-covers',
  true,
  10485760,  -- 10 MB
  ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO NOTHING;

-- ── Storage RLS policies ───────────────────────────────────

-- avatars: anyone can view; only owner can upload/replace their own avatar
CREATE POLICY "avatars: public read"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'avatars');

CREATE POLICY "avatars: owner upload"
  ON storage.objects FOR INSERT
  WITH CHECK (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  );

CREATE POLICY "avatars: owner update"
  ON storage.objects FOR UPDATE
  USING (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  );

CREATE POLICY "avatars: owner delete"
  ON storage.objects FOR DELETE
  USING (
    bucket_id = 'avatars'
    AND auth.uid()::text = (storage.foldername(name))[1]
  );

-- album-covers: anyone can view; only band admins can upload
CREATE POLICY "album-covers: public read"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'album-covers');

CREATE POLICY "album-covers: band admin upload"
  ON storage.objects FOR INSERT
  WITH CHECK (
    bucket_id = 'album-covers'
    AND auth.uid() IS NOT NULL
  );

CREATE POLICY "album-covers: band admin delete"
  ON storage.objects FOR DELETE
  USING (
    bucket_id = 'album-covers'
    AND auth.uid() IS NOT NULL
  );
