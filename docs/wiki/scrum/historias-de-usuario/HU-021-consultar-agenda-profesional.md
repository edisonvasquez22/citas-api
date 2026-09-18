---
id: HU-021
tipo: historia-de-usuario
titulo: "Consultar agenda propia del profesional"
estado: Borrador
epica: "[[EP-009-agenda-profesional-y-cierre]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-014-solicitar-cita-general]]"
  - "[[HU-016-aprobar-rechazar-cita-especializada]]"
relacionadas:
  - "[[HU-022-marcar-cierre-de-atencion]]"
---

# HU-021 — Consultar agenda propia del profesional

## Historia de usuario

**COMO** PROFESSIONAL activo
**QUIERO** consultar mis citas `APPROVED` por día, semana y sede
**PARA** saber a quién debo atender sin ver información de otros profesionales

> Como PROFESSIONAL activo, quiero consultar mi agenda de citas aprobadas para saber a quién debo atender.

## Contexto y descripción

RF-16. El profesional no debe poder ver datos de usuarios fuera de sus propias citas.

## Alcance

- `GET /api/professionals/me/agenda` con filtro por día/semana y sede, mostrando solo citas `APPROVED` propias.

## Fuera de alcance

- Cambiar el estado de la cita (ver [[HU-022-marcar-cierre-de-atencion]]).

## Reglas de negocio

- Ownership estricto: un profesional solo ve sus propias citas `APPROVED` (RF-16).

## Dependencias y relaciones

- Épica: [[EP-009-agenda-profesional-y-cierre]]
- Dependencias: [[HU-014-solicitar-cita-general]], [[HU-016-aprobar-rechazar-cita-especializada]] (deben existir citas `APPROVED`).
- Relacionadas: [[HU-022-marcar-cierre-de-atencion]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** consulta de solo lectura con ownership y filtro de fecha/sede.

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `ConsultarAgendaPropia`**
  Dificultad: Bajo
  Descripción: aplica ownership y filtro de día/semana/sede.
- [ ] **T-02 — Endpoint REST**
  Dificultad: Bajo
  Descripción: autorización exclusiva de PROFESSIONAL sobre su propia agenda.
- [ ] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: agenda propia con filtro, intento de acceso a agenda de otro profesional.

## Criterios de aceptación

### CA-01 — Agenda propia filtrada

**Dado** un profesional con citas `APPROVED` en distintas sedes/fechas
**Cuando** filtra por día/semana y sede
**Entonces** ve únicamente sus propias citas `APPROVED` que cumplen el filtro.

### CA-02 — Sin acceso a agenda ajena

**Dado** un profesional autenticado
**Cuando** intenta consultar la agenda de otro profesional
**Entonces** el sistema lo rechaza por falta de autorización.

## Definition of Done

- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| DoD-01 | Pendiente | — | — |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.

## Notas y decisiones

- Ninguna.
