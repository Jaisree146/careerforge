-- ===================================================================
-- CareerForge Database Migration: V5 Skills and Jobs Tables
-- Engine: InnoDB
-- Character Set: utf8mb4
-- ===================================================================

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_skills_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS candidate_skills (
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, skill_id),
    CONSTRAINT fk_candidate_skills_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_candidate_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    experience VARCHAR(100),
    salary VARCHAR(100),
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED') NOT NULL DEFAULT 'DRAFT',
    employment_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'REMOTE') NOT NULL DEFAULT 'FULL_TIME',
    deadline DATE,
    company_id BIGINT NOT NULL,
    recruiter_id BIGINT NOT NULL,
    location_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    CONSTRAINT fk_jobs_recruiter FOREIGN KEY (recruiter_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_jobs_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE SET NULL,
    INDEX idx_jobs_status (status),
    INDEX idx_jobs_company (company_id),
    INDEX idx_jobs_recruiter (recruiter_id),
    INDEX idx_jobs_location (location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_skills (
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (job_id, skill_id),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
