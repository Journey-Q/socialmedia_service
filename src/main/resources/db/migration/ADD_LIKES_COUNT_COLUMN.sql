-- ============================================================================
-- SIMPLE VERSION: Add likes_count column to user_stats table
-- Run this SQL directly in your PostgreSQL database (DataGrip, pgAdmin, etc.)
-- ============================================================================

-- Step 1: Add the likes_count column if it doesn't exist
ALTER TABLE user_stats
ADD COLUMN IF NOT EXISTS likes_count INTEGER NOT NULL DEFAULT 0;

-- Step 2: Create index for performance
CREATE INDEX IF NOT EXISTS idx_stats_likes_count ON user_stats(likes_count);

-- Step 3: Initialize existing rows to 0
UPDATE user_stats
SET likes_count = 0
WHERE likes_count IS NULL;

-- Step 4: Verify the column was added successfully
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'user_stats'
AND column_name = 'likes_count';

-- Step 5: View sample data
SELECT
    user_id,
    followers_count,
    following_count,
    posts_count,
    likes_count
FROM user_stats
LIMIT 5;
