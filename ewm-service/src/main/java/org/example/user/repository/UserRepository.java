package org.example.user.repository;

import org.example.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIdIn(List<Long> id);

    boolean existsUserByEmail(String email);
}
