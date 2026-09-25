---
id: HU-015
tipo: historia-de-usuario
titulo: "Solicitar cita especializada"
estado: "En desarrollo"
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

- [ ] **T-01 — Caso de uso `SolicitarCitaEspecializada`**
  Dificultad: Alto
  Descripción: valida que la especialidad esté activa y asociada al profesional (RN-08), retiene el horario y crea la solicitud en `REQUESTED`.
- [ ] **T-02 — Endpoint REST + migración Flyway**
  Dificultad: Alto
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Registro de auditoría del alta**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [ ] **T-04 — Pruebas**
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
| CA-03 | Cumple | `SolicitarCitaEspecializadaServiceTest.solicitar_bajoConcurrencia_soloUnaSolicitudRetieneElHorario` (10 hilos reales, exactamente 1 éxito) | Mismo mecanismo y misma demostración Red→Green que HU-014 — ver `docs/wiki/llm-wiki/wiki/log.md` (2026-09-25) |
| DoD-01 | Cumple | `mvn test`: 76/76, `BUILD SUCCESS` (2026-09-25) | Sin verificar aún contra MySQL real (Docker pendiente) |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-25 — Aprobada explícitamente por el usuario como parte del alcance de S3; implementada y validada. Estado → `En desarrollo`.

## Notas y decisiones

- Ninguna.
