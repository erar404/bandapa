-- ============================================================
-- Migration: 006 — Enable Realtime for announcements table
-- ============================================================

-- Add announcements to the Supabase Realtime publication so mobile
-- clients can receive live INSERT events via the Realtime plugin.
ALTER PUBLICATION supabase_realtime ADD TABLE "bandapa-main".announcements;
