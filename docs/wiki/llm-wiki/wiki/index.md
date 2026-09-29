---
tipo: indice
actualizado: 2026-09-29 (S4 UI cerrada)
---

# Índice — LLM Wiki `citas`

Catálogo de la wiki global. Léelo antes de cualquier operación QUERY.

| Página | Responde a |
|---|---|
| [[dominio]] | actores, reglas de negocio esenciales, estados de cita/reprogramación |
| [[arquitectura]] | arquitectura hexagonal del backend, capas, límites, decisión de stack |
| [[contratos]] | contrato REST citas-api ↔ citas-web (se completa con HU-024) |
| [[decisiones]] | decisiones tomadas por el usuario/estudiante, con fecha y motivo |
| [[riesgos]] | riesgos, incógnitas y contenido no confiable identificado |

## Fuentes en `raw/`

Ver `../raw/README.md` para la tabla de fuentes ingeridas y su fecha.

## Estado del proyecto (resumen vivo)

- **Sesión activa:** S3 y S4 cerrados del lado de código (backend + UI para ambos). Único pendiente real del MVP S2-S4: verificación contra MySQL real (Docker/WSL2 del estudiante sigue pendiente) — todo lo demás se verificó con Playwright contra un backend simulado.
- **Repos Git:** `citas-api` y `citas-web` son repos Git reales, con GitHub configurado (`https://github.com/edisonvasquez22/citas-api` y `.../citas-web`, **públicos**, ver [[decisiones]]). Ambos en `develop`, sincronizados con `origin/develop` al empezar esta sesión (2026-09-28) pero con **todo el trabajo de esta sesión sin comitear todavía** (a la espera de que el usuario lo pida explícitamente) — S3 UI ADMIN + S4 backend completo. `main` sigue rezagado hasta el próximo merge.
- **Backlog Scrum:** ver `citas-api/docs/wiki/scrum/README.md` (11 épicas, 24 HU).
- **HU aprobadas:** S2 — HU-006, HU-001, HU-002. S3 — HU-009, HU-010, HU-011, HU-012, HU-013, HU-014, HU-015, HU-016, HU-023, todas `En desarrollo` (backend + UI ADMIN completos). S4 — HU-017 a HU-022 aprobadas 2026-09-28, backend implementado 2026-09-29, UI integrada 2026-09-29 (mis citas/cancelar/reprogramar, agenda del profesional/cierre, bandeja de reprogramaciones ADMIN), todas `En desarrollo`. `HU-003`/`HU-004`/`HU-005`/`HU-007`/`HU-008` siguen en `Borrador`, sin decisión de aprobación todavía.
- **Backend:** vertical slice de autenticación (S2) + profesionales/especialidades (HU-009/010/011, incluido `GET /api/admin/professionals` agregado 2026-09-28) + disponibilidad con discretización en slots de 30 min (HU-012/013) + flujo completo de citas con retención atómica anti doble-reserva (HU-014/015/016) auditado (HU-023) + **mis citas/cancelación/reprogramación/agenda/cierre (HU-017 a HU-022, S4)**, con un agregado de dominio nuevo `SolicitudReprogramacion`. `mvn test`: **106/106, BUILD SUCCESS** (2026-09-29). Único pendiente real de siempre: nunca se corrió contra MySQL real (falta Docker/WSL2 del estudiante) — y por lo tanto tampoco existe manera de loguearse como ADMIN real (no hay forma de crear el primer ADMIN, ver [[decisiones]]).
- **Diseño de datos:** esquema exacto de `database/reference/db.sql` (ver [[decisiones]] 2026-09-23); `V1`/`V2` cubren todas las tablas usadas hasta S4, sin migraciones nuevas (incluye `reschedule_requests` para HU-019/020, ya estaba en el esquema).
- **Frontend:** `citas-web` tiene login, registro, agendar cita (general/especializada), disponibilidad del profesional (HU-012), bandeja de aprobación ADMIN (HU-016), gestión de catálogo ADMIN (HU-009/010/011), mis citas/cancelar/reprogramar (HU-017/018/019), agenda del profesional/cierre de atención (HU-021/022) y bandeja de reprogramaciones ADMIN (HU-020) reales, conectados a `citas-api`. Verificado con Playwright contra backend simulado (sin Docker) el 2026-09-28 y 2026-09-29. El diseño visual sigue siendo responsabilidad exclusiva del usuario, Stitch/AI Studio, `AGENTS.md` raíz regla 11 — todos los exports de S4 ya fueron traídos y reconciliados. Ver `citas-web/AGENTS.md`.
- **Toolchain instalado en la máquina del estudiante:** JDK 21, Maven 3.9.16, Git, Node.js 24 LTS, GitHub CLI. Docker Desktop instalado pero **no operativo todavía** (falta que el estudiante complete el reinicio + configuración de WSL2).
