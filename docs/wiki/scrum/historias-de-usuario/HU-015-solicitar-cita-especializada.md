---
id: HU-015
tipo: historia-de-usuario
titulo: "Solicitar cita especializada"
estado: Terminada
epica: "[[EP-007-cita-especializada-y-aprobacion]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-013-consultar-disponibilidad]]"
relacionadas:
  - "[[HU-016-aprobar-rechazar-cita-especializada]]"
  - "[[HU-023-historial-de-estados-de-cita]]"
---

# HU-015 — Solicitar cita especializada

## Historia de usuario

**COMO** USER autenticado
**QUIERO** solicitar una cita en una especialidad distinta a Medicina General
**PARA** ser atendido por el profesional adecuado, aunque requiera aprobación administrativa

> Como USER autenticado, quiero solicitar una cita especializada aunque requiera aprobación de un administrador.

## Contexto y descripción

RF-12. A diferencia de la cita general, nace en `REQUESTED` y retiene el horario mientras se decide.

## Alcance

- `POST /api/appointments/specialized`: selecciona especialidad, sede, profesional y horario; crea la solicitud en `REQUESTED` reteniendo el horario.

## Fuera de alcance

- La decisión de ADMIN (ver [[HU-016-aprobar-rechazar-cita-especializada]]).

## Reglas de negocio

- RN-01 (retención evita doble reserva), RN-03 (requiere ADMIN para pasar a `APPROVED`), RN-08 (especialidad activa y asociada al profesional).

## Dependencias y relaciones

- Épica: [[EP-007-cita-especializada-y-aprobacion]]
- Dependencias: [[HU-013-consultar-disponibilidad]]
- Relacionadas: [[HU-016-aprobar-rechazar-cita-especializada]], [[HU-023-historial-de-estados-de-cita]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** retención atómica del horario en estado intermedio (`REQUESTED`), con la misma exigencia anti doble-reserva que la cita general.

## Tareas de desarrollo

- [x] **T-01 — Caso de uso `SolicitarCitaEspecializada`**
  Dificultad: Alto
  Descripción: valida que la especialidad esté activa y asociada al profesional (RN-08), retiene el horario y crea la solicitud en `REQUESTED`.
- [x] **T-02 — Endpoint REST + migración Flyway**
  Dificultad: Alto
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [x] **T-03 — Registro de auditoría del alta**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [x] **T-04 — Pruebas**
  Dificultad: Alto
  Descripción: solicitud exitosa, especialidad no asociada al profesional, especialidad inactiva, doble reserva concurrente sobre el mismo horario.

## Criterios de aceptación

### CA-01 — Solicitud exitosa

**Dado** un horario disponible de una especialidad activa asociada al profesional elegido
**Cuando** el usuario solicita la cita
**Entonces** se crea en estado `REQUESTED` y el horario queda retenido para otros usuarios.

### CA-02 — Especialidad no asociada o inactiva

**Dado** una especialidad no asociada al profesional elegido, o desactivada
**Cuando** el usuario intenta solicitar la cita
**Entonces** el sistema rechaza la solicitud.

### CA-03 — Doble reserva bajo concurrencia

**Dado** dos solicitudes simultáneas sobre el mismo horario
**Cuando** ambas intentan solicitar al mismo tiempo
**Entonces** solo una retiene el horario y la otra es rechazada.

## Definition of Done

- [x] CA-01 a CA-03 validados con evidencia, incluyendo una prueba explícita de concurrencia/doble reserva.
- [x] Migración Flyway coherente con el diseño 3FN aprobado.
- [x] Transición de estado registrada en auditoría (RF-19).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `SolicitarCitaEspecializadaServiceTest.solicitar_conEspecialidadAsociadaYHorarioLibre_naceEnRequested` | — |
| CA-02 | Cumple | `SolicitarCitaEspecializadaServiceTest.solicitar_conEspecialidadNoAsociadaAlProfesional_seRechaza`, `.solicitar_conEspecialidadInactiva_seRechaza` | — |
| CA-03 | Cumple | `SolicitarCitaEspecializadaServiceTest.solicitar_bajoConcurrencia_soloUnaSolicitudRetieneElHorario` (10 hilos reales, exactamente 1 éxito) | Mismo mecanismo y misma demostración Red→Green que HU-014 — ver `docs/wiki/llm-wiki/wiki/log.md` (2026-09-25) Verificado además con 10 peticiones HTTP simultáneas contra MySQL 8.4 real (2026-10-02, base temporal): 1 reserva (201) y 9 conflictos (409). |
| DoD-01 | Cumple | `mvn test`: 76/76, `BUILD SUCCESS` (2026-09-25); app arrancada contra MySQL 8.4 real (2026-10-02 con MySQL nativo en este equipo; antes, 2026-09-30, con Docker en otro equipo) | Conexión real a MySQL verificada; ver HU-014 para la nota de la prueba de concurrencia específica, aún no repetida contra MySQL real. |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-25 — Aprobada explícitamente por el usuario como parte del alcance de S3; implementada y validada. Estado → `En desarrollo`.

## Notas y decisiones

- Ninguna.

## Ejecución formal de GOAL_02 (2026-10-02)

`GOAL_02_GUIADO_AVANZADO.md` se había diferido el 2026-09-25 porque exigía que el frontend enviara la solicitud real y esa pantalla no existía. Hoy existe (`BookingView.tsx`), así que se verificó cada condición de parada con evidencia:

| Condición de la meta | Resultado | Evidencia |
|---|---|---|
| USER selecciona sede + especialidad + profesional + horario | PASS | `BookingView.tsx`: selector de sede (2 sedes), especialidad, profesional filtrado por especialidad y sede, y horarios de `GET /api/availability`. |
| El frontend envía la solicitud real a `citas-api` | PASS | `BookingView.tsx` (`POST /api/appointments/specialized` con profesionalId, sedeId, especialidadId, fecha y hora); sin datos simulados. |
| El backend retiene los slots y la cita queda `REQUESTED` | PASS | `SolicitarCitaEspecializadaServiceTest`; además, contra MySQL 8.4 real (2026-10-02), 3 solicitudes especializadas quedaron `REQUESTED`. |
| Una segunda reserva incompatible no puede tomar esos slots | PASS | `SolicitarCitaEspecializadaServiceTest.solicitar_bajoConcurrencia_soloUnaSolicitudRetieneElHorario` (10 hilos); y contra MySQL real 10 peticiones simultáneas → 1×201 y 9×409. |
| Respuesta y errores se muestran en el frontend | PASS | `BookingView.tsx` maneja `409` (horario perdido), `400`/`404` (mensaje del backend) y error de red con mensajes visibles. |
| Pruebas backend del flujo pasan | PASS | `mvn test`: 161/161. |
| El frontend compila y sus verificaciones pasan | PASS | `npm run build` y `npm run lint` sin errores; `vitest`: 17/17 (ejecutado en serie; en paralelo este equipo agota el tiempo de arranque de los workers). |
| Sin secretos hardcodeados | PASS | Hook de pre-commit (versionado en `scripts/git-hooks/`) y búsqueda de secretos conocidos en lo versionado: sin hallazgos. |
| No se implementa la decisión ADMIN en esta HU | PASS | Es HU-016, aprobada y verificada por separado. |

Resultado: **GOAL_02 PASS**. Verificación hecha por revisión directa del agente (sin Verifier aislado), que es la diferencia con LOOP_01/LOOP_02.
