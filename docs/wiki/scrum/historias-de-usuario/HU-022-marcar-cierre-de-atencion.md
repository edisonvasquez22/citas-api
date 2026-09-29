---
id: HU-022
tipo: historia-de-usuario
titulo: "Marcar cita como completada o no asistida"
estado: "En desarrollo"
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

- [x] **T-01 — Casos de uso `MarcarCompletada`/`MarcarNoShow`**
  Dificultad: Bajo
  Descripción: valida ownership y que la cita esté `APPROVED` y sea pasada/aplicable. Criterio de "aplicable" implementado tal como sugería la nota: `fin` del slot ya transcurrido (`Cita.requerirCerrable`).
- [x] **T-02 — Endpoints REST**
  Dificultad: Bajo
  Descripción: autorización exclusiva del profesional dueño de la cita (`POST /api/appointments/{id}/complete`, `.../no-show`).
- [x] **T-03 — Registro de auditoría**
  Dificultad: Bajo
  Descripción: integra con [[HU-023-historial-de-estados-de-cita]] vía `HistorialEstadoCitaPort`.
- [x] **T-04 — Pruebas**
  Dificultad: Bajo
  Descripción: marcar completada, marcar no-show, intento sobre cita de otro profesional, intento sobre cita futura (no aplicable todavía).

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

- [x] CA-01 a CA-03 validados con evidencia.
- [x] Transición de estado registrada en auditoría (RF-19).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `CerrarAtencionServiceTest.completar_citaPropiaAprobadaYPasada_pasaACompleted` | — |
| CA-02 | Cumple | `CerrarAtencionServiceTest.marcarNoShow_citaPropiaAprobadaYPasada_pasaANoShow` | — |
| CA-03 | Cumple | `CerrarAtencionServiceTest.completar_citaDeOtroProfesional_seRechazaComoNoEncontrada` | 404 en vez de 403, mismo patrón de ownership ya usado en HU-018/HU-021 |
| DoD-01 | Cumple | `mvn test`: 106/106, `BUILD SUCCESS` (2026-09-29) | Sin verificar aún contra MySQL real (Docker pendiente); sin UI en `citas-web` todavía |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`).
- 2026-09-29 — Backend implementado y validado (`POST /api/appointments/{id}/complete|no-show`). Estado → `En desarrollo`.

## Notas y decisiones

- Criterio de "pasada/aplicable" resuelto en implementación: hora de fin del slot ya transcurrida (`fin.isAfter(ahora)` rechaza el cierre si es futura).
