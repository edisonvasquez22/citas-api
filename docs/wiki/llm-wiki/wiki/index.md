---
tipo: indice
actualizado: 2026-09-21
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

- **Sesión activa:** S2 — especificar, inicializar y construir el primer incremento. S3 en adelante no ha empezado (requiere nueva aprobación explícita de HU).
- **Repos Git:** `citas-api` y `citas-web` son repos Git reales, con GitHub configurado (`https://github.com/edisonvasquez22/citas-api` y `.../citas-web`, **públicos**, ver [[decisiones]]). `main` y `develop` están sincronizados en ambos (merge fast-forward 2026-09-21).
- **Backlog Scrum:** ver `citas-api/docs/wiki/scrum/README.md` (11 épicas, 24 HU).
- **HU aprobadas para S2:** HU-006, HU-001, HU-002 (aprobación explícita del usuario, ver [[decisiones]]), estado `En desarrollo`. HU-003 (recuperar contraseña) sigue en `Borrador`, planeada para S4.
- **Backend:** vertical slice de autenticación completo (registro, login, refresh con rotación, logout) sobre Spring Boot 3.5.16, con adaptadores JPA/MySQL reales (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`). `mvn test` verificado repetidas veces: **15/15, BUILD SUCCESS**. GOAL_01_GUIADO_SIMPLE ejecutado formalmente: PASS (ver `HU-002-login-y-sesion-jwt.md`). Único pendiente real: nunca se corrió contra MySQL real (falta que el estudiante complete la configuración de Docker/WSL2).
- **Diseño de datos (3FN):** hecho por el agente (decisión del usuario), ver `citas-api/docs/db-design/MODELO_3FN.md` y `COMPARACION_REFERENCIA.md`. Esquema completo del PRD en `V1__esquema_inicial.sql`; solo usuarios/roles/refresh tokens tienen adaptador JPA implementado hoy.
- **Frontend:** `citas-web` tiene la pantalla de login real, conectada a `POST /api/auth/login`/`logout`. Se descartaron dos exports de AI Studio con dominio incorrecto antes de llegar a este (ver [[decisiones]], entradas 2026-09-18). Registro (formulario) y recuperación de contraseña siguen como placeholders informativos, no funcionales. Ver `citas-web/AGENTS.md` para el detalle completo del estado del frontend.
- **Toolchain instalado en la máquina del estudiante:** JDK 21, Maven 3.9.16, Git, Node.js 24 LTS, GitHub CLI. Docker Desktop instalado pero **no operativo todavía** (falta que el estudiante complete el reinicio + configuración de WSL2).
