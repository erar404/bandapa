-- Migration: 008 — Add latitude/longitude to venues
ALTER TABLE "bandapa-main".venues
  ADD COLUMN IF NOT EXISTS latitude  double precision,
  ADD COLUMN IF NOT EXISTS longitude double precision;
