package com.careerforge.backend.modules.profile.repository;

import com.careerforge.backend.modules.profile.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
