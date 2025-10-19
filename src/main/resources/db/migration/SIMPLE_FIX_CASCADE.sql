-- SIMPLE VERSION: Run this if you want a quick fix
-- Copy and paste this into your PostgreSQL client

-- Drop and recreate post_content foreign key with CASCADE
ALTER TABLE post_content DROP CONSTRAINT IF EXISTS fk_post_content_post;
ALTER TABLE post_content DROP CONSTRAINT IF EXISTS post_content_post_id_fkey;
ALTER TABLE post_content
ADD CONSTRAINT fk_post_content_post
FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE;

-- Drop and recreate place_wise_content foreign key with CASCADE
ALTER TABLE place_wise_content DROP CONSTRAINT IF EXISTS fk_place_wise_content_post;
ALTER TABLE place_wise_content DROP CONSTRAINT IF EXISTS place_wise_content_post_id_fkey;
ALTER TABLE place_wise_content
ADD CONSTRAINT fk_place_wise_content_post
FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE;

-- Drop and recreate post_likes foreign key with CASCADE
ALTER TABLE post_likes DROP CONSTRAINT IF EXISTS fk_post_likes_post;
ALTER TABLE post_likes DROP CONSTRAINT IF EXISTS post_likes_post_id_fkey;
ALTER TABLE post_likes
ADD CONSTRAINT fk_post_likes_post
FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE;

-- Drop and recreate post_comments foreign key with CASCADE
ALTER TABLE post_comments DROP CONSTRAINT IF EXISTS fk_post_comments_post;
ALTER TABLE post_comments DROP CONSTRAINT IF EXISTS post_comments_post_id_fkey;
ALTER TABLE post_comments
ADD CONSTRAINT fk_post_comments_post
FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE;

-- Verify the constraints
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
