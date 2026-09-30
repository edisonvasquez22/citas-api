-- RF-07: un profesional tiene exactamente una especialidad primaria. Hasta ahora solo lo validaba el
-- dominio (Profesional.validarAsignaciones); esto garantiza "como máximo una" también en la BD.
-- MySQL no tiene índices únicos parciales: la columna generada vale 1 solo en la fila primaria y NULL
-- en las demás, y un índice UNIQUE admite varios NULL. ("Al menos una" sigue siendo regla de dominio.)
ALTER TABLE professional_specialties
    ADD COLUMN primary_flag TINYINT
        GENERATED ALWAYS AS (IF(is_primary, 1, NULL)) STORED,
    ADD CONSTRAINT uq_professional_primary_specialty UNIQUE (professional_id, primary_flag);
