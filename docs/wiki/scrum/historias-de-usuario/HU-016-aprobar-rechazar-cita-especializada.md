---
id: HU-016
tipo: historia-de-usuario
titulo: "Aprobar/rechazar solicitudes especializadas"
estado: "En desarrollo"
epica: "[[EP-007-cita-especializada-y-aprobacion]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-015-solicitar-cita-especializada]]"
relacionadas:
  - "[[HU-023-historial-de-estados-de-cita]]"
---

# HU-016 — Aprobar/rechazar solicitudes especializadas

## Historia de usuario

**COMO** ADMIN
**QUIERO** ver una bandeja de citas especializadas `REQUESTED` y aprobarlas o rechazarlas
**PARA** controlar el acceso a especialidades con recurso limitado

> Como ADMIN, quiero aprobar o rechazar solicitudes especializadas para controlar el acceso a especialidades limitadas.

## Contexto y descripción

RF-12 (decisión) + RF-18 (bandeja). El rechazo exige motivo y libera los slots retenidos.

## Alcance

- `GET /api/admin/appointments/requested`: bandeja con filtros por sede, profesional, especialidad y fecha.
- `POST /api/admin/appointments/{id}/approve`: pasa a `APPROVED`.
- `POST /api/admin/appointments/{id}/reject`: exige motivo, pasa a `REJECTED` y libera los slots.

## Fuera de alcance

- Bandeja de reprogramaciones `PENDING` (ver [[HU-020-aprobar-rechazar-reprogramacion]], puede compartir la misma pantalla en el frontend pero es una HU de backend distinta).

## Reglas de negocio

- RN-03 (requiere ADMIN), RN-04 (rechazo exige motivo), RN-09 (rechazo libera slots retenidos).

## Dependencias y relaciones

- Épica: [[EP-007-cita-especializada-y-aprobacion]]
- Dependencias: [[HU-015-solicitar-cita-especializada]]
- Relacionadas: [[HU-023-historial-de-estados-de-cita]]

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** dos transiciones de estado con reglas distintas (liberar slots solo en el rechazo) y una bandeja con filtros.

## Tareas de desarrollo

- [ ] **T-01 — Casos de uso `AprobarCitaEspecializada`/`RechazarCitaEspecializada`**
  Dificultad: Medio
  Descripción: valida estado previo `REQUESTED`, exige motivo en el rechazo, libera slots solo al rechazar.
- [ ] **T-02 — Endpoint de bandeja con filtros**
  Dificultad: Bajo
  Descripción: `GET` con combinación de filtros.
- [ ] **T-03 — Registro de auditoría de la transición**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [ ] **T-04 — Pruebas**
  Dificultad: Medio
  Descripción: aprobar solicitud `REQUESTED`, rechazar con motivo (libera slots), rechazar sin motivo (debe fallar), intentar decidir una solicitud que no está en `REQUESTED`.

## Criterios de aceptación

### CA-01 — Aprobación

**Dado** una solicitud en `REQUESTED`
**Cuando** ADMIN la aprueba
**Entonces** pasa a `APPROVED` y el horario queda confirmado para el profesional/especialidad/sede solicitados.

### CA-02 — Rechazo con motivo

**Dado** una solicitud en `REQUESTED`
**Cuando** ADMIN la rechaza indicando un motivo
**Entonces** pasa a `REJECTED`, se libera el horario retenido y el motivo queda visible para el usuario.

### CA-03 — Rechazo sin motivo

**Dado** un intento de rechazo sin motivo
**Cuando** ADMIN lo envía
**Entonces** el sistema rechaza la operación por validación.

### CA-04 — Bandeja filtrable

**Dado** varias solicitudes `REQUESTED` de distintas sedes/especialidades
**Cuando** ADMIN filtra la bandeja
**Entonces** solo ve las que cumplen los filtros aplicados.

## Definition of Done

- [x] CA-01 a CA-04 validados con evidencia.
- [x] Transición de estado registrada en auditoría (RF-19), incluyendo el motivo cuando aplica.
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `GestionarSolicitudesEspecializadasServiceTest.aprobar_solicitudRequested_pasaAApproved` | — |
| CA-02 | Cumple | `GestionarSolicitudesEspecializadasServiceTest.rechazar_conMotivo_pasaARejectedYLiberaElHorario` | El motivo se audita en `appointment_status_history.reason` (HU-023); `appointments` no tiene columna propia para él |
| CA-03 | Cumple | `GestionarSolicitudesEspecializadasServiceTest.rechazar_sinMotivo_seRechaza` | — |
| CA-04 | Cumple | `GestionarSolicitudesEspecializadasServiceTest.listarSolicitudes_filtraPorSedeYSoloDevuelveRequested` | — |
| DoD-01 | Cumple | `mvn test`: 76/76, `BUILD SUCCESS` (2026-09-25) | Sin verificar aún contra MySQL real (Docker pendiente) |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-25 — Aprobada explícitamente por el usuario como parte del alcance de S3; implementada y validada. Estado → `En desarrollo`.

## Notas y decisiones

- Ninguna.
