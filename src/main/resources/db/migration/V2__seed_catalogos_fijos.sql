-- Seed de catálogos (RF-05 fijos + catálogo público de sedes/especialidades),
-- copiado de database/reference/db.sql secciones 5 y 6. Ver decisiones.md
-- (2026-09-23): se adoptó el esquema de referencia exacto.
--
-- Deliberadamente NO se copian los seeds sintéticos de operación de
-- db.sql (sección 7: profesionales, pacientes demo, disponibilidad, citas
-- de ejemplo, reprogramaciones): esas tablas todavía no tienen ningún
-- caso de uso/adaptador JPA implementado (EP-002 en adelante, sin aprobar
-- todavía en S3/S4) y no hay HU aprobada que los necesite hoy.

INSERT INTO roles (id, code, name, description) VALUES
(1, 'USER', 'Usuario', 'Paciente/usuario que solicita y gestiona sus citas'),
(2, 'PROFESSIONAL', 'Profesional', 'Profesional que administra su disponibilidad'),
(3, 'ADMIN', 'Administrador', 'Administra catálogos, profesionales y aprobaciones');

INSERT INTO insurance_regimes (id, code, name) VALUES
(1, 'CONTRIBUTIVO', 'Contributivo'),
(2, 'SUBSIDIADO', 'Subsidiado'),
(3, 'ESPECIAL', 'Especial'),
(4, 'EXCEPCION', 'Excepción'),
(5, 'PARTICULAR', 'Particular');

INSERT INTO appointment_statuses (id, code, name, is_terminal) VALUES
(1, 'REQUESTED', 'Solicitada / pendiente de aprobación', FALSE),
(2, 'APPROVED', 'Aprobada', FALSE),
(3, 'REJECTED', 'Rechazada', TRUE),
(4, 'CANCELLED', 'Cancelada', TRUE),
(5, 'COMPLETED', 'Atendida / completada', TRUE),
(6, 'NO_SHOW', 'No asistió', TRUE);

INSERT INTO reschedule_request_statuses (id, code, name, is_terminal) VALUES
(1, 'PENDING', 'Pendiente', FALSE),
(2, 'APPROVED', 'Aprobada', TRUE),
(3, 'REJECTED', 'Rechazada', TRUE),
(4, 'CANCELLED', 'Cancelada por el usuario', TRUE);

-- Sedes: información pública institucional (PRD sección 3).
INSERT INTO locations (id, code, name, address, city, department, active) VALUES
(
  1,
  'HIC',
  'Hospital Internacional de Colombia (HIC)',
  'Km 7 Autopista Bucaramanga - Piedecuesta, Valle de Menzulí',
  'Piedecuesta',
  'Santander',
  TRUE
),
(
  2,
  'ICV',
  'Fundación Cardiovascular de Colombia - Instituto Cardiovascular (ICV)',
  'Calle 155A No. 23-58, Urbanización El Bosque',
  'Floridablanca',
  'Santander',
  TRUE
);

-- Especialidades/servicios clínicos tomados de la oferta pública de FCV.
-- Las duraciones 30/60 min son supuestos del laboratorio.
INSERT INTO specialties
(id, code, name, appointment_duration_minutes, is_general, requires_admin_approval, active)
VALUES
(1, 'MEDICINA_GENERAL', 'Medicina General', 30, TRUE, FALSE, TRUE),
(2, 'CARDIOLOGIA_ADULTO', 'Cardiología Adulto', 30, FALSE, TRUE, TRUE),
(3, 'CARDIOLOGIA_PEDIATRICA', 'Cardiología Pediátrica', 30, FALSE, TRUE, TRUE),
(4, 'MEDICINA_INTERNA', 'Medicina Interna', 30, FALSE, TRUE, TRUE),
(5, 'PEDIATRIA', 'Pediatría', 30, FALSE, TRUE, TRUE),
(6, 'NEFROLOGIA', 'Nefrología', 30, FALSE, TRUE, TRUE),
(7, 'UROLOGIA', 'Urología', 30, FALSE, TRUE, TRUE),
(8, 'GASTROENTEROLOGIA', 'Gastroenterología', 30, FALSE, TRUE, TRUE),
(9, 'NEUMOLOGIA_ADULTO', 'Neumología Adulto', 30, FALSE, TRUE, TRUE),
(10, 'ENDOCRINOLOGIA', 'Endocrinología', 30, FALSE, TRUE, TRUE),
(11, 'ORTOPEDIA_TRAUMATOLOGIA', 'Ortopedia y Traumatología', 60, FALSE, TRUE, TRUE),
(12, 'NEUROLOGIA', 'Neurología', 60, FALSE, TRUE, TRUE);
