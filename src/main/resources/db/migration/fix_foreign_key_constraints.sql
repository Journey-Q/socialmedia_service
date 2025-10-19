-- SQL Script to Fix Foreign Key Constraints for Post Deletion
-- Run this script manually on your PostgreSQL database to add ON DELETE CASCADE

-- This script fixes the foreign key constraints so that when a post is deleted,
-- all related records (post_content, place_wise_content, likes, comments) are also deleted

-- ============================================================================
-- IMPORTANT: Run this script manually using a PostgreSQL client or pgAdmin
-- ============================================================================

-- 1. Drop existing foreign key constraint on post_content table (if exists)
-- First, we need to find the constraint name
DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Find the constraint name for post_content.post_id -> posts.post_id
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'post_content'::regclass
    AND contype = 'f'
    AND confrelid = 'posts'::regclass;

    -- Drop the constraint if it exists
    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE post_content DROP CONSTRAINT IF EXISTS %I', constraint_name);
        RAISE NOTICE 'Dropped constraint: %', constraint_name;
    END IF;
END $$;

-- 2. Add new foreign key constraint with ON DELETE CASCADE for post_content
ALTER TABLE post_content
ADD CONSTRAINT fk_post_content_post
FOREIGN KEY (post_id)
REFERENCES posts(post_id)
ON DELETE CASCADE;

-- ============================================================================

-- 3. Drop existing foreign key constraint on place_wise_content table (if exists)
DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Find the constraint name for place_wise_content.post_id -> posts.post_id
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'place_wise_content'::regclass
    AND contype = 'f'
    AND confrelid = 'posts'::regclass;

    -- Drop the constraint if it exists
    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE place_wise_content DROP CONSTRAINT IF EXISTS %I', constraint_name);
        RAISE NOTICE 'Dropped constraint: %', constraint_name;
    END IF;
END $$;

-- 4. Add new foreign key constraint with ON DELETE CASCADE for place_wise_content
ALTER TABLE place_wise_content
ADD CONSTRAINT fk_place_wise_content_post
FOREIGN KEY (post_id)
REFERENCES posts(post_id)
ON DELETE CASCADE;

-- ============================================================================

-- 5. Drop existing foreign key constraint on post_likes table (if exists)
DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Find the constraint name for post_likes.post_id -> posts.post_id
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'post_likes'::regclass
    AND contype = 'f'
    AND confrelid = 'posts'::regclass;

    -- Drop the constraint if it exists
    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE post_likes DROP CONSTRAINT IF EXISTS %I', constraint_name);
        RAISE NOTICE 'Dropped constraint: %', constraint_name;
    END IF;
END $$;

-- 6. Add new foreign key constraint with ON DELETE CASCADE for post_likes
ALTER TABLE post_likes
ADD CONSTRAINT fk_post_likes_post
FOREIGN KEY (post_id)
REFERENCES posts(post_id)
ON DELETE CASCADE;

-- ============================================================================

-- 7. Drop existing foreign key constraint on post_comments table (if exists)
DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Find the constraint name for post_comments.post_id -> posts.post_id
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'post_comments'::regclass
    AND contype = 'f'
    AND confrelid = 'posts'::regclass;

    -- Drop the constraint if it exists
    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE post_comments DROP CONSTRAINT IF EXISTS %I', constraint_name);
        RAISE NOTICE 'Dropped constraint: %', constraint_name;
    END IF;
END $$;

-- 8. Add new foreign key constraint with ON DELETE CASCADE for post_comments
ALTER TABLE post_comments
ADD CONSTRAINT fk_post_comments_post
FOREIGN KEY (post_id)
REFERENCES posts(post_id)
ON DELETE CASCADE;

-- ============================================================================

-- Verification: Check all foreign key constraints
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
JOIN information_schema.referential_constraints AS rc
    ON rc.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND ccu.table_name = 'posts'
ORDER BY tc.table_name;
