-- ============================================================
-- Migration: 002 — Functions and triggers
-- ============================================================

-- ── Invite code generator ──────────────────────────────────
-- Generates a unique 6-char alphanumeric code (A-Z, 0-9)
CREATE OR REPLACE FUNCTION "bandapa-main".generate_invite_code()
RETURNS char(6)
LANGUAGE plpgsql AS $$
DECLARE
  chars      text    := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  code       text    := '';
  i          int;
  already_used boolean;
BEGIN
  LOOP
    code := '';
    FOR i IN 1..6 LOOP
      code := code || substr(chars, floor(random() * length(chars) + 1)::int, 1);
    END LOOP;
    SELECT EXISTS(
      SELECT 1 FROM "bandapa-main".bands WHERE invite_code = code
    ) INTO already_used;
    EXIT WHEN NOT already_used;
  END LOOP;
  RETURN code;
END;
$$;

-- Auto-assign invite code before INSERT on bands if not provided
CREATE OR REPLACE FUNCTION "bandapa-main".trg_set_band_invite_code()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.invite_code IS NULL OR trim(NEW.invite_code) = '' THEN
    NEW.invite_code := "bandapa-main".generate_invite_code();
  END IF;
  RETURN NEW;
END;
$$;

CREATE TRIGGER band_set_invite_code
  BEFORE INSERT ON "bandapa-main".bands
  FOR EACH ROW
  EXECUTE FUNCTION "bandapa-main".trg_set_band_invite_code();

-- ── Auto-create user profile on sign-up ───────────────────
-- Placed in public schema so it can reference auth.users via trigger
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public AS $$
BEGIN
  INSERT INTO "bandapa-main".users (
    id,
    username,
    full_name,
    first_name,
    last_name
  )
  VALUES (
    NEW.id,
    -- Use provided username; fall back to email prefix to satisfy NOT NULL
    COALESCE(
      NEW.raw_user_meta_data->>'username',
      split_part(NEW.email, '@', 1)
    ),
    COALESCE(NEW.raw_user_meta_data->>'full_name',  ''),
    COALESCE(NEW.raw_user_meta_data->>'first_name', ''),
    COALESCE(NEW.raw_user_meta_data->>'last_name',  '')
  )
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$;

-- Fires after every new auth.users row (sign-up / OAuth)
CREATE OR REPLACE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();

-- ── Auto-resolve conflicts after a vote is cast ───────────
-- Resolution rules:
--   • Any single "cancel" vote → conflict is cancelled
--   • All band members vote "greenlit" → conflict is greenlit
CREATE OR REPLACE FUNCTION "bandapa-main".trg_resolve_conflict_on_vote()
RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  v_total_members  int;
  v_cancel_votes   int;
  v_greenlit_votes int;
BEGIN
  -- Total members in the band of the conflicting band event
  SELECT COUNT(*) INTO v_total_members
  FROM "bandapa-main".band_members bm
  JOIN "bandapa-main".events      e  ON e.band_id = bm.band_id
  JOIN "bandapa-main".conflicts   c  ON c.band_event_id = e.id
  WHERE c.id = NEW.conflict_id;

  -- Tally current votes for this conflict
  SELECT
    COUNT(*) FILTER (WHERE vote = 'cancel'),
    COUNT(*) FILTER (WHERE vote = 'greenlit')
  INTO v_cancel_votes, v_greenlit_votes
  FROM "bandapa-main".conflict_votes
  WHERE conflict_id = NEW.conflict_id;

  IF v_cancel_votes > 0 THEN
    UPDATE "bandapa-main".conflicts
    SET status = 'cancelled'
    WHERE id = NEW.conflict_id AND status = 'pending';

  ELSIF v_greenlit_votes >= v_total_members THEN
    UPDATE "bandapa-main".conflicts
    SET status = 'greenlit'
    WHERE id = NEW.conflict_id AND status = 'pending';
  END IF;

  RETURN NEW;
END;
$$;

CREATE TRIGGER conflict_vote_resolve
  AFTER INSERT OR UPDATE ON "bandapa-main".conflict_votes
  FOR EACH ROW
  EXECUTE FUNCTION "bandapa-main".trg_resolve_conflict_on_vote();

-- ── Helper: look up a band by invite code ─────────────────
-- Returns band details + member count for the join preview screen
CREATE OR REPLACE FUNCTION "bandapa-main".get_band_by_invite_code(p_code char(6))
RETURNS TABLE (
  band_id      uuid,
  name         text,
  description  text,
  genres       jsonb,
  date_formed  date,
  member_count bigint
)
LANGUAGE sql
STABLE AS $$
  SELECT
    b.id,
    b.name,
    b.description,
    b.genres,
    b.date_formed,
    COUNT(bm.id) AS member_count
  FROM "bandapa-main".bands       b
  LEFT JOIN "bandapa-main".band_members bm ON bm.band_id = b.id
  WHERE b.invite_code = upper(p_code)
  GROUP BY b.id;
$$;

-- ── Helper: detect overlapping events for a user ──────────
-- Returns all events that overlap [p_start, p_end) for a given user,
-- used by the mobile app before saving a new event.
CREATE OR REPLACE FUNCTION "bandapa-main".get_overlapping_events(
  p_user_id  uuid,
  p_start    timestamptz,
  p_end      timestamptz,
  p_band_ids uuid[] DEFAULT NULL  -- band IDs to check band calendars for
)
RETURNS SETOF "bandapa-main".events
LANGUAGE sql
STABLE AS $$
  SELECT e.*
  FROM "bandapa-main".events e
  WHERE
    e.start_time < p_end
    AND e.end_time > p_start
    AND (
      -- Personal events owned by the user
      e.owner_id = p_user_id
      -- Band events in any of the user's bands
      OR (p_band_ids IS NOT NULL AND e.band_id = ANY(p_band_ids))
    );
$$;
