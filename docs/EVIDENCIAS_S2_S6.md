# Evidencias y trazabilidad S2–S6

Índice para quien revise **solo los repositorios** (`citas-api` y `citas-web`, ramas `develop` y `main`, públicos). Cada fila apunta al commit y al documento donde está la prueba. Detalle cronológico completo: `docs/wiki/llm-wiki/wiki/log.md`.

Repos: `https://github.com/edisonvasquez22/citas-api` · `https://github.com/edisonvasquez22/citas-web`. `main` y `develop` están sincronizados en el último commit de cada repo.

## Cómo reproducir

```text
citas-api:  mvn test                 → 161 pruebas, 0 fallos (perfil "test", sin MySQL)
            mvn spring-boot:run      → con MySQL 8.4 y las variables de .env.example; aplica Flyway V1–V4
citas-web:  npm ci && npm run lint && npm run build && npm test
```

Nota: en un equipo lento, `npm test` puede agotar el tiempo de arranque de los workers de Vitest; en ese caso usar `npx vitest run --no-file-parallelism --maxWorkers=1` (las 17 pruebas pasan).

## Evidencia por sesión

| Sesión | Qué pide la guía | Evidencia | Commits (api / web) |
|---|---|---|---|
| **S2** | Repos, AGENTS, Scrum con HU aprobadas, wiki, BD con migraciones, registro + login JWT, frontend con login/registro, GOAL_01, subagente | `AGENTS.md` de ambos repos; `docs/wiki/scrum/`; `docs/wiki/llm-wiki/`; `docs/db-design/` (3FN + comparación con la referencia); HU-001, HU-002 (sección "Ejecución formal de GOAL_01": PASS); log 2026-09-17 (investigación delegada a subagente) | `9999dbf` `47fa961` / `5fd9167` `55bfc2d` `5e927f1` |
| **S3** | Flujo general + especializado, aprobación admin, pruebas, Red→Green, doble reserva, hook FAIL/PASS, secreto ficticio bloqueado, GOAL_02 | HU-009 a HU-016 y HU-023; log 2026-09-25 (Red→Green de la atomicidad de reserva; hook bloqueando un secreto ficticio y luego commit permitido); hook versionado en `scripts/git-hooks/`; HU-015 sección "Ejecución formal de GOAL_02": PASS | `29d8028` `6e4ffb4` `db528ff` `886b1b7` / `4419291` `bca9c88` `72f978c` |
| **S4** | Mis citas, cancelar, reprogramar, agenda del profesional, COMPLETED/NO_SHOW, EPS/planes/especialidades, recuperar contraseña, historial, loop guiado + propio con logs | HU-003 a HU-008 y HU-017 a HU-022; LOOP_01 y LOOP_02 con Builder/Verifier aislados (HU-019, log 2026-09-29); LOOP_03 (HU-019 CA-04, Red→Green); HU-020 con prueba de concurrencia | `2d9ef9b` `539e686` `4f88e9c` `a9470a8` `ebd65be` / `5949800` `dd8fcec` `8d844b4` |
| **S5** | WF-001 JSON, evidencia MCP, riesgos residuales | `automations/n8n/WF-001-appointment-reminders.json`; `automations/n8n/README.md` (evidencia MCP: listar, inspeccionar, ejecutar #72/#74, hallazgo y corrección de zona horaria; riesgos residuales) | `afd4ad7` `4ac858b` `c7a67e9` `322a710` |
| **S6** | WF-002 JSON, WF-003 (bonus), validaciones, `main` estable | `automations/n8n/WF-002-status-notifications.json` y `WF-003-daily-operational-summary.json`; WF-002 probado de punta a punta con la API real; verificación contra MySQL 8.4 real (concurrencia RN-01, HU-020, LOOP_03, filtros RF-18); HU-001 a HU-023 y EP-001 a EP-010 en `Terminada` | `b9e06ef` `afd4ad7` / `3033541` |

## Verificaciones destacadas

- **Pruebas:** backend 161/161; frontend 17/17 (`npm run lint` y `npm run build` sin errores).
- **MySQL real:** Flyway V1–V4 aplicado; concurrencia con 10 peticiones simultáneas → 1 éxito y 9 conflictos en RN-01, aprobación de reprogramación y solicitud duplicada de reprogramación (HU-014, HU-015, HU-019, HU-020).
- **Seguridad:** BCrypt, JWT access/refresh con rotación, roles + ownership (404 fuera de dueño), CORS explícito, validación en servidor, secretos solo en variables de entorno, hook anti-secretos. Ningún `.env` está versionado.
- **n8n:** los JSON se importan sin secretos; las credenciales (Gmail OAuth, `X-Webhook-Secret`, `X-Integration-Key`) se crean en n8n. Todos los correos van a un destinatario de laboratorio.
- **Contrato REST:** 52 endpoints implementados = 52 documentados (`docs/wiki/llm-wiki/wiki/contratos.md`, HU-024).

## Qué hay que saber (limitaciones y decisiones, dichas con honestidad)

- **Docker:** el proyecto se levantó con Docker (MySQL 8.4) en otro equipo (2026-09-30); en el equipo de la última verificación Docker no fue viable y se usó MySQL Community Server nativo (2026-10-02). Ambas verificaciones están registradas en las HU.
- **LOOP_03** lo diseñó el agente orquestador, presentado y aprobado explícitamente por el estudiante antes de ejecutarse (HU-019, log 2026-09-30).
- **GOAL_02** se difirió el 2026-09-25 hasta tener la pantalla real de agendar y se ejecutó formalmente el 2026-10-02; la verificación fue por revisión directa, sin Verifier aislado.
- **n8n:** WF-001 y WF-003 se ejecutaron a mano contra la API expuesta con un túnel temporal; no se publicaron con horario porque la API no tiene URL pública estable. WF-002 sí está publicado.
- **Gmail OAuth:** el nodo de n8n exige un alcance amplio; se recomienda cuenta de laboratorio y revocar el acceso al terminar.
- **Diseño visual:** lo decidió y ejecutó el estudiante (Stitch + AI Studio); el agente solo reconcilió contra la API real.
