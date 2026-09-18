---
id: HU-020
tipo: historia-de-usuario
titulo: "Aprobar/rechazar reprogramación"
estado: Borrador
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

- [ ] **T-01 — Casos de uso `AprobarReprogramacion`/`RechazarReprogramacion`**
  Dificultad: Alto
  Descripción: aprobar libera slots antiguos y asigna los nuevos en la misma operación; rechazar libera solo la reserva provisional.
- [ ] **T-02 — Endpoint de bandeja + acciones**
  Dificultad: Medio
  Descripción: filtros por sede/profesional/especialidad/fecha, compartidos o no con HU-016 según decisión de implementación.
- [ ] **T-03 — Registro de auditoría de la transición**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [ ] **T-04 — Pruebas**
  Dificultad: Alto
  Descripción: aprobación exitosa (verifica liberación de slots antiguos y asignación de nuevos), rechazo con motivo (verifica que la cita original quedó intacta), rechazo sin motivo.

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

- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Transición de estado registrada en auditoría (RF-19), incluyendo el motivo cuando aplica.
- [ ] `mvn test` pasa para los módulos afectados, incluida una prueba que verifique la atomicidad de la aprobación.
- [ ] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| CA-03 | Pendiente | — | — |
| DoD-01 | Pendiente | — | — |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.

## Notas y decisiones

- Ninguna.
