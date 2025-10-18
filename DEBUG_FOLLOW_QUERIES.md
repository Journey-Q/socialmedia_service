# Debug Follow Queries

## Check your database with these SQL queries:

### 1. View all follow relationships
```sql
SELECT * FROM follows;
```

### 2. View all accepted follows
```sql
SELECT * FROM follows WHERE status = 'accepted';
```

### 3. Get followers for a specific user (replace 'USER_ID' with actual userId)
```sql
-- This finds people who are following USER_ID
SELECT * FROM follows
WHERE follower_id = 'USER_ID'
AND status = 'accepted';
```

### 4. Get following for a specific user (replace 'USER_ID' with actual userId)
```sql
-- This finds people that USER_ID is following
SELECT * FROM follows
WHERE following_id = 'USER_ID'
AND status = 'accepted';
```

## Understanding the schema:
- **follower_id**: The user being followed (the person receiving the follow)
- **following_id**: The user who is following (the person initiating the follow)

## Example:
If User A follows User B:
- following_id = A
- follower_id = B
- status = 'accepted' (after acceptance)

So:
- B's followers query: `WHERE follower_id = 'B'` → Returns A
- A's following query: `WHERE following_id = 'A'` → Returns B
