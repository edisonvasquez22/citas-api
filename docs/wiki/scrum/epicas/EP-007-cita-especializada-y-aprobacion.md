---
id: EP-007
tipo: epica
titulo: "Cita especializada y aprobación administrativa"
estado: Borrador
historias:
  - "[[HU-015-solicitar-cita-especializada]]"
  - "[[HU-016-aprobar-rechazar-cita-especializada]]"
dependencias:
  - "[[EP-005-agenda-y-disponibilidad]]"
  - "[[EP-003-catalogos-del-sistema]]"
  - "[[EP-010-auditoria-de-estados]]"
---

# EP-007 — Cita especializada y aprobación administrativa

## Objetivo

Que `USER` solicite citas de especialidades distintas a Medicina General y que `ADMIN` las apruebe o rechace desde una bandeja administrativa.

## Valor esperado

Da control administrativo donde el recurso profesional es más limitado o sensible (RF-12, RF-18).

## Actores

- USER.
- ADMIN.

## Alcance

- Solicitud de cita especializada que nace en `REQUESTED`, con el horario retenido para evitar doble reserva.
- Bandeja administrativa con filtros por sede, profesional, especialidad y fecha (incluye también reprogramaciones `PENDING`, ver [[EP-008-mis-citas-cancelacion-reprogramacion]]).
- Aprobar (→ `APPROVED`) o rechazar con motivo obligatorio (→ `REJECTED`, libera los slots retenidos).

## Fuera de alcance

- Reprogramación de una cita ya aprobada (ver [[EP-008-mis-citas-cancelacion-reprogramacion]]).

## Reglas de negocio

- RN-01, RN-03 (requiere ADMIN), RN-04 (rechazo exige motivo), RN-08 (especialidad activa y asociada al profesional).

## Dependencias

- [[EP-005-agenda-y-disponibilidad]]
- [[EP-003-catalogos-del-sistema]] (especialidad activa)
- [[EP-010-auditoria-de-estados]]

## Historias de usuario

- [[HU-015-solicitar-cita-especializada]]
- [[HU-016-aprobar-rechazar-cita-especializada]]

## Criterio de completitud de la épica

- [ ] HU-015 y HU-016 están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- Ventana de retención del horario mientras la solicitud está `REQUESTED`: definir expiración o no (a resolver en `HU-015`).
