---
id: HU-020
tipo: historia-de-usuario
titulo: "Aprobar/rechazar reprogramación"
estado: Terminada
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
  Actualización 2026-10-02: aprobar y rechazar se ejecutan bajo `CitaRepositoryPort.conBloqueoDeEscritura` (misma técnica que LOOP_03) y releen la solicitud dentro del bloqueo, así que decisiones simultáneas sobre la misma solicitud se serializan y solo la primera tiene éxito.
- [x] **T-02 — Endpoint de bandeja + acciones**
  Dificultad: Medio
  Descripción: **decisión de implementación**: endpoint propio (`AdminReschedulesController`, `/api/admin/reschedules`), no compartido con HU-016. `GET` con filtros opcionales `sedeId`/`profesionalId`/`especialidadId`/`fecha` (RF-18, agregados el 2026-10-02; `sedeId` y `fecha` aplican a lo SOLICITADO, profesional y especialidad se resuelven desde la cita).
- [x] **T-03 — Registro de auditoría de la transición**
  Dificultad: Bajo
  Descripción: ver DoD-02 — la transición vive en `reschedule_requests` mismo (`status_id`/`decided_by_user_id`/`decided_at`/`decision_reason`), no en `appointment_status_history` (la cita no cambia de `EstadoCita` en este flujo).
- [x] **T-04 — Pruebas**
  Dificultad: Alto
  Descripción: aprobación exitosa (verifica liberación de slots antiguos y asignación de nuevos), rechazo con motivo (verifica que la cita original quedó intacta), rechazo sin motivo, filtros de la bandeja y aprobación concurrente (10 hilos sobre la misma solicitud).

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
- [x] Prueba dedicada de atomicidad de la aprobación bajo concurrencia (ver DoD-03).
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `GestionarReprogramacionesServiceTest.aprobar_liberaFranjaAntiguaYActualizaLaCitaConLaNueva` | Verifica que la franja antigua queda libre y la cita apunta al nuevo horario |
| CA-02 | Cumple | `GestionarReprogramacionesServiceTest.rechazar_conMotivo_liberaSoloLaFranjaNuevaYDejaLaCitaOriginalIntacta` | Verifica explícitamente que la franja antigua sigue ocupada y la nueva quedó libre |
| CA-03 | Cumple | `GestionarReprogramacionesServiceTest.rechazar_sinMotivo_seRechaza` | — |
| DoD-02 | Cumple | `SolicitudReprogramacion.aprobar()`/`rechazar()` (columnas `decided_by_user_id`/`decided_at`/`decision_reason`) | Mismo razonamiento que HU-019 DoD-02: no hay entrada en `appointment_status_history` porque el `EstadoCita` de la cita no cambia en este flujo (RN-10) |
| DoD-03 | Cumple | `GestionarReprogramacionesServiceTest.aprobar_concurrentementeLaMismaSolicitud_soloUnaAprobacionTieneExito` (10 hilos, latencia simulada en la lectura de la solicitud) | **Red→Green demostrado el 2026-10-02**: sin el bloqueo la prueba falló (varias aprobaciones simultáneas tenían éxito, un bug real); con `conBloqueoDeEscritura` pasa (1 éxito, 9 `TransicionEstadoInvalidaException`). Verificado además con 10 peticiones HTTP simultáneas contra MySQL 8.4 real (2026-10-02, base temporal): 10 aprobaciones simultáneas -> 1 éxito (200) y 9 conflictos (409); 5 aprobar + 5 rechazar -> 1 sola decisión. |
| RF-18 | Cumple | `GestionarReprogramacionesServiceTest.listarPendientes_aplicaFiltrosOpcionales`; `AdminReschedulesController` (`@RequestParam` sedeId/profesionalId/especialidadId/fecha) | La bandeja de reprogramaciones ya se filtra igual que la de citas especializadas (HU-016 CA-04). Verificado también contra MySQL real (2026-10-02) con una solicitud PENDING: los filtros por sede, profesional, especialidad y fecha devuelven lo esperado. |
| DoD-01 | Cumple | `mvn test`: 161/161, `BUILD SUCCESS` (2026-10-02); stack levantado contra MySQL 8.4 real con Docker en otro equipo (2026-09-30) y con MySQL nativo en este equipo (2026-10-02) | Ya no queda pendiente la verificación contra MySQL real. |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`).
- 2026-09-29 — Backend implementado y validado (`GET/POST /api/admin/reschedules/**`). Estado → `En desarrollo`. Queda pendiente (no bloqueante) la prueba de concurrencia de DoD-03 si se quiere elevar el rigor al nivel de RN-01.
- 2026-09-29 — UI integrada (`BandejaReprogramacionesScreen.tsx`/`RechazarReprogramacionModal.tsx`) y verificada con Playwright.
- 2026-09-29 — Ejecución de `LOOP_02_GUIADO_AVANZADO.md` (detalle completo en [[HU-019-solicitar-reprogramacion]]): confirmó que "solo ADMIN decide" se cumple, y corrigió que el paciente no podía ver el estado/motivo de la decisión del ADMIN — la afirmación de `RechazarReprogramacionModal.tsx` ("el motivo queda visible para el paciente") era falsa hasta esta corrección.

## Notas y decisiones

- Ver [[HU-019-solicitar-reprogramacion]] para el detalle de la ejecución de LOOP_02.
- 2026-10-02 — Cierre de brechas del checklist PRD↔HU↔código: prueba de concurrencia de doble-aprobación (encontró y corrigió un bug real) y filtros de la bandeja (RF-18). `mvn test`: 161/161.
