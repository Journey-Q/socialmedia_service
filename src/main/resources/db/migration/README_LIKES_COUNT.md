# Add Likes Count to User Stats - Migration Guide

## Overview
This migration adds a `likes_count` column to the `user_stats` table to track the total number of likes each user has given.

## Files
1. **ADD_LIKES_COUNT_COLUMN.sql** - Creates the column and index
2. **POPULATE_LIKES_COUNT.sql** - Populates data for existing users
3. **add_likes_count_to_user_stats.sql** - Idempotent version with checks

## Step-by-Step Instructions

### Step 1: Connect to Your Database
Connect to your PostgreSQL database using one of these tools:
- **DataGrip** (recommended)
- pgAdmin
- psql command line

**Connection Details:**
```
Host: database-1-instance-1.c9caq2mcm8op.ap-south-1.rds.amazonaws.com
Port: 5432
Database: postgres
Username: postgres
Password: [your password from application.properties]
```

### Step 2: Verify Current Schema
Before making changes, check the current user_stats table:

```sql
-- Check current columns
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'user_stats'
ORDER BY ordinal_position;

-- Check sample data
SELECT * FROM user_stats LIMIT 5;
```

### Step 3: Run the Migration
Execute the following SQL to add the column:

```sql
-- Add likes_count column
ALTER TABLE user_stats
ADD COLUMN IF NOT EXISTS likes_count INTEGER NOT NULL DEFAULT 0;

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_stats_likes_count ON user_stats(likes_count);
```

### Step 4: Populate Existing Data (Optional but Recommended)
If you have existing users who have already liked posts, update their counts:

```sql
-- Count existing likes for each user and update user_stats
UPDATE user_stats us
SET likes_count = COALESCE((
    SELECT COUNT(*)
    FROM post_likes pl
    WHERE pl.user_id = us.user_id::bigint
), 0);
```

### Step 5: Verify the Migration
Check that everything worked correctly:

```sql
-- 1. Verify column exists
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'user_stats' AND column_name = 'likes_count';

-- 2. Verify index exists
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'user_stats' AND indexname = 'idx_stats_likes_count';

-- 3. Check sample data
SELECT
    user_id,
    followers_count,
    following_count,
    posts_count,
    likes_count
FROM user_stats
LIMIT 10;

-- 4. Verify counts match
SELECT
    us.user_id,
    us.likes_count as stats_count,
    COUNT(pl.like_id) as actual_count
FROM user_stats us
LEFT JOIN post_likes pl ON pl.user_id = us.user_id::bigint
GROUP BY us.user_id, us.likes_count
HAVING us.likes_count != COUNT(pl.like_id)
LIMIT 5;
```

Expected result: No rows (means counts match perfectly)

## What Happens Next

After running this migration, the application will automatically:

1. **When a user LIKES a post:**
   - Increment `user_stats.likes_count` by 1
   - Increment `posts.likes_count` by 1
   - Create record in `post_likes` table

2. **When a user UNLIKES a post:**
   - Decrement `user_stats.likes_count` by 1
   - Decrement `posts.likes_count` by 1
   - Delete record from `post_likes` table

## Rollback (If Needed)
If you need to undo this migration:

```sql
-- Remove the column
ALTER TABLE user_stats DROP COLUMN IF EXISTS likes_count;

-- Remove the index
DROP INDEX IF EXISTS idx_stats_likes_count;
```

## Testing

After migration, test the functionality:

1. **Test Like Action:**
```bash
# Like a post
curl -X POST http://localhost:8081/likes/toggle \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"postId": 1, "userId": 1}'

# Check user_stats
SELECT user_id, likes_count FROM user_stats WHERE user_id = '1';
```

2. **Test Unlike Action:**
```bash
# Unlike the same post
curl -X POST http://localhost:8081/likes/toggle \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"postId": 1, "userId": 1}'

# Check user_stats (should decrease by 1)
SELECT user_id, likes_count FROM user_stats WHERE user_id = '1';
```

## Monitoring Queries

**Check top users by likes:**
```sql
SELECT user_id, likes_count
FROM user_stats
ORDER BY likes_count DESC
LIMIT 10;
```

**Check users with mismatched counts:**
```sql
SELECT
    us.user_id,
    us.likes_count,
    COUNT(pl.like_id) as actual_likes
FROM user_stats us
LEFT JOIN post_likes pl ON pl.user_id = us.user_id::bigint
GROUP BY us.user_id, us.likes_count
HAVING us.likes_count != COUNT(pl.like_id);
```

**Summary statistics:**
```sql
SELECT
    COUNT(*) as total_users,
    SUM(likes_count) as total_likes,
    ROUND(AVG(likes_count), 2) as avg_likes_per_user,
    MAX(likes_count) as max_likes
FROM user_stats;
```

## Troubleshooting

### Issue: Column already exists
**Error:** `column "likes_count" of relation "user_stats" already exists`

**Solution:** This is fine! The column is already there. Just run the POPULATE script.

### Issue: Type casting error
**Error:** `cannot cast type character varying to bigint`

**Solution:** The `user_id` in user_stats is VARCHAR, while in post_likes it's BIGINT. The migration handles this with `::bigint` casting.

### Issue: Null values
**Error:** Some users have NULL in likes_count

**Solution:**
```sql
UPDATE user_stats SET likes_count = 0 WHERE likes_count IS NULL;
```

## Support

If you encounter issues:
1. Check the application logs
2. Verify database connection
3. Check that all SQL scripts ran successfully
4. Ensure the LikeService code is deployed

## Summary

✅ **Column Added:** `user_stats.likes_count INTEGER NOT NULL DEFAULT 0`
✅ **Index Created:** `idx_stats_likes_count`
✅ **Data Populated:** Existing likes counted and updated
✅ **Application Updated:** LikeService now updates the count automatically

🎉 **The likes count feature is ready to use!**
