---
id: HU-019
tipo: historia-de-usuario
titulo: "Solicitar reprogramación de cita"
estado: "En desarrollo"
epica: "[[EP-008-mis-citas-cancelacion-reprogramacion]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-017-consultar-mis-citas]]"
  - "[[HU-013-consultar-disponibilidad]]"
relacionadas:
  - "[[HU-020-aprobar-rechazar-reprogramacion]]"
---

# HU-019 — Solicitar reprogramación de cita

## Historia de usuario

**COMO** USER autenticado
**QUIERO** solicitar una nueva fecha/hora para una cita ya aprobada
**PARA** ajustar mi cita sin perderla mientras se decide

> Como USER autenticado, quiero solicitar una reprogramación sin perder mi cita original mientras se decide.

## Contexto y descripción

RF-15. Solo una cita `APPROVED` y futura puede solicitar reprogramación; conserva profesional y especialidad. La cita original conserva su franja hasta que ADMIN decida (RN-10).

## Alcance

- `POST /api/appointments/{id}/reschedule`: USER elige nueva fecha/hora disponible para el mismo profesional/especialidad; la nueva franja se retiene mientras la solicitud está `PENDING`.

## Fuera de alcance

- Cambiar de profesional (se trata como una cita nueva, no como reprogramación).
- La decisión de ADMIN (ver [[HU-020-aprobar-rechazar-reprogramacion]]).

## Reglas de negocio

- Solo cita `APPROVED` y futura es reprogramable.
- Conserva profesional y especialidad.
- La nueva franja se retiene mientras la solicitud está `PENDING` (RN-01); la cita original no se toca hasta la decisión (RN-10).

## Dependencias y relaciones

- Épica: [[EP-008-mis-citas-cancelacion-reprogramacion]]
- Dependencias: [[HU-017-consultar-mis-citas]], [[HU-013-consultar-disponibilidad]]
- Relacionadas: [[HU-020-aprobar-rechazar-reprogramacion]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** coexisten dos reservas (original vigente + nueva retenida) sin que una invalide a la otra hasta la decisión administrativa.

## Tareas de desarrollo

- [x] **T-01 — Caso de uso `SolicitarReprogramacion`**
  Dificultad: Alto
  Descripción: valida estado `APPROVED`/futuro, mismo profesional/especialidad, retiene la nueva franja sin tocar la original.
- [x] **T-02 — Endpoint REST**
  Dificultad: Alto
  Descripción: `reschedule_requests`/`reschedule_request_statuses` ya existen en `V1__esquema_inicial.sql` (esquema de referencia adoptado 2026-09-23) — no hizo falta migración nueva, solo el `@Entity`/adaptador correspondiente (`RescheduleRequestJpaEntity`/`SolicitudReprogramacionJpaAdapter`).
- [x] **T-03 — Pruebas**
  Dificultad: Alto
  Descripción: solicitud válida, cita no `APPROVED`, cita pasada, nuevo horario ya ocupado, verificación de que la cita original sigue intacta mientras está `PENDING`.

## Criterios de aceptación

### CA-01 — Solicitud válida

**Dado** una cita `APPROVED` y futura, y un nuevo horario disponible del mismo profesional/especialidad
**Cuando** el usuario solicita la reprogramación
**Entonces** se crea una solicitud `PENDING`, la nueva franja queda retenida y la cita original conserva su franja intacta.

### CA-02 — Cita no reprogramable

**Dado** una cita que no está `APPROVED` (p. ej. `REQUESTED`, `CANCELLED`) o no es futura
**Cuando** el usuario intenta reprogramarla
**Entonces** el sistema rechaza la solicitud.

### CA-03 — Nuevo horario no disponible

**Dado** un horario nuevo que ya está reservado/retenido
**Cuando** el usuario lo solicita como reprogramación
**Entonces** el sistema rechaza la solicitud sin afectar la cita original.

## Definition of Done

- [x] CA-01 a CA-03 validados con evidencia.
- [x] Entidad/adaptador JPA coherente con `reschedule_requests`/`reschedule_request_statuses` (ya existentes en `V1__esquema_inicial.sql`).
- [x] Transición registrada (ver observación DoD-02 sobre el mecanismo exacto).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_horarioDisponible_quedaPendingSinTocarLaCitaOriginal` | — |
| CA-02 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_citaNoAprobada_seRechaza` | — |
| CA-03 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_nuevoHorarioNoDisponible_seRechazaSinAfectarLaCitaOriginal` | — |
| DoD-02 | Cumple | `SolicitudReprogramacion.solicitar()` (estado `PENDING` al crear) | La transición vive en el propio `reschedule_requests.status_id` (y luego `decided_by_user_id`/`decided_at`/`decision_reason` al decidir, ver HU-020), no en `appointment_status_history`: el `EstadoCita` de la cita no cambia mientras la solicitud está pendiente (RN-10), así que no aplica un registro ahí |
| DoD-01 | Cumple | `mvn test`: 106/106, `BUILD SUCCESS` (2026-09-29) | Sin verificar aún contra MySQL real (Docker pendiente); sin UI en `citas-web` todavía |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`). Se corrigió T-02/DoD: la migración ya no está bloqueada, el esquema de referencia (adoptado 2026-09-23) ya incluye `reschedule_requests`.
- 2026-09-29 — Backend implementado y validado (`POST /api/appointments/{id}/reschedule`). Nuevo agregado de dominio `SolicitudReprogramacion`; durante `PENDING`, franja antigua y nueva quedan ambas retenidas bajo el mismo `citaId` en `professional_slots`, distinguibles por horario (ver HU-020 para cómo se resuelve al decidir). Estado → `En desarrollo`.

## Notas y decisiones

- Buena candidata para el GOAL/loop cross-repo de S3 (`GOAL_02_GUIADO_AVANZADO.md`) por combinar reglas de dominio complejas con UI de confirmación.
