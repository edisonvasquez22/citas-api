-- Esquema inicial — copia estructural exacta de database/reference/db.sql
-- (decisión explícita del usuario, 2026-09-23; ver citas-api/docs/db-design/MODELO_3FN.md
-- y COMPARACION_REFERENCIA.md para el detalle de la decisión y lo que cambió).
-- No incluye CREATE DATABASE/USE/SET NAMES/SET time_zone: ese esquema/charset
-- ya lo establece la conexión (spring.datasource.url + docker-compose.yml).

-- ============================================================
-- 1. SEGURIDAD Y USUARIOS
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(40) NOT NULL,
    email VARCHAR(160) NOT NULL,
    phone VARCHAR(30) NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_document UNIQUE (document_type, document_number),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    device_info VARCHAR(255) NULL,
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    INDEX ix_refresh_tokens_user (user_id),
    INDEX ix_refresh_tokens_expiry (expires_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_password_reset_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    INDEX ix_password_reset_user (user_id),
    INDEX ix_password_reset_expiry (expires_at)
) ENGINE=InnoDB;

-- ============================================================
-- 2. ASEGURAMIENTO / EPS
-- ============================================================

CREATE TABLE IF NOT EXISTS insurance_regimes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS eps (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS eps_plans (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    eps_id BIGINT UNSIGNED NOT NULL,
    regime_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_eps_plan_code UNIQUE (eps_id, code),
    CONSTRAINT fk_eps_plans_eps
        FOREIGN KEY (eps_id) REFERENCES eps(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_eps_plans_regime
        FOREIGN KEY (regime_id) REFERENCES insurance_regimes(id)
        ON DELETE RESTRICT,
    INDEX ix_eps_plans_regime (regime_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_insurance_affiliations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    plan_id BIGINT UNSIGNED NOT NULL,
    membership_number VARCHAR(80) NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from DATE NULL,
    valid_to DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_membership UNIQUE (user_id, plan_id, membership_number),
    CONSTRAINT fk_user_insurance_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_insurance_plan
        FOREIGN KEY (plan_id) REFERENCES eps_plans(id)
        ON DELETE RESTRICT,
    INDEX ix_user_insurance_current (user_id, is_current)
) ENGINE=InnoDB;

-- ============================================================
-- 3. CATÁLOGOS DE SERVICIO
-- ============================================================

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL UNIQUE,
    appointment_duration_minutes INT UNSIGNED NOT NULL,
    is_general BOOLEAN NOT NULL DEFAULT FALSE,
    requires_admin_approval BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_specialty_duration
        CHECK (appointment_duration_minutes IN (30, 60))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professionals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL UNIQUE,
    professional_code VARCHAR(40) NOT NULL UNIQUE,
    license_number VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_professionals_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_specialties (
    professional_id BIGINT UNSIGNED NOT NULL,
    specialty_id BIGINT UNSIGNED NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (professional_id, specialty_id),
    CONSTRAINT fk_prof_specialty_professional
        FOREIGN KEY (professional_id) REFERENCES professionals(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_prof_specialty_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialties(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professional_locations (
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id BIGINT UNSIGNED NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (professional_id, location_id),
    CONSTRAINT fk_prof_location_professional
        FOREIGN KEY (professional_id) REFERENCES professionals(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_prof_location_location
        FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ============================================================
-- 4. CITAS Y ESTADOS
-- ============================================================

CREATE TABLE IF NOT EXISTS appointment_statuses (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT UNSIGNED NOT NULL,
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id BIGINT UNSIGNED NOT NULL,
    specialty_id BIGINT UNSIGNED NOT NULL,
    insurance_affiliation_id BIGINT UNSIGNED NULL,
    status_id BIGINT UNSIGNED NOT NULL,
    reason VARCHAR(500) NULL,
    scheduled_start_at DATETIME NOT NULL,
    scheduled_end_at DATETIME NOT NULL,
    created_by_user_id BIGINT UNSIGNED NOT NULL,
    approved_by_user_id BIGINT UNSIGNED NULL,
    approved_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_appointment_time
        CHECK (scheduled_end_at > scheduled_start_at),
    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_professional
        FOREIGN KEY (professional_id) REFERENCES professionals(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_location
        FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialties(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_insurance
        FOREIGN KEY (insurance_affiliation_id) REFERENCES user_insurance_affiliations(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_appointments_status
        FOREIGN KEY (status_id) REFERENCES appointment_statuses(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_approved_by
        FOREIGN KEY (approved_by_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    INDEX ix_appointments_patient (patient_user_id, scheduled_start_at),
    INDEX ix_appointments_professional (professional_id, scheduled_start_at),
    INDEX ix_appointments_status (status_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS availability_blocks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id BIGINT UNSIGNED NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_availability_block_time
        CHECK (end_time > start_time),
    CONSTRAINT fk_availability_professional
        FOREIGN KEY (professional_id) REFERENCES professionals(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_availability_location
        FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE RESTRICT,
    INDEX ix_availability_prof_date
        (professional_id, available_date, start_time),
    INDEX ix_availability_location_date
        (location_id, available_date)
) ENGINE=InnoDB;

-- Slots atómicos de 30 minutos.
-- Una cita de 30 min reserva 1 slot; una de 60 min reserva 2 consecutivos.
CREATE TABLE IF NOT EXISTS professional_slots (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    availability_block_id BIGINT UNSIGNED NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    appointment_id BIGINT UNSIGNED NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_professional_slot_time
        CHECK (end_at > start_at),
    CONSTRAINT uq_block_slot UNIQUE (availability_block_id, start_at),
    CONSTRAINT fk_slots_availability_block
        FOREIGN KEY (availability_block_id) REFERENCES availability_blocks(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_slots_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id)
        ON DELETE SET NULL,
    INDEX ix_slots_start (start_at),
    INDEX ix_slots_appointment (appointment_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointment_status_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    status_id BIGINT UNSIGNED NOT NULL,
    changed_by_user_id BIGINT UNSIGNED NULL,
    change_source VARCHAR(20) NOT NULL DEFAULT 'USER',
    reason VARCHAR(500) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_status_history_source
        CHECK (change_source IN ('SYSTEM', 'USER', 'ADMIN')),
    CONSTRAINT fk_status_history_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_status_history_status
        FOREIGN KEY (status_id) REFERENCES appointment_statuses(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_status_history_user
        FOREIGN KEY (changed_by_user_id) REFERENCES users(id)
        ON DELETE SET NULL,
    INDEX ix_status_history_appointment (appointment_id, changed_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reschedule_request_statuses (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reschedule_requests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    requested_by_user_id BIGINT UNSIGNED NOT NULL,
    requested_location_id BIGINT UNSIGNED NOT NULL,
    status_id BIGINT UNSIGNED NOT NULL,
    previous_start_at DATETIME NOT NULL,
    previous_end_at DATETIME NOT NULL,
    requested_start_at DATETIME NOT NULL,
    requested_end_at DATETIME NOT NULL,
    decision_reason VARCHAR(500) NULL,
    decided_by_user_id BIGINT UNSIGNED NULL,
    decided_at DATETIME NULL,
    patient_action_after_rejection VARCHAR(30) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_reschedule_time
        CHECK (requested_end_at > requested_start_at),
    CONSTRAINT ck_reschedule_patient_action
        CHECK (
            patient_action_after_rejection IS NULL
            OR patient_action_after_rejection IN ('KEEP_APPOINTMENT', 'CANCEL_APPOINTMENT')
        ),
    CONSTRAINT fk_reschedule_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reschedule_requested_by
        FOREIGN KEY (requested_by_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_location
        FOREIGN KEY (requested_location_id) REFERENCES locations(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_status
        FOREIGN KEY (status_id) REFERENCES reschedule_request_statuses(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_decided_by
        FOREIGN KEY (decided_by_user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    INDEX ix_reschedule_appointment (appointment_id),
    INDEX ix_reschedule_status (status_id)
) ENGINE=InnoDB;
