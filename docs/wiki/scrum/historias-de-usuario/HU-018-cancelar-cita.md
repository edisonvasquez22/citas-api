---
id: HU-018
tipo: historia-de-usuario
titulo: "Cancelar cita"
estado: Borrador
epica: "[[EP-008-mis-citas-cancelacion-reprogramacion]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-017-consultar-mis-citas]]"
relacionadas:
  - "[[HU-023-historial-de-estados-de-cita]]"
---

# HU-018 — Cancelar cita

## Historia de usuario

**COMO** USER autenticado
**QUIERO** cancelar una cita futura no terminal
**PARA** liberar el horario cuando ya no la necesito

> Como USER autenticado, quiero cancelar una cita futura para liberar el horario cuando ya no la necesito.

## Contexto y descripción

RF-14. Una cita cancelada no se reactiva directamente y debe quedar historial del cambio.

## Alcance

- `POST /api/appointments/{id}/cancel`: cancela una cita propia, futura y no terminal.

## Fuera de alcance

- Reactivar una cita cancelada (no permitido; el usuario debe crear una nueva).

## Reglas de negocio

- Solo citas futuras y no terminales (`APPROVED`/`REQUESTED`) son cancelables.
- `CANCELLED` libera los slots asociados (RN-09).
- Una cita cancelada no se reactiva directamente.

## Dependencias y relaciones

- Épica: [[EP-008-mis-citas-cancelacion-reprogramacion]]
- Dependencias: [[HU-017-consultar-mis-citas]]
- Relacionadas: [[HU-023-historial-de-estados-de-cita]]

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** transición de estado con liberación de slots y ownership, más el registro de auditoría.

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `CancelarCita`**
  Dificultad: Medio
  Descripción: valida ownership, que la cita sea futura y no terminal, libera slots.
- [ ] **T-02 — Endpoint REST**
  Dificultad: Bajo
  Descripción: autorización por ownership.
- [ ] **T-03 — Registro de auditoría**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [ ] **T-04 — Pruebas**
  Dificultad: Medio
  Descripción: cancelación válida, cita ya terminal (`COMPLETED`/`CANCELLED`/`NO_SHOW`), cita de otro usuario, cita pasada.

## Criterios de aceptación

### CA-01 — Cancelación válida

**Dado** una cita propia, futura y no terminal
**Cuando** el usuario la cancela
**Entonces** pasa a `CANCELLED` y sus slots quedan liberados.

### CA-02 — Cita ya terminal

**Dado** una cita ya `COMPLETED`, `CANCELLED` o `NO_SHOW`
**Cuando** el usuario intenta cancelarla
**Entonces** el sistema rechaza la operación.

### CA-03 — Cita de otro usuario

**Dado** una cita que no pertenece al usuario autenticado
**Cuando** intenta cancelarla
**Entonces** el sistema rechaza la operación por falta de autorización.

## Definition of Done

- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Transición de estado registrada en auditoría (RF-19).
- [ ] `mvn test` pasa para los módulos afectados.
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
