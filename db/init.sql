CREATE DATABASE IF NOT EXISTS sd3;
USE sd3;

CREATE TABLE IF NOT EXISTS `users` (
  `id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `login` VARCHAR(20) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE
);
-- ============================================
-- CIS Phase 2: Topics Table (Florencia)
-- ============================================
CREATE TABLE IF NOT EXISTS topics (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE INDEX idx_topics_active ON topics(active);
