# CareerForge — Database Design

**Status:** Design v1 — Frozen  
**Database:** MySQL 8 (InnoDB, utf8mb4)

## 1. Overview

CareerForge uses a relational database to manage users, candidate profiles, recruiters, companies, jobs, skills, applications, resumes, and notifications. The schema follows normalization principles to reduce data duplication and maintain data integrity.

## 2. Entities and Attributes

### User
- `id` (PK)
- `name`
- `email` (Unique)
- `password_hash`
- `role` (CANDIDATE, RECRUITER, ADMIN)

### CandidateProfile
- `user_id` (PK, FK → User.id)
- `bio`

### RecruiterProfile
- `user_id` (PK, FK → User.id)
- `company_id` (FK → Company.id)

### Company
- `id` (PK)
- `name`
- `description`
- `location_id` (FK → Location.id)
- `website_url`

### Location
- `id` (PK)
- `name`

### Skill
- `id` (PK)
- `name` (Unique)

### CandidateSkill
- `user_id` (PK, FK → User.id)
- `skill_id` (PK, FK → Skill.id)

### Job
- `id` (PK)
- `title`
- `description`
- `experience`
- `salary`
- `status`
- `employment_type`
- `deadline`
- `company_id` (FK → Company.id)
- `recruiter_id` (FK → RecruiterProfile.user_id)
- `location_id` (FK → Location.id)

### JobSkill
- `job_id` (PK, FK → Job.id)
- `skill_id` (PK, FK → Skill.id)
- `is_required`

### Application
- `id` (PK)
- `user_id` (FK → User.id)
- `job_id` (FK → Job.id)
- `resume_id` (FK → Resume.id)
- `status`
- `applied_at`

**Constraint:** A candidate can apply to a particular job only once. Enforce uniqueness on `(user_id, job_id)`.

### Resume
- `id` (PK)
- `user_id` (FK → User.id, Unique)
- `file_url`
- `uploaded_at`

**Constraint:** Each candidate can have one resume in v1.

### Education
- `id` (PK)
- `user_id` (FK → User.id)
- `degree`
- `institution`
- `start_date`
- `end_date`

### Experience
- `id` (PK)
- `user_id` (FK → User.id)
- `company_name`
- `role`
- `start_date`
- `end_date`

### SavedJob
- `user_id` (PK, FK → User.id)
- `job_id` (PK, FK → Job.id)

### Notification
- `id` (PK)
- `user_id` (FK → User.id)
- `message`
- `seen`

## 3. Relationships

- User → CandidateProfile: one-to-zero-or-one
- User → RecruiterProfile: one-to-zero-or-one
- Company → RecruiterProfile: one-to-many
- Company → Job: one-to-many
- RecruiterProfile → Job: one-to-many
- Location → Company: one-to-many
- Location → Job: one-to-many
- User ↔ Skill: many-to-many through CandidateSkill
- Job ↔ Skill: many-to-many through JobSkill
- User → Education: one-to-many
- User → Experience: one-to-many
- User → Resume: one-to-one
- User ↔ Job: many-to-many through Application
- User ↔ Job: many-to-many through SavedJob
- User → Notification: one-to-many

## 4. Design Decisions

1. A common `User` entity stores authentication and role information.
2. Candidate and recruiter profiles store role-specific information.
3. `CandidateSkill` and `JobSkill` resolve many-to-many relationships.
4. `Location` is separate to support consistent location-based job searches.
5. `Application` stores application status and submission time.
6. A candidate has one resume in v1; file metadata is stored in the database.
7. `SavedJob` allows candidates to bookmark jobs.
8. Passwords are stored as secure hashes, never as plaintext.

## 5. Integrity Constraints

- User email must be unique.
- Skill name must be unique.
- A candidate cannot apply to the same job twice.
- A candidate cannot save the same job twice.
- Foreign keys must reference existing records.
- Job status and application status must use defined allowed values.
- Only authorized recruiters can manage their jobs and review their applications.

## 6. Next Steps

1. Validate the ER diagram.
2. Review normalization and constraints.
3. Finalize data types and allowed values.
4. Create MySQL tables and indexes with InnoDB and utf8mb4.
5. Add version-controlled database migrations.

