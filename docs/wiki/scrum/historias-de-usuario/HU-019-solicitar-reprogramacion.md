---
id: HU-019
tipo: historia-de-usuario
titulo: "Solicitar reprogramación de cita"
estado: Borrador
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

- [ ] **T-01 — Caso de uso `SolicitarReprogramacion`**
  Dificultad: Alto
  Descripción: valida estado `APPROVED`/futuro, mismo profesional/especialidad, retiene la nueva franja sin tocar la original.
- [ ] **T-02 — Endpoint REST + migración Flyway de solicitudes de reprogramación**
  Dificultad: Alto
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Pruebas**
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

- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración Flyway coherente con el diseño 3FN aprobado.
- [ ] Transición registrada en auditoría (RF-19).
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

- Buena candidata para el GOAL/loop cross-repo de S3 (`GOAL_02_GUIADO_AVANZADO.md`) por combinar reglas de dominio complejas con UI de confirmación.
