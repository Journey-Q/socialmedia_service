-- Add likes_count column to user_stats table
-- This tracks the total number of likes a user has given to posts

-- Check if column exists before adding (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'user_stats'
        AND column_name = 'likes_count'
    ) THEN
        -- Add the likes_count column
        ALTER TABLE user_stats
        ADD COLUMN likes_count INTEGER NOT NULL DEFAULT 0;

        RAISE NOTICE 'Added likes_count column to user_stats table';
    ELSE
        RAISE NOTICE 'likes_count column already exists in user_stats table';
    END IF;
END $$;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_stats_likes_count ON user_stats(likes_count);

-- Set default value for existing rows (if any don't have it)
UPDATE user_stats
SET likes_count = 0
WHERE likes_count IS NULL;

-- Verify the column was added
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'user_stats' AND column_name = 'likes_count';
