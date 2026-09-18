---
tipo: indice
actualizado: 2026-09-17
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

- **Sesión activa:** S2 — especificar, inicializar y construir el primer incremento.
- **Repos Git:** `citas-api` y `citas-web` **todavía no son repos Git** (pendiente de que el estudiante instale Git/Docker y corra `scripts/init-repos.ps1`; ver [[decisiones]]).
- **Backlog Scrum:** ver `citas-api/docs/wiki/scrum/README.md` (11 épicas, 24 HU).
- **HU aprobadas para S2:** HU-006, HU-001, HU-002 (aprobación explícita del usuario, ver [[decisiones]]), estado `En desarrollo`.
- **Backend:** implementado el vertical slice de autenticación (registro, login, refresh con rotación, logout) sobre Spring Boot 3.5.16, con adaptadores JPA/MySQL reales (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`). Sin ejecutar `mvn test` todavía (sin JDK/Maven en el entorno de generación).
- **Diseño de datos (3FN):** hecho por el agente (decisión del usuario), ver `citas-api/docs/db-design/MODELO_3FN.md` y `COMPARACION_REFERENCIA.md`. Esquema completo del PRD en `V1__esquema_inicial.sql`; solo usuarios/roles/refresh tokens tienen adaptador JPA implementado hoy.
- **Frontend:** `citas-web` sigue vacío; prototipado Stitch/AI Studio pendiente, sigue siendo tarea exclusiva del usuario.
