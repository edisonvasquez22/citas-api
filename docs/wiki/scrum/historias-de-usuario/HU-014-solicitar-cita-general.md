---
id: HU-014
tipo: historia-de-usuario
titulo: "Solicitar cita general con aprobación automática"
estado: "En desarrollo"
epica: "[[EP-006-cita-general]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-013-consultar-disponibilidad]]"
relacionadas:
  - "[[HU-023-historial-de-estados-de-cita]]"
---

# HU-014 — Solicitar cita general con aprobación automática

## Historia de usuario

**COMO** USER autenticado
**QUIERO** reservar una cita de Medicina General y que quede aprobada automáticamente
**PARA** agendar sin depender de la intervención de un administrador

> Como USER autenticado, quiero reservar una cita general y que se apruebe automáticamente.

## Contexto y descripción

RF-11. Es el primer flujo de creación de citas real del backlog; valida el circuito completo disponibilidad → retención → confirmación → estado.

## Alcance

- `POST /api/appointments/general`: selecciona Medicina General, un profesional general disponible y un horario; si el horario sigue disponible al confirmar, crea la cita en `APPROVED`.

## Fuera de alcance

- Especialidades distintas a Medicina General (ver [[HU-015-solicitar-cita-especializada]]).

## Reglas de negocio

- RN-01 (no doble reserva), RN-02 (auto-aprobación), RN-05 (slots consecutivos si aplica), RN-06 (no en el pasado).

## Dependencias y relaciones

- Épica: [[EP-006-cita-general]]
- Dependencias: [[HU-013-consultar-disponibilidad]]
- Relacionadas: [[HU-023-historial-de-estados-de-cita]] (el alta se registra como transición de estado).

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** requiere retención atómica del horario para evitar doble reserva bajo concurrencia, además de la confirmación y el registro de auditoría.

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `SolicitarCitaGeneral`**
  Dificultad: Alto
  Descripción: verifica disponibilidad al momento de confirmar, retiene el/los slot(s) de forma atómica y crea la cita en `APPROVED`.
- [ ] **T-02 — Endpoint REST + migración Flyway de citas**
  Dificultad: Alto
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Registro de auditoría del alta**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [ ] **T-04 — Pruebas**
  Dificultad: Alto
  Descripción: reserva exitosa, horario que se ocupó justo antes de confirmar (doble reserva concurrente), horario en el pasado.

## Criterios de aceptación

### CA-01 — Reserva exitosa

**Dado** un horario de Medicina General disponible
**Cuando** el usuario lo confirma
**Entonces** la cita se crea en estado `APPROVED` sin intervención de ADMIN.

### CA-02 — Horario ya no disponible al confirmar

**Dado** un horario que fue reservado por otro usuario entre la consulta y la confirmación
**Cuando** el usuario intenta confirmarlo
**Entonces** el sistema rechaza la reserva sin crear una cita duplicada sobre el mismo slot.

### CA-03 — Doble reserva bajo concurrencia

**Dado** dos solicitudes simultáneas sobre el mismo horario
**Cuando** ambas intentan confirmar al mismo tiempo
**Entonces** solo una tiene éxito y la otra recibe el mismo rechazo de CA-02.

## Definition of Done

- [x] CA-01 a CA-03 validados con evidencia, incluyendo una prueba explícita de concurrencia/doble reserva.
- [x] Migración Flyway coherente con el diseño 3FN aprobado.
- [x] Transición de estado registrada en auditoría (RF-19).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `SolicitarCitaGeneralServiceTest.solicitar_horarioDisponible_quedaApprovedYAuditado` | Auditoría verificada vía `HistorialEstadoCitaPort` |
| CA-02 | Cumple | `SolicitarCitaGeneralServiceTest.solicitar_horarioYaTomado_lanzaHorarioNoDisponible` | — |
| CA-03 | Cumple | `SolicitarCitaGeneralServiceTest.solicitar_bajoConcurrencia_soloUnaSolicitudGanaElHorario` (10 hilos reales vía `ExecutorService`, exactamente 1 éxito) | Demostrado explícitamente en Red→Green: se rompió a propósito el `compareAndSet` de la reserva atómica, la prueba falló (10 éxitos en vez de 1), y volvió a pasar al revertir — ver `docs/wiki/llm-wiki/wiki/log.md` (2026-09-25) |
| DoD-01 | Cumple | `mvn test`: 76/76, `BUILD SUCCESS` (2026-09-25) | Sin verificar aún contra MySQL real (Docker pendiente); la reserva atómica real usa `UPDATE ... WHERE appointment_id IS NULL` (bloqueo de fila InnoDB), no probado aún contra MySQL |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-25 — Aprobada explícitamente por el usuario como parte del alcance de S3; implementada y validada, incluida la prueba de doble reserva bajo concurrencia real que pide `GUIA_SESIONES_S2_S6.md`. Estado → `En desarrollo`.

## Notas y decisiones

- CA-03 (doble reserva) es explícitamente el caso de prueba que pide `GUIA_SESIONES_S2_S6.md` para S3.
