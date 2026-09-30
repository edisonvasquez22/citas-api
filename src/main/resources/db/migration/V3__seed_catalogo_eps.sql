-- Seed del catálogo de EPS/planes (HU-007/HU-008), copiado de
-- database/reference/db.sql sección 2. Datos sintéticos de demostración, no
-- reales (ver AGENTS.md raíz, regla 6). No se necesitó una nueva migración
-- de esquema: `eps`/`eps_plans` ya existen en V1__esquema_inicial.sql desde
-- la adopción del esquema de referencia (2026-09-23); solo faltaba el seed,
-- omitido en V2 porque hasta ahora ningún caso de uso los usaba (ver
-- comentario en V2__seed_catalogos_fijos.sql).

INSERT INTO eps (id, code, name, active) VALUES
(1, 'EPS_DEMO_A', 'EPS Demo Salud', TRUE),
(2, 'EPS_DEMO_B', 'EPS Demo Familiar', TRUE);

INSERT INTO eps_plans (id, eps_id, regime_id, code, name, active) VALUES
(1, 1, 1, 'A-CONTRIB', 'Plan Contributivo Demo', TRUE),
(2, 1, 2, 'A-SUBS', 'Plan Subsidiado Demo', TRUE),
(3, 2, 1, 'B-CONTRIB', 'Plan Contributivo Familiar Demo', TRUE),
(4, 2, 3, 'B-ESPECIAL', 'Plan Especial Demo', TRUE);
