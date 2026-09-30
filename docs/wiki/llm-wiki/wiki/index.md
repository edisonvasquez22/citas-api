---
tipo: indice
actualizado: 2026-09-30 (todas las pantallas y backend del PRD; verificado contra MySQL real; S5/S6 n8n en curso)
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

- **Funcionalidad (2026-09-30):** backend y frontend cubren todas las HU y pantallas obligatorias del PRD para los 3 roles. `mvn test` 159/159; `npm test` 17/17, `npm run lint` y `npm run build` sin errores. Verificado contra **MySQL 8.4 real en Docker** con recorridos Playwright (16 flujos en ES + recorrido completo en EN).
- **Repos:** `citas-api` y `citas-web` en GitHub (`edisonvasquez22`), rama de trabajo `develop`. La raíz del workspace no es repo git: `docker-compose.yml`, `.env.example` y `scripts/` se copian aparte.
- **Arranque:** `docker compose up -d` → `mvn spring-boot:run` en `citas-api-dev` (Flyway V1–V4) → `npm run dev` en `citas-web-dev` → `scripts/seed-demo.ps1` para datos demo (contraseña `Demo1234*`), o `ADMIN_BOOTSTRAP_*` para crear solo un ADMIN.
- **S5/S6 (n8n/MCP):** MCP de n8n conectado. El backend ya está listo: API key, endpoints de recordatorios y resumen diario, y webhook de cambios de estado. Faltan los workflows WF-001/002/003 en n8n y sus JSON exportados; están bloqueados por la credencial Gmail (la crea el usuario) y por una URL pública de la API para WF-001/003. Ver [[riesgos]].
- **Pendiente de proceso (S2–S4):** hook de pre-commit con evidencia FAIL/PASS, `AGENTS.md` raíz, logs de Builder/Verifier por ejecución y cierre formal de HU/épicas (todas siguen en "En desarrollo" o "Borrador" hasta validar el DoD con el usuario).
- **Decisiones recientes:** hexagonal pragmática (Spring en la capa de aplicación), unicidad de especialidad primaria en BD (V4), i18n propio sin librería. Ver [[decisiones]].
