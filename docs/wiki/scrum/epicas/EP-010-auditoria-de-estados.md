---
id: EP-010
tipo: epica
titulo: "Auditoría de estados"
estado: Borrador
historias:
  - "[[HU-023-historial-de-estados-de-cita]]"
dependencias: []
---

# EP-010 — Auditoría de estados

## Objetivo

Dejar trazabilidad verificable de cada cambio de estado de una cita.

## Valor esperado

Soporta RN-11/RN-12 y es evidencia obligatoria de la Definition of Done de varias historias (EP-006 a EP-009).

## Actores

- Sistema (registra la transición).
- ADMIN, USER, PROFESSIONAL (consultan el historial según ownership).

## Alcance

- Registrar por cada transición: cita, estado nuevo, actor (cuando exista), fuente (`SYSTEM`, `USER` o `ADMIN`), fecha/hora y motivo opcional (RF-19).
- Exponer el historial dentro de "mis citas" (EP-008), la bandeja administrativa (EP-007) y la agenda del profesional (EP-009).

## Fuera de alcance

- Editar o borrar un registro de auditoría existente: no es un CRUD normal (RN-12).

## Reglas de negocio

- RN-11 (transiciones explícitas y verificables), RN-12 (auditoría inmutable).

## Dependencias

- Se activa desde [[EP-006-cita-general]] en adelante, en la primera transición de estado real del sistema.

## Historias de usuario

- [[HU-023-historial-de-estados-de-cita]]

## Criterio de completitud de la épica

- [ ] HU-023 está `Completada`.
- [ ] Todas las épicas que producen transiciones de estado (EP-006 a EP-009) registran evidencia de auditoría en su propia DoD.

## Riesgos e incógnitas

- No identificado.
