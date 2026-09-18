-- Seed de catalogos fijos (RF-05, HU-006). Ver database/REQUISITOS_NORMALIZACION_3FN.md
-- y citas-api/docs/db-design/MODELO_3FN.md. Estos catalogos son de solo lectura por API.

INSERT INTO roles (code, name) VALUES
    ('USER', 'Usuario'),
    ('PROFESSIONAL', 'Profesional'),
    ('ADMIN', 'Administrador');

-- Direcciones publicas reales (PRD seccion 3); el resto del dominio es sintetico.
INSERT INTO sites (code, name, address) VALUES
    ('HIC', 'Hospital Internacional de Colombia',
        'Km 7 Autopista Bucaramanga-Piedecuesta, Valle de Menzuli, Santander'),
    ('ICV', 'Fundacion Cardiovascular de Colombia / Instituto Cardiovascular',
        'Calle 155A No. 23-58, Urbanizacion El Bosque, Floridablanca, Santander');

INSERT INTO regimes (code, name) VALUES
    ('CONTRIBUTIVO', 'Regimen contributivo'),
    ('SUBSIDIADO', 'Regimen subsidiado');

INSERT INTO appointment_statuses (code, name) VALUES
    ('REQUESTED', 'Solicitada'),
    ('APPROVED', 'Aprobada'),
    ('REJECTED', 'Rechazada'),
    ('CANCELLED', 'Cancelada'),
    ('COMPLETED', 'Completada'),
    ('NO_SHOW', 'No asistio');

INSERT INTO reschedule_statuses (code, name) VALUES
    ('PENDING', 'Pendiente'),
    ('APPROVED', 'Aprobada'),
    ('REJECTED', 'Rechazada');

INSERT INTO slot_statuses (code, name) VALUES
    ('FREE', 'Libre'),
    ('HELD', 'Retenido'),
    ('BOOKED', 'Reservado');
