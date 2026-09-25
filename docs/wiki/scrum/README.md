# Backlog Scrum — Sistema de agendamiento de citas

Generado a partir de `PRD.md` + `RESTRICCIONES_TECNICAS.md` + `database/REQUISITOS_NORMALIZACION_3FN.md`, siguiendo `prompts/spec-driven/PROMPT_INICIO_SCRUM_SPEC.md`. Ningún código fuente fue modificado para producir este backlog.

## Stack detectado/asumido

Backend: Java 21 + Spring Boot 3.5.x + Maven + arquitectura hexagonal + MySQL 8.4 + Flyway + Spring Security JWT (ver versiones exactas en `../llm-wiki/wiki/decisiones.md`). Frontend: React o Angular + TypeScript + Tailwind, a definir por el estudiante al importar desde Google AI Studio (supuesto: aún no hay repositorio de frontend inicializado).

## Épicas

| Épica | Objetivo | HU |
|---|---|---|
| [[EP-001-autenticacion-y-cuentas]] | Registro, login/JWT, recuperación de contraseña | 3 |
| [[EP-002-perfil-y-afiliacion]] | Perfil y afiliación EPS/plan/régimen | 2 |
| [[EP-003-catalogos-del-sistema]] | Catálogos fijos y configurables | 4 |
| [[EP-004-gestion-de-profesionales]] | Alta y administración de profesionales | 2 |
| [[EP-005-agenda-y-disponibilidad]] | Bloques de disponibilidad y consulta | 2 |
| [[EP-006-cita-general]] | Cita general auto-aprobada | 1 |
| [[EP-007-cita-especializada-y-aprobacion]] | Cita especializada + bandeja ADMIN | 2 |
| [[EP-008-mis-citas-cancelacion-reprogramacion]] | Mis citas, cancelación, reprogramación | 4 |
| [[EP-009-agenda-profesional-y-cierre]] | Agenda del profesional y cierre de atención | 2 |
| [[EP-010-auditoria-de-estados]] | Historial de transiciones de estado | 1 |
| [[EP-011-contrato-rest]] | Contrato REST documentado (continuo) | 1 |

Total: 11 épicas, 24 historias de usuario.

## Propuesta de incrementos (sin duración ni capacidad)

### Sprint 1 — fundación + autenticación (objetivo S2)
- [[HU-006-precarga-de-catalogos-fijos]]
- [[HU-001-registrar-usuario]]
- [[HU-002-login-y-sesion-jwt]]
- [[HU-024-contrato-rest-citas-api]] (arranca aquí, continúa todo el proyecto)

### Sprint 2 — núcleo de agendamiento (objetivo S3)
- [[HU-009-administrar-catalogo-especialidades]]
- [[HU-010-registrar-profesional]]
- [[HU-011-activar-desactivar-profesional]]
- [[HU-012-gestionar-bloques-de-disponibilidad]]
- [[HU-013-consultar-disponibilidad]]
- [[HU-014-solicitar-cita-general]]
- [[HU-015-solicitar-cita-especializada]]
- [[HU-016-aprobar-rechazar-cita-especializada]]
- [[HU-023-historial-de-estados-de-cita]]

### Sprint 3 — completar el MVP (objetivo S4)
- [[HU-003-recuperar-contrasena]]
- [[HU-004-consultar-y-actualizar-perfil]]
- [[HU-005-asociar-afiliacion]]
- [[HU-007-administrar-catalogo-eps]]
- [[HU-008-administrar-catalogo-planes-eps]]
- [[HU-017-consultar-mis-citas]]
- [[HU-018-cancelar-cita]]
- [[HU-019-solicitar-reprogramacion]]
- [[HU-020-aprobar-rechazar-reprogramacion]]
- [[HU-021-consultar-agenda-profesional]]
- [[HU-022-marcar-cierre-de-atencion]]

## Alcance aprobado para el incremento S2

Aprobado explícitamente por el usuario el 2026-09-17. En desarrollo:

1. [[HU-006-precarga-de-catalogos-fijos]] — prerrequisito técnico (rol `USER`).
2. [[HU-001-registrar-usuario]] — RF-01.
3. [[HU-002-login-y-sesion-jwt]] — RF-02 (access + refresh + logout).

## Alcance aprobado para el incremento S3

Aprobado explícitamente por el usuario el 2026-09-25 — el alcance completo que ya proponía este backlog para "Sprint 2 (objetivo S3)", sin recortes. En desarrollo:

4. [[HU-009-administrar-catalogo-especialidades]] — RF-06/RF-09 (dependencia de HU-010).
5. [[HU-010-registrar-profesional]] — RF-07.
6. [[HU-011-activar-desactivar-profesional]] — RF-07.
7. [[HU-012-gestionar-bloques-de-disponibilidad]] — RF-08, RN-06/RN-07.
8. [[HU-013-consultar-disponibilidad]] — RF-10, RN-01/RN-05.
9. [[HU-014-solicitar-cita-general]] — RF-11, RN-01/RN-02.
10. [[HU-015-solicitar-cita-especializada]] — RF-12, RN-01/RN-03/RN-08.
11. [[HU-016-aprobar-rechazar-cita-especializada]] — RF-12/RF-18, RN-03/RN-04/RN-09.
12. [[HU-023-historial-de-estados-de-cita]] — RF-19, RN-11/RN-12 (transversal, usada por HU-014/015/016).

Backend implementado y probado (`mvn test` 76/76). La UI correspondiente en `citas-web` no se implementó en S3: el diseño visual sigue reservado al usuario (Stitch/AI Studio) — ver `AGENTS.md` raíz regla 11 y `docs/wiki/llm-wiki/wiki/decisiones.md` (2026-09-25).

`HU-003` (recuperar contraseña) queda en `Borrador` para S4, tal como indica `GUIA_SESIONES_S2_S6.md`. El resto del backlog (EP-002, EP-008 a EP-011) permanece en `Borrador`/`Pendiente de aprobación` hasta S4.

## Decisiones/incógnitas pendientes

- Ver `../llm-wiki/wiki/decisiones.md` y `../llm-wiki/wiki/riesgos.md`.
- El diseño 3FN de la base de datos y el prototipado visual (Stitch/AI Studio) son actividades reservadas al usuario; varias HU tienen tareas de persistencia/UI explícitamente bloqueadas hasta que esas decisiones existan.
