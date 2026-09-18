---
id: EP-002
tipo: epica
titulo: "Perfil y afiliación de usuario"
estado: Borrador
historias:
  - "[[HU-004-consultar-y-actualizar-perfil]]"
  - "[[HU-005-asociar-afiliacion]]"
dependencias:
  - "[[EP-001-autenticacion-y-cuentas]]"
  - "[[EP-003-catalogos-del-sistema]]"
---

# EP-002 — Perfil y afiliación de usuario

## Objetivo

Permitir que `USER` consulte/actualice sus datos de perfil y asocie su afiliación (EPS, plan, régimen).

## Valor esperado

Mantiene actualizada la identidad del usuario y su afiliación, requisito de negocio explícito de RF-04.

## Actores

- USER.

## Alcance

- Consultar y actualizar los datos permitidos del perfil propio.
- Asociar una afiliación EPS/plan/régimen evitando duplicados dentro del mismo usuario.

## Fuera de alcance

- Historia clínica o validación real ante una EPS (fuera de alcance del PRD completo).

## Reglas de negocio

- La aplicación debe evitar duplicar EPS, régimen y plan dentro de un mismo usuario.

## Dependencias

- [[EP-001-autenticacion-y-cuentas]] (requiere sesión autenticada).
- [[EP-003-catalogos-del-sistema]] (catálogos de EPS, planes y régimen deben existir).

## Historias de usuario

- [[HU-004-consultar-y-actualizar-perfil]]
- [[HU-005-asociar-afiliacion]]

## Criterio de completitud de la épica

- [ ] HU-004 y HU-005 están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- No identificado; PRD no define reglas clínicas ligadas a la afiliación.
