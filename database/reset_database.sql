-- =====================================================================
-- RUN THIS ONCE (MySQL Workbench / phpMyAdmin / mysql CLI) BEFORE the
-- first start of the fixed backend.
--
-- Why: Employee ID changed from BIGINT to VARCHAR ("EMP004"), and MySQL
-- cannot convert an existing primary key + foreign keys automatically.
-- WARNING: this DELETES the old demo data. Spring Boot recreates all tables,
-- a default admin (admin / admin123) and default school settings.
-- =====================================================================

DROP DATABASE IF EXISTS biometric_atttendance_db;
CREATE DATABASE biometric_atttendance_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
