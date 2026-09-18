-- Esquema inicial 3FN — diseño propio, ver citas-api/docs/db-design/MODELO_3FN.md
-- para el diagrama ER, las dependencias funcionales y la justificación de cada decisión.

-- =========================================================================
-- 1. Catalogos (RF-05 fijos / RF-06 configurables)
-- =========================================================================

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_roles_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    CONSTRAINT uk_sites_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE regimes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_regimes_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE appointment_statuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_appointment_statuses_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE reschedule_statuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_reschedule_statuses_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE slot_statuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_slot_statuses_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE eps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uk_eps_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE eps_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eps_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_eps_plans_eps FOREIGN KEY (eps_id) REFERENCES eps (id),
    CONSTRAINT uk_eps_plans_eps_name UNIQUE (eps_id, name)
) ENGINE=InnoDB;

CREATE TABLE specialties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    duration_minutes SMALLINT NOT NULL,
    is_general TINYINT(1) NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uk_specialties_name UNIQUE (name),
    CONSTRAINT chk_specialties_duration CHECK (duration_minutes IN (30, 60))
) ENGINE=InnoDB;

-- =========================================================================
-- 2. Nucleo de usuarios (RF-01, RF-02, RF-03)
-- =========================================================================

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    document_number VARCHAR(30) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_document UNIQUE (document_number)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    KEY idx_refresh_tokens_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE password_reset_tokens (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================================================================
-- 3. Profesionales, especialidades y sedes (RF-07, RF-08, RF-09)
-- =========================================================================

CREATE TABLE professionals (
    user_id CHAR(36) PRIMARY KEY,
    professional_code VARCHAR(30) NOT NULL,
    license_number VARCHAR(30) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_professionals_code UNIQUE (professional_code),
    CONSTRAINT uk_professionals_license UNIQUE (license_number)
) ENGINE=InnoDB;

CREATE TABLE professional_specialties (
    professional_id CHAR(36) NOT NULL,
    specialty_id BIGINT NOT NULL,
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    primary_flag CHAR(36) AS (CASE WHEN is_primary = 1 THEN professional_id ELSE NULL END) STORED,
    PRIMARY KEY (professional_id, specialty_id),
    CONSTRAINT fk_prof_spec_professional FOREIGN KEY (professional_id) REFERENCES professionals (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_prof_spec_specialty FOREIGN KEY (specialty_id) REFERENCES specialties (id),
    CONSTRAINT uk_prof_spec_one_primary UNIQUE (primary_flag)
) ENGINE=InnoDB;

CREATE TABLE professional_sites (
    professional_id CHAR(36) NOT NULL,
    site_id BIGINT NOT NULL,
    PRIMARY KEY (professional_id, site_id),
    CONSTRAINT fk_prof_sites_professional FOREIGN KEY (professional_id) REFERENCES professionals (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_prof_sites_site FOREIGN KEY (site_id) REFERENCES sites (id)
) ENGINE=InnoDB;

-- =========================================================================
-- 4. Afiliacion (RF-04)
-- =========================================================================

CREATE TABLE user_affiliations (
    user_id CHAR(36) PRIMARY KEY,
    eps_plan_id BIGINT NOT NULL,
    regime_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_affiliation_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_affiliation_eps_plan FOREIGN KEY (eps_plan_id) REFERENCES eps_plans (id),
    CONSTRAINT fk_affiliation_regime FOREIGN KEY (regime_id) REFERENCES regimes (id)
) ENGINE=InnoDB;

-- =========================================================================
-- 5. Disponibilidad y slots (RF-08, RF-09, RF-10, RN-01, RN-05)
-- =========================================================================

CREATE TABLE availability_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id CHAR(36) NOT NULL,
    site_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avail_block_professional FOREIGN KEY (professional_id) REFERENCES professionals (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_avail_block_site FOREIGN KEY (site_id) REFERENCES sites (id),
    CONSTRAINT chk_avail_block_range CHECK (end_at > start_at)
) ENGINE=InnoDB;

-- =========================================================================
-- 6. Citas y reprogramaciones (RF-11 a RF-15, RF-18)
-- =========================================================================

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    professional_id CHAR(36) NOT NULL,
    specialty_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    rejection_reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals (user_id),
    CONSTRAINT fk_appointments_specialty FOREIGN KEY (specialty_id) REFERENCES specialties (id),
    CONSTRAINT fk_appointments_site FOREIGN KEY (site_id) REFERENCES sites (id),
    CONSTRAINT fk_appointments_status FOREIGN KEY (status_id) REFERENCES appointment_statuses (id),
    CONSTRAINT chk_appointments_range CHECK (end_at > start_at),
    KEY idx_appointments_user_status (user_id, status_id),
    KEY idx_appointments_professional_status (professional_id, status_id, start_at)
) ENGINE=InnoDB;

CREATE TABLE reschedule_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    new_start_at DATETIME NOT NULL,
    new_end_at DATETIME NOT NULL,
    status_id BIGINT NOT NULL,
    rejection_reason VARCHAR(255) NULL,
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at DATETIME NULL,
    decided_by CHAR(36) NULL,
    CONSTRAINT fk_reschedule_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_reschedule_status FOREIGN KEY (status_id) REFERENCES reschedule_statuses (id),
    CONSTRAINT fk_reschedule_decided_by FOREIGN KEY (decided_by) REFERENCES users (id),
    CONSTRAINT chk_reschedule_range CHECK (new_end_at > new_start_at)
) ENGINE=InnoDB;

-- availability_slots va despues de appointments/reschedule_requests porque los referencia.
CREATE TABLE availability_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    block_id BIGINT NOT NULL,
    professional_id CHAR(36) NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    status_id BIGINT NOT NULL,
    appointment_id BIGINT NULL,
    reschedule_request_id BIGINT NULL,
    CONSTRAINT fk_slots_block FOREIGN KEY (block_id) REFERENCES availability_blocks (id) ON DELETE CASCADE,
    CONSTRAINT fk_slots_professional FOREIGN KEY (professional_id) REFERENCES professionals (user_id),
    CONSTRAINT fk_slots_status FOREIGN KEY (status_id) REFERENCES slot_statuses (id),
    CONSTRAINT fk_slots_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_slots_reschedule FOREIGN KEY (reschedule_request_id) REFERENCES reschedule_requests (id),
    CONSTRAINT uk_slots_professional_start UNIQUE (professional_id, start_at),
    CONSTRAINT chk_slots_range CHECK (end_at > start_at),
    CONSTRAINT chk_slots_single_holder CHECK (
        (appointment_id IS NULL AND reschedule_request_id IS NULL)
        OR (appointment_id IS NOT NULL AND reschedule_request_id IS NULL)
        OR (appointment_id IS NULL AND reschedule_request_id IS NOT NULL)
    )
) ENGINE=InnoDB;

-- =========================================================================
-- 7. Auditoria de estados (RF-19, RN-11, RN-12)
-- =========================================================================

CREATE TABLE appointment_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    actor_user_id CHAR(36) NULL,
    source ENUM('SYSTEM', 'USER', 'ADMIN') NOT NULL,
    reason VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_status_history_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_status_history_status FOREIGN KEY (status_id) REFERENCES appointment_statuses (id),
    CONSTRAINT fk_status_history_actor FOREIGN KEY (actor_user_id) REFERENCES users (id),
    KEY idx_status_history_appointment (appointment_id)
) ENGINE=InnoDB;
