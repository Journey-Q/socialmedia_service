package org.example.socialmedia_services.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.example.socialmedia_services.entity.User;


@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
