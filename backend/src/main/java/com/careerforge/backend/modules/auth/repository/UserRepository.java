package com.careerforge.backend.modules.auth.repository;

import com.careerforge.backend.modules.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(com.careerforge.backend.modules.auth.model.Role role);
}
