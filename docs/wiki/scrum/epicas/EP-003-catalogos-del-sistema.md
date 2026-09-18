---
id: EP-003
tipo: epica
titulo: "Catálogos del sistema"
estado: Borrador
historias:
  - "[[HU-006-precarga-de-catalogos-fijos]]"
  - "[[HU-007-administrar-catalogo-eps]]"
  - "[[HU-008-administrar-catalogo-planes-eps]]"
  - "[[HU-009-administrar-catalogo-especialidades]]"
dependencias: []
---

# EP-003 — Catálogos del sistema

## Objetivo

Proveer los catálogos fijos (precargados) y configurables (CRUD por ADMIN) que sostienen el resto del dominio.

## Valor esperado

Es la base fundacional: sin catálogos de roles, estados, régimen, sedes, EPS, planes y especialidades ninguna otra épica puede completarse de forma verificable.

## Actores

- Sistema (catálogos fijos, vía seed/migración).
- ADMIN (catálogos configurables, vía CRUD).

## Alcance

- Precarga de catálogos fijos y de solo lectura: roles, estados de cita, estados de reprogramación, regímenes, sedes (RF-05).
- CRUD de EPS, planes de EPS y especialidades (RF-06), incluyendo la duración (30/60 min) de cada especialidad (RF-09).

## Fuera de alcance

- Borrado físico de un catálogo referenciado por transacciones (se usa activación/desactivación).

## Reglas de negocio

- No se permite borrar físicamente un catálogo referenciado por transacciones.
- Cada especialidad define su duración en 30 o 60 minutos (RF-09); el profesional no la sobrescribe.

## Dependencias

- Ninguna. Esta épica es fundacional y bloquea a casi todas las demás.

## Historias de usuario

- [[HU-006-precarga-de-catalogos-fijos]]
- [[HU-007-administrar-catalogo-eps]]
- [[HU-008-administrar-catalogo-planes-eps]]
- [[HU-009-administrar-catalogo-especialidades]]

## Criterio de completitud de la épica

- [ ] Las cuatro HU están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- No identificado.
