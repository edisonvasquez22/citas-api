---
tipo: indice
actualizado: 2026-09-29 (S4 completo, comiteado, LOOP_01/LOOP_02 ejecutados; S5/S6 sin empezar)
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

- **Sesión activa:** S3 y S4 cerrados del lado de código (backend + UI para ambos) y ya comiteados en `develop`. Además se ejecutaron formalmente `LOOP_01_GUIADO_SIMPLE.md` (PASS) y `LOOP_02_GUIADO_AVANZADO.md` (PASS en iteración 2/4, corrigió un hallazgo real). **Importante: según `GUIA_SESIONES_S2_S6.md`, el proyecto NO está terminado** — falta `LOOP_03_RETO_INDEPENDIENTE.md` (debe diseñarlo el propio estudiante, no un agente) y S5/S6 completos (n8n/MCP, workflows WF-001/WF-002, sustentación técnica). Único pendiente técnico real de S2-S4: verificación contra MySQL real — Docker Desktop seguía bloqueado por una versión vieja de WSL2, se corrigió con `wsl --update` (ahora WSL 3.0.1.0) en esta sesión, pendiente que el estudiante reintente Docker Desktop.
- **Repos Git:** `citas-api` y `citas-web` son repos Git reales, con GitHub configurado (`https://github.com/edisonvasquez22/citas-api` y `.../citas-web`, **públicos**, ver [[decisiones]]). Ambos en `develop`, con el trabajo de S3 UI ADMIN + S4 completo (backend + UI + LOOP_01/LOOP_02) comiteado localmente (commits `2d9ef9b` en citas-api, `5949800` en citas-web, más el fix de LOOP_02 sin comitear todavía) pero **sin push a GitHub** (no se ha pedido). `main` sigue rezagado hasta el próximo merge `develop → main` (decisión del estudiante).
- **Backlog Scrum:** ver `citas-api/docs/wiki/scrum/README.md` (11 épicas, 24 HU).
- **HU aprobadas:** S2 — HU-006, HU-001, HU-002. S3 — HU-009, HU-010, HU-011, HU-012, HU-013, HU-014, HU-015, HU-016, HU-023, todas `En desarrollo` (backend + UI ADMIN completos). S4 — HU-017 a HU-022 aprobadas 2026-09-28, backend implementado 2026-09-29, UI integrada 2026-09-29 (mis citas/cancelar/reprogramar, agenda del profesional/cierre, bandeja de reprogramaciones ADMIN), todas `En desarrollo`. `HU-003`/`HU-004`/`HU-005`/`HU-007`/`HU-008` siguen en `Borrador`, sin decisión de aprobación todavía.
- **Backend:** vertical slice de autenticación (S2) + profesionales/especialidades (HU-009/010/011, incluido `GET /api/admin/professionals` agregado 2026-09-28) + disponibilidad con discretización en slots de 30 min (HU-012/013) + flujo completo de citas con retención atómica anti doble-reserva (HU-014/015/016) auditado (HU-023) + **mis citas/cancelación/reprogramación/agenda/cierre (HU-017 a HU-022, S4)**, con un agregado de dominio nuevo `SolicitudReprogramacion`. `mvn test`: **109/109, BUILD SUCCESS** (2026-09-29, tras LOOP_02). Único pendiente real de siempre: nunca se corrió contra MySQL real — y por lo tanto tampoco existe manera de loguearse como ADMIN real (no hay forma de crear el primer ADMIN, ver [[decisiones]]); Docker debería quedar operativo pronto (WSL2 ya actualizado esta sesión).
- **Diseño de datos:** esquema exacto de `database/reference/db.sql` (ver [[decisiones]] 2026-09-23); `V1`/`V2` cubren todas las tablas usadas hasta S4, sin migraciones nuevas (incluye `reschedule_requests` para HU-019/020, ya estaba en el esquema).
- **Frontend:** `citas-web` tiene login, registro, agendar cita (general/especializada), disponibilidad del profesional (HU-012), bandeja de aprobación ADMIN (HU-016), gestión de catálogo ADMIN (HU-009/010/011), mis citas/cancelar/reprogramar (HU-017/018/019), agenda del profesional/cierre de atención (HU-021/022) y bandeja de reprogramaciones ADMIN (HU-020) reales, conectados a `citas-api`. Verificado con Playwright contra backend simulado (sin Docker) el 2026-09-28 y 2026-09-29. El diseño visual sigue siendo responsabilidad exclusiva del usuario, Stitch/AI Studio, `AGENTS.md` raíz regla 11 — todos los exports de S4 ya fueron traídos y reconciliados. Ver `citas-web/AGENTS.md`.
- **Toolchain instalado en la máquina del estudiante:** JDK 21, Maven 3.9.16, Git, Node.js 24 LTS, GitHub CLI. Docker Desktop instalado; bloqueado por WSL2 desactualizado (error "WSL needs updating") — corregido en esta sesión con `wsl --update` (ahora WSL 3.0.1.0, kernel 6.18.40.1-1); pendiente que el estudiante reintente Docker Desktop y confirme que arranca.
- **S5/S6 (n8n/MCP):** sin empezar. Solo existen los `.md` de especificación de los workflows (`citas-api/automations/n8n/WF-001/002/003-*.md`), no los JSON reales que exige el entregable final. Requiere que el estudiante tenga n8n accesible y credenciales OAuth propias (precondición del profesor/trainer, ver `GUIA_SESIONES_S2_S6.md` S5).
