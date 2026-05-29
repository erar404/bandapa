-- ============================================================
-- Migration: 007 — Add image_url to announcements + storage bucket
-- ============================================================

ALTER TABLE "bandapa-main".announcements
  ADD COLUMN IF NOT EXISTS image_url text;

INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'announcement-images',
  'announcement-images',
  true,
  10485760,
  ARRAY['image/jpeg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO NOTHING;
