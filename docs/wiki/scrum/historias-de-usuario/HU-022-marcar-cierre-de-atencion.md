---
id: HU-022
tipo: historia-de-usuario
titulo: "Marcar cita como completada o no asistida"
estado: Borrador
epica: "[[EP-009-agenda-profesional-y-cierre]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-021-consultar-agenda-profesional]]"
relacionadas:
  - "[[HU-023-historial-de-estados-de-cita]]"
---

# HU-022 — Marcar cita como completada o no asistida

## Historia de usuario

**COMO** PROFESSIONAL activo
**QUIERO** marcar una cita pasada/aplicable como completada o no asistida
**PARA** cerrar el ciclo de atención y dejar el estado real registrado

> Como PROFESSIONAL activo, quiero marcar el resultado real de una cita para cerrar su ciclo de atención.

## Contexto y descripción

RF-17. Debe registrarse historial del cambio (RF-19).

## Alcance

- `POST /api/appointments/{id}/complete`: marca `COMPLETED`.
- `POST /api/appointments/{id}/no-show`: marca `NO_SHOW`.

## Fuera de alcance

- Modificar cualquier otro dato de la cita.

## Reglas de negocio

- Ownership: solo el profesional de esa cita puede cerrarla.
- Solo aplica a una cita `APPROVED` pasada/aplicable (a definir el corte exacto de "aplicable" en implementación, p. ej. hora de fin ya transcurrida).

## Dependencias y relaciones

- Épica: [[EP-009-agenda-profesional-y-cierre]]
- Dependencias: [[HU-021-consultar-agenda-profesional]]
- Relacionadas: [[HU-023-historial-de-estados-de-cita]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** transición de estado terminal simple, con ownership y registro de auditoría.

## Tareas de desarrollo

- [ ] **T-01 — Casos de uso `MarcarCompletada`/`MarcarNoShow`**
  Dificultad: Bajo
  Descripción: valida ownership y que la cita esté `APPROVED` y sea pasada/aplicable.
- [ ] **T-02 — Endpoints REST**
  Dificultad: Bajo
  Descripción: autorización exclusiva del profesional dueño de la cita.
- [ ] **T-03 — Registro de auditoría**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]].
- [ ] **T-04 — Pruebas**
  Dificultad: Bajo
  Descripción: marcar completada, marcar no-show, intento sobre cita de otro profesional, intento sobre cita no `APPROVED`.

## Criterios de aceptación

### CA-01 — Marcar completada

**Dado** una cita `APPROVED` propia, pasada/aplicable
**Cuando** el profesional la marca como completada
**Entonces** pasa a `COMPLETED` y queda registrada en el historial de estados.

### CA-02 — Marcar no asistida

**Dado** una cita `APPROVED` propia, pasada/aplicable
**Cuando** el profesional la marca como no asistida
**Entonces** pasa a `NO_SHOW` y queda registrada en el historial de estados.

### CA-03 — Sin ownership

**Dado** una cita que no pertenece al profesional autenticado
**Cuando** intenta cerrarla
**Entonces** el sistema rechaza la operación.

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

- Definir en implementación el criterio exacto de "pasada/aplicable" (p. ej. hora de fin del slot ya transcurrida).
