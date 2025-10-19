-- ============================================================================
-- POPULATE likes_count for existing users
-- This script counts existing likes and updates user_stats accordingly
-- Run this AFTER adding the likes_count column
-- ============================================================================

-- Populate likes_count based on existing post_likes records
UPDATE user_stats us
SET likes_count = COALESCE((
    SELECT COUNT(*)
    FROM post_likes pl
    WHERE pl.user_id = us.user_id::bigint
), 0);

-- Verify the update
SELECT
    us.user_id,
    us.likes_count as stats_likes_count,
    COALESCE(COUNT(pl.like_id), 0) as actual_likes_count,
    CASE
        WHEN us.likes_count = COALESCE(COUNT(pl.like_id), 0) THEN '✓ Match'
        ELSE '✗ Mismatch'
    END as status
FROM user_stats us
LEFT JOIN post_likes pl ON pl.user_id = us.user_id::bigint
GROUP BY us.user_id, us.likes_count
ORDER BY us.likes_count DESC
LIMIT 10;

-- Summary statistics
SELECT
    COUNT(*) as total_users,
    SUM(likes_count) as total_likes,
    AVG(likes_count) as avg_likes_per_user,
    MAX(likes_count) as max_likes
FROM user_stats;
