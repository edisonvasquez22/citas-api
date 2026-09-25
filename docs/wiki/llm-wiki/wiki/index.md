---
tipo: indice
actualizado: 2026-09-25
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

- **Sesión activa:** S3 — flujo de citas (profesionales, disponibilidad, reserva general/especializada, aprobación) y "red que dice no" (pruebas, hook local, bloqueo de secretos). Backend completo y probado; falta la mitad frontend (bloqueada por diseño visual reservado al usuario). S4 en adelante no ha empezado.
- **Repos Git:** `citas-api` y `citas-web` son repos Git reales, con GitHub configurado (`https://github.com/edisonvasquez22/citas-api` y `.../citas-web`, **públicos**, ver [[decisiones]]). `develop` tiene todo el trabajo de S3 (sin comitear todavía a la espera de que el usuario lo pida explícitamente); `main` sigue al nivel de S2 hasta el próximo merge.
- **Backlog Scrum:** ver `citas-api/docs/wiki/scrum/README.md` (11 épicas, 24 HU).
- **HU aprobadas:** S2 — HU-006, HU-001, HU-002. S3 — HU-009, HU-010, HU-011, HU-012, HU-013, HU-014, HU-015, HU-016, HU-023 (aprobación explícita del usuario 2026-09-25, ver [[decisiones]]), todas `En desarrollo`. HU-003 (recuperar contraseña) sigue en `Borrador`, planeada para S4.
- **Backend:** además del vertical slice de autenticación (S2), ahora incluye profesionales + especialidades (HU-009/010/011), disponibilidad con discretización en slots de 30 min (HU-012/013), y el flujo completo de citas con retención atómica anti doble-reserva (HU-014/015/016) auditado (HU-023). `mvn test`: **76/76, BUILD SUCCESS**, incluida una prueba de concurrencia real (10 hilos) demostrada explícitamente en Red→Green. Único pendiente real de siempre: nunca se corrió contra MySQL real (falta Docker/WSL2 del estudiante).
- **Diseño de datos:** esquema exacto de `database/reference/db.sql` (ver [[decisiones]] 2026-09-23); `V1`/`V2` ya cubren todas las tablas de S3, sin migraciones nuevas.
- **Frontend:** `citas-web` tiene login, registro y agendar cita (general/especializada) reales, conectados a `citas-api`. Profesionales/aprobación admin/disponibilidad del profesional siguen sin pantalla (el diseño visual sigue siendo responsabilidad exclusiva del usuario, Stitch/AI Studio, `AGENTS.md` raíz regla 11). Ver `citas-web/AGENTS.md`.
- **Toolchain instalado en la máquina del estudiante:** JDK 21, Maven 3.9.16, Git, Node.js 24 LTS, GitHub CLI. Docker Desktop instalado pero **no operativo todavía** (falta que el estudiante complete el reinicio + configuración de WSL2).
