---
id: HU-020
tipo: historia-de-usuario
titulo: "Aprobar/rechazar reprogramación"
estado: "En desarrollo"
epica: "[[EP-008-mis-citas-cancelacion-reprogramacion]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-019-solicitar-reprogramacion]]"
relacionadas:
  - "[[HU-016-aprobar-rechazar-cita-especializada]]"
  - "[[HU-023-historial-de-estados-de-cita]]"
---

# HU-020 — Aprobar/rechazar reprogramación

## Historia de usuario

**COMO** ADMIN
**QUIERO** aprobar o rechazar una solicitud de reprogramación `PENDING`
**PARA** decidir si el cambio de horario se hace efectivo

> Como ADMIN, quiero aprobar o rechazar una reprogramación para decidir si el cambio de horario se hace efectivo.

## Contexto y descripción

RF-15 (decisión) + RF-18 (aparece en la bandeja administrativa junto a las citas especializadas `REQUESTED`).

## Alcance

- Bandeja de reprogramaciones `PENDING` (puede compartir el endpoint de listado con [[HU-016-aprobar-rechazar-cita-especializada]] o exponer uno propio, a decidir en implementación).
- `POST /api/admin/reschedules/{id}/approve`: libera los slots antiguos, asigna los nuevos y actualiza la cita.
- `POST /api/admin/reschedules/{id}/reject`: exige motivo, libera la reserva provisional y mantiene la cita original.

## Fuera de alcance

- La solicitud en sí ([[HU-019-solicitar-reprogramacion]]).

## Reglas de negocio

- RN-10 (la cita original no se destruye hasta la aprobación), RN-09 (liberar reservas al rechazar/aprobar según corresponda), RN-04 (rechazo exige motivo).
- Tras el rechazo, USER puede conservar la cita original o cancelarla (ver [[HU-018-cancelar-cita]]).

## Dependencias y relaciones

- Épica: [[EP-008-mis-citas-cancelacion-reprogramacion]]
- Dependencias: [[HU-019-solicitar-reprogramacion]]
- Relacionadas: [[HU-016-aprobar-rechazar-cita-especializada]], [[HU-023-historial-de-estados-de-cita]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** la aprobación mueve dos reservas (libera antigua + confirma nueva) de forma atómica; el rechazo debe dejar la cita original exactamente como estaba.

## Tareas de desarrollo

- [x] **T-01 — Casos de uso `AprobarReprogramacion`/`RechazarReprogramacion`**
  Dificultad: Alto
  Descripción: aprobar libera slots antiguos y asigna los nuevos en la misma operación; rechazar libera solo la reserva provisional.
- [x] **T-02 — Endpoint de bandeja + acciones**
  Dificultad: Medio
  Descripción: **decisión de implementación**: endpoint propio (`AdminReschedulesController`, `/api/admin/reschedules`), no compartido con HU-016. `GET` sin filtros (todas las `PENDING`) — el alcance original mencionaba filtros por sede/profesional/especialidad, pero eso exigiría un join `reschedule_requests`↔`appointments` sin relación JPA mapeada; se dejó fuera por no estar cubierto por ningún CA numerado, ver nota abajo.
- [x] **T-03 — Registro de auditoría de la transición**
  Dificultad: Bajo
  Descripción: ver DoD-02 — la transición vive en `reschedule_requests` mismo (`status_id`/`decided_by_user_id`/`decided_at`/`decision_reason`), no en `appointment_status_history` (la cita no cambia de `EstadoCita` en este flujo).
- [x] **T-04 — Pruebas**
  Dificultad: Alto
  Descripción: aprobación exitosa (verifica liberación de slots antiguos y asignación de nuevos), rechazo con motivo (verifica que la cita original quedó intacta), rechazo sin motivo. **No incluye** una prueba de concurrencia dedicada para doble-aprobación simultánea de la misma solicitud (a diferencia de RN-01 en HU-014/015) — ver DoD-03.

## Criterios de aceptación

### CA-01 — Aprobación

**Dado** una solicitud de reprogramación `PENDING`
**Cuando** ADMIN la aprueba
**Entonces** se liberan los slots de la franja antigua, se confirman los de la nueva franja y la cita queda actualizada con el nuevo horario.

### CA-02 — Rechazo con motivo

**Dado** una solicitud de reprogramación `PENDING`
**Cuando** ADMIN la rechaza indicando un motivo
**Entonces** se libera únicamente la reserva provisional de la nueva franja y la cita original permanece exactamente como estaba antes de la solicitud.

### CA-03 — Rechazo sin motivo

**Dado** un intento de rechazo sin motivo
**Cuando** ADMIN lo envía
**Entonces** el sistema rechaza la operación por validación.

## Definition of Done

- [x] CA-01 a CA-03 validados con evidencia.
- [x] Transición registrada (ver DoD-02: mecanismo distinto a `appointment_status_history`).
- [x] `mvn test` pasa para los módulos afectados.
- [ ] Prueba dedicada de atomicidad de la aprobación bajo concurrencia (ver DoD-03: no incluida).
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `GestionarReprogramacionesServiceTest.aprobar_liberaFranjaAntiguaYActualizaLaCitaConLaNueva` | Verifica que la franja antigua queda libre y la cita apunta al nuevo horario |
| CA-02 | Cumple | `GestionarReprogramacionesServiceTest.rechazar_conMotivo_liberaSoloLaFranjaNuevaYDejaLaCitaOriginalIntacta` | Verifica explícitamente que la franja antigua sigue ocupada y la nueva quedó libre |
| CA-03 | Cumple | `GestionarReprogramacionesServiceTest.rechazar_sinMotivo_seRechaza` | — |
| DoD-02 | Cumple | `SolicitudReprogramacion.aprobar()`/`rechazar()` (columnas `decided_by_user_id`/`decided_at`/`decision_reason`) | Mismo razonamiento que HU-019 DoD-02: no hay entrada en `appointment_status_history` porque el `EstadoCita` de la cita no cambia en este flujo (RN-10) |
| DoD-03 | No verificable | — | No se escribió una prueba de concurrencia (10 hilos, patrón de HU-014/015 RN-01) para doble-aprobación simultánea de la misma solicitud. Riesgo acotado: aunque ocurriera, la atomicidad real de los slots (RN-01, ya probada) sigue protegiendo contra doble-reserva del horario; el peor caso es una ambigüedad de "quién decidió" en `reschedule_requests`, no una inconsistencia de agenda. Pendiente si se quiere cerrar formalmente. |
| DoD-01 | Cumple | `mvn test`: 106/106, `BUILD SUCCESS` (2026-09-29) | Sin verificar aún contra MySQL real (Docker pendiente); sin UI en `citas-web` todavía |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`).
- 2026-09-29 — Backend implementado y validado (`GET/POST /api/admin/reschedules/**`). Estado → `En desarrollo`. Queda pendiente (no bloqueante) la prueba de concurrencia de DoD-03 si se quiere elevar el rigor al nivel de RN-01.

## Notas y decisiones

- Ninguna.
