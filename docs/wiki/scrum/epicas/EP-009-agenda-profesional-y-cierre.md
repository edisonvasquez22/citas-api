---
id: EP-009
tipo: epica
titulo: "Agenda del profesional y cierre de atención"
estado: Borrador
historias:
  - "[[HU-021-consultar-agenda-profesional]]"
  - "[[HU-022-marcar-cierre-de-atencion]]"
dependencias:
  - "[[EP-006-cita-general]]"
  - "[[EP-007-cita-especializada-y-aprobacion]]"
  - "[[EP-010-auditoria-de-estados]]"
---

# EP-009 — Agenda del profesional y cierre de atención

## Objetivo

Que `PROFESSIONAL` consulte su agenda de citas aprobadas y cierre la atención marcando el resultado real de cada cita.

## Valor esperado

Permite operar el día a día del profesional y alimenta reportes/automatizaciones futuras de S5-S6 (RF-16, RF-17).

## Actores

- PROFESSIONAL.

## Alcance

- Consultar citas `APPROVED` propias por día/semana y sede, sin ver datos de usuarios fuera de sus propias citas.
- Marcar una cita pasada/aplicable como `COMPLETED` o `NO_SHOW`.

## Fuera de alcance

- Modificar la cita más allá del cambio de estado de cierre.

## Reglas de negocio

- Ownership estricto: el profesional solo ve sus propias citas (RF-16).
- El cierre de atención se registra en el historial de auditoría (RF-17).

## Dependencias

- [[EP-006-cita-general]] y [[EP-007-cita-especializada-y-aprobacion]] (debe existir una cita `APPROVED`).
- [[EP-010-auditoria-de-estados]].

## Historias de usuario

- [[HU-021-consultar-agenda-profesional]]
- [[HU-022-marcar-cierre-de-atencion]]

## Criterio de completitud de la épica

- [ ] HU-021 y HU-022 están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- No identificado.
