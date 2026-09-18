---
id: EP-006
tipo: epica
titulo: "Cita general"
estado: Borrador
historias:
  - "[[HU-014-solicitar-cita-general]]"
dependencias:
  - "[[EP-005-agenda-y-disponibilidad]]"
  - "[[EP-001-autenticacion-y-cuentas]]"
  - "[[EP-010-auditoria-de-estados]]"
---

# EP-006 — Cita general

## Objetivo

Que `USER` reserve una cita de Medicina General con aprobación automática, sin intervención de ADMIN.

## Valor esperado

Es el flujo de menor fricción del producto (RF-11); valida el circuito completo disponibilidad → reserva → estado antes de abordar el flujo especializado, más complejo.

## Actores

- USER.

## Alcance

- Seleccionar `Medicina General`, elegir uno de los profesionales generales disponibles y confirmar.
- Si el horario sigue disponible al confirmar, la cita se crea en `APPROVED` automáticamente.

## Fuera de alcance

- Especialidades distintas a Medicina General (ver [[EP-007-cita-especializada-y-aprobacion]]).

## Reglas de negocio

- RN-01 (no doble reserva), RN-02 (auto-aprobación), RN-05, RN-06.

## Dependencias

- [[EP-005-agenda-y-disponibilidad]]
- [[EP-001-autenticacion-y-cuentas]]
- [[EP-010-auditoria-de-estados]] (el alta se registra como transición de estado).

## Historias de usuario

- [[HU-014-solicitar-cita-general]]

## Criterio de completitud de la épica

- [ ] HU-014 está `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- Compartido con EP-005: riesgo de doble reserva en confirmación concurrente.
