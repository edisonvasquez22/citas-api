---
tipo: indice
actualizado: 2026-10-02 (backlog cruzado contra PRD/código; MySQL real sin Docker verificado en este equipo; S5/S6 n8n en curso)
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

- **Funcionalidad (2026-09-30):** backend y frontend cubren todas las HU y pantallas obligatorias del PRD para los 3 roles. `mvn test` 159/159; `npm test` 17/17, `npm run lint` y `npm run build` sin errores. Un cruce exhaustivo (3 agentes, 2026-10-02) contra PRD §6 (17 pantallas obligatorias) y §8 (seguridad mínima) confirmó que ambos están cubiertos end-to-end sin mocks.
- **MySQL real, en este equipo, sin Docker (2026-10-02):** Docker Desktop resultó inviable en la máquina del estudiante (3 fallos distintos en 3 días: WSL desactualizado, VPN corporativa rompiendo la red de WSL2, fallo del túnel `vpnkit`). Se instaló **MySQL Community Server 8.4.9 nativo en Windows** (sin Docker/WSL) — decisión aprobada explícitamente por el usuario, ver [[decisiones]]. `mvn spring-boot:run` aplicó `V1`→`V4` con Flyway por primera vez en este equipo, creó el primer ADMIN real (`AdminBootstrapRunner`) y se verificó end-to-end con peticiones HTTP reales (login, perfil, catálogos, endpoints ADMIN). Resuelve el bloqueador de DoD que tenían HU-001, HU-002, HU-006 y HU-009 a HU-016 (actualizadas individualmente). (Nota: el log del 2026-09-30 registra una verificación previa contra MySQL en Docker, pero desde una copia distinta del proyecto en otra carpeta — no esta misma instancia del workspace.)
- **Repos:** `citas-api` y `citas-web` en GitHub (`edisonvasquez22`), rama de trabajo `develop`. La raíz del workspace no es repo git: `docker-compose.yml`, `.env.example` y `scripts/` se copian aparte.
- **Arranque en este equipo (sin Docker):** servicio de Windows `MySQL84` ya queda corriendo solo → `mvn spring-boot:run` en `citas-api` (con las variables de `.env` cargadas en el entorno) → `npm run dev` en `citas-web`. `ADMIN_BOOTSTRAP_EMAIL`/`ADMIN_BOOTSTRAP_PASSWORD` en `.env` crean el primer ADMIN automáticamente al arrancar. `scripts/seed-demo.ps1` (datos demo completos) asume Docker/MySQL en el puerto del `docker-compose.yml` — revisar antes de usarlo en este equipo.
- **S5/S6 (n8n/MCP):** MCP de n8n conectado. El backend ya está listo: API key, endpoints de recordatorios y resumen diario, y webhook de cambios de estado. Faltan los workflows WF-001/002/003 en n8n y sus JSON exportados; están bloqueados por la credencial Gmail (la crea el usuario) y por una URL pública de la API para WF-001/03. Ver [[riesgos]].
- **Pendiente real detectado en el cruce del 2026-10-02 (sin contar n8n):** HU-020 sin prueba de concurrencia dedicada para doble-aprobación de reprogramación; RF-18 incompleto en la bandeja de reprogramaciones (sin filtros, a diferencia de la de citas especializadas); HU-024 (contrato REST) y la evidencia de HU-018/021/022/023 desactualizadas frente al código real (la UI de esas 4 ya existe y funciona, pero sus HU todavía dicen "sin UI todavía"/"parcial").
- **Pendiente de proceso (S2–S4):** hook de pre-commit con evidencia FAIL/PASS, `AGENTS.md` raíz, logs de Builder/Verifier por ejecución y cierre formal de HU/épicas (todas siguen en "En desarrollo" o "Borrador" hasta validar el DoD con el usuario).
- **Decisiones recientes:** hexagonal pragmática (Spring en la capa de aplicación), unicidad de especialidad primaria en BD (V4), i18n propio sin librería, MySQL nativo en vez de Docker. Ver [[decisiones]].
