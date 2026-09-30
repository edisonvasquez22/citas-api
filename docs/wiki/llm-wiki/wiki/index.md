---
tipo: indice
actualizado: 2026-09-30 (HU-003/004/005/007/008 implementadas en backend, 133/133; S5/S6 sin empezar)
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

- **Sesión activa (2026-09-30):** LOOP_03 comiteado y fusionado a `main` en `citas-api` (`4f88e9c`, pusheado a GitHub). El usuario preguntó qué faltaba además de n8n; se detectó que `GUIA_SESIONES_S2_S6.md` pedía explícitamente "recuperación de contraseña" y "EPS/planes/especialidades CRUD" dentro de S4, pero `HU-003/004/005/007/008` habían quedado en `Borrador` fuera de la aprobación de S4 (brecha real entre la guía y lo aprobado). El usuario pidió implementarlas todas: backend completo de las 5 HU terminado y probado (`mvn test` 133/133), **sin comitear todavía**. Falta generar los prompts de AI Studio para la UI (pendiente, diseño visual reservado al usuario) y decidir cuándo comitear+mergear este trabajo. Docker: seguía en "WSL needs updating" pese al `wsl --update` de la sesión anterior — causa real encontrada en los logs de Docker Desktop (`com.docker.backend.exe.log`): un timeout transitorio de `wsl.exe` justo durante la actualización, que dejó el motor marcado como "necesita actualización" sin volver a revisarse. Un `Restart` limpio lo superó, pero luego se atascó creando la VM (`ERROR_TIMEOUT`) — causa probable: VPN corporativa GlobalProtect activa, interfiere con la red virtual de WSL2. Usuario desconectó la VPN; quedó pendiente un reinicio limpio de Docker Desktop (se mataron procesos colgados manualmente) para confirmar si ya arranca. **Importante: según `GUIA_SESIONES_S2_S6.md`, el proyecto sigue sin estar terminado** — falta S5/S6 completos (n8n/MCP, workflows WF-001/WF-002, sustentación técnica).
- **Repos Git:** `citas-api` y `citas-web` son repos Git reales, con GitHub configurado (`https://github.com/edisonvasquez22/citas-api` y `.../citas-web`, **públicos**, ver [[decisiones]]). `main` y `develop` sincronizados en `citas-api` hasta `4f88e9c` (LOOP_03); `citas-web` sincronizado hasta `dd8fcec`. El backend de HU-003/004/005/007/008 (2026-09-30) es lo único que queda sin comitear ahora mismo.
- **Backlog Scrum:** ver `citas-api/docs/wiki/scrum/README.md` (11 épicas, 24 HU).
- **HU aprobadas:** S2 — HU-006, HU-001, HU-002. S3 — HU-009 a HU-016, HU-023, todas `En desarrollo` (backend + UI ADMIN completos). S4 — HU-017 a HU-022, backend+UI completos, `En desarrollo`. **HU-003/004/005/007/008 — backend completo 2026-09-30, `En desarrollo`** (UI pendiente).
- **Backend:** vertical slice de autenticación (S2) + profesionales/especialidades (HU-009/010/011) + disponibilidad con slots de 30 min (HU-012/013) + flujo completo de citas con retención atómica anti doble-reserva (HU-014/015/016) auditado (HU-023) + mis citas/cancelación/reprogramación/agenda/cierre (HU-017 a HU-022) + **recuperar contraseña, perfil, afiliación EPS/plan y catálogo EPS/planes (HU-003/004/005/007/008, 2026-09-30)**. `mvn test`: **133/133, BUILD SUCCESS** (2026-09-30, +22 sobre las 111 previas). Único pendiente real de siempre: nunca se corrió contra MySQL real — y por lo tanto tampoco existe manera de loguearse como ADMIN real (no hay forma de crear el primer ADMIN, ver [[decisiones]]); Docker sigue sin confirmar que arranca (ver arriba).
- **Diseño de datos:** esquema exacto de `database/reference/db.sql` (ver [[decisiones]] 2026-09-23); `V1`/`V2` cubren todas las tablas usadas hasta HU-022, sin migraciones nuevas. `V3__seed_catalogo_eps.sql` (2026-09-30) agrega el único seed que faltaba: datos demo de `eps`/`eps_plans` (las tablas ya existían en V1, nunca se habían sembrado porque ningún caso de uso las usaba hasta ahora).
- **Frontend:** `citas-web` tiene login, registro, agendar cita, disponibilidad del profesional, bandeja de aprobación ADMIN, gestión de catálogo ADMIN (especialidades/profesionales), mis citas/cancelar/reprogramar, agenda del profesional/cierre, bandeja de reprogramaciones ADMIN — todo real, conectado a `citas-api`. **Pendiente**: UI de HU-003/004/005/007/008 (recuperar contraseña, perfil, afiliación, catálogo EPS/planes) — backend listo, diseño visual reservado al usuario (Stitch/AI Studio), `AGENTS.md` raíz regla 11. Ver `citas-web/AGENTS.md`.
- **Toolchain instalado en la máquina del estudiante:** JDK 21, Maven 3.9.16, Git, Node.js 24 LTS, GitHub CLI, Docker Desktop 4.91.0. WSL2 actualizado (3.0.1.0) pero Docker Desktop sigue sin confirmarse operativo — ver nota de sesión activa arriba.
- **S5/S6 (n8n/MCP):** sin empezar. Solo existen los `.md` de especificación de los workflows (`citas-api/automations/n8n/WF-001/002/003-*.md`), no los JSON reales que exige el entregable final. Requiere que el estudiante tenga n8n accesible y credenciales OAuth propias (precondición del profesor/trainer, ver `GUIA_SESIONES_S2_S6.md` S5).
