# Database Migration for Post Deletion Fix

## Problem
When deleting a post, you're getting a foreign key violation error because the database doesn't have `ON DELETE CASCADE` configured on the foreign key constraints.

## Solution
Run the SQL script to add `ON DELETE CASCADE` to all foreign key constraints related to posts.

## How to Run

### Option 1: Using pgAdmin or PostgreSQL GUI Client
1. Connect to your database: `database-1-instance-1.c9caq2mcm8op.ap-south-1.rds.amazonaws.com:5432`
2. Open the SQL query editor
3. Copy and paste the contents of `SIMPLE_FIX_CASCADE.sql`
4. Execute the script

### Option 2: Using psql Command Line
```bash
psql -h database-1-instance-1.c9caq2mcm8op.ap-south-1.rds.amazonaws.com \
     -p 5432 \
     -U postgres \
     -d postgres \
     -f SIMPLE_FIX_CASCADE.sql
```

### Option 3: Using DBeaver or Similar Tools
1. Create a new SQL script
2. Copy the contents of `SIMPLE_FIX_CASCADE.sql`
3. Execute the script

## What This Does

The script will:
1. Drop existing foreign key constraints (if they exist)
2. Recreate them with `ON DELETE CASCADE`
3. Verify the constraints are correctly set

After running this script, when you delete a post:
- All `post_content` records will be automatically deleted
- All `place_wise_content` records will be automatically deleted
- All `post_likes` records will be automatically deleted
- All `post_comments` records will be automatically deleted

## Tables Affected
- `post_content` → `posts` (ON DELETE CASCADE)
- `place_wise_content` → `posts` (ON DELETE CASCADE)
- `post_likes` → `posts` (ON DELETE CASCADE)
- `post_comments` → `posts` (ON DELETE CASCADE)

## Verification
After running the script, you can verify the constraints with:
```sql
SELECT
    tc.table_name,
    tc.constraint_name,
    rc.delete_rule
FROM information_schema.table_constraints tc
JOIN information_schema.referential_constraints rc
    ON tc.constraint_name = rc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND tc.table_name IN ('post_content', 'place_wise_content', 'post_likes', 'post_comments')
ORDER BY tc.table_name;
```

You should see `delete_rule = 'CASCADE'` for all constraints.

## Important Notes
- **Backup your database** before running this script
- This script is **idempotent** - you can run it multiple times safely
- The service layer code has been updated to manually handle deletions as well, so this works as a **defense in depth** strategy
