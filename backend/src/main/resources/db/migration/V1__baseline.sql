-- ===================================================================
-- CareerForge Database Migration: V1 Baseline Verification
-- Engine: InnoDB
-- Character Set: utf8mb4 (supports full Unicode & emojis)
-- ===================================================================

CREATE TABLE IF NOT EXISTS platform_health (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO platform_health (component_name, status)
VALUES ('DATABASE', 'UP')
ON DUPLICATE KEY UPDATE status = 'UP', checked_at = CURRENT_TIMESTAMP;
