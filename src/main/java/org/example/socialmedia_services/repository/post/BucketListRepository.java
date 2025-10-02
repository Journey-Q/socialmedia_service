package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.BucketList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BucketListRepository extends JpaRepository<BucketList, Long> {

    /**
     * Find bucket list by user ID
     */
    Optional<BucketList> findByUserId(Long userId);

    /**
     * Check if bucket list exists for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete bucket list by user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Find bucket list with all data by user ID
     */
    @Query("SELECT bl FROM BucketList bl WHERE bl.userId = :userId")
    Optional<BucketList> findByUserIdWithAllData(@Param("userId") Long userId);
}