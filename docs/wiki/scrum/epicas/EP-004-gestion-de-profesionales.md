---
id: EP-004
tipo: epica
titulo: "Gestión de profesionales"
estado: Borrador
historias:
  - "[[HU-010-registrar-profesional]]"
  - "[[HU-011-activar-desactivar-profesional]]"
dependencias:
  - "[[EP-001-autenticacion-y-cuentas]]"
  - "[[EP-003-catalogos-del-sistema]]"
---

# EP-004 — Gestión de profesionales

## Objetivo

Permitir que ADMIN cree y administre profesionales ficticios con sus especialidades y sedes asignadas.

## Valor esperado

Sin profesionales activos y habilitados en una sede/especialidad, no puede existir agenda ni citas (RF-07).

## Actores

- ADMIN.
- PROFESSIONAL (resultado del alta).

## Alcance

- Crear el usuario `PROFESSIONAL`, registrar código profesional y matrícula ficticia.
- Asignar una o varias especialidades marcando una primaria.
- Asignar una o ambas sedes.
- Activar/desactivar al profesional.

## Fuera de alcance

- Autogestión de especialidades/sedes por el propio profesional (siempre las administra ADMIN).

## Reglas de negocio

- La especialidad primaria es única por profesional.
- Las sedes asignadas limitan dónde el profesional puede publicar agenda (RN-07).
- Nombres y matrículas son sintéticos.

## Dependencias

- [[EP-001-autenticacion-y-cuentas]] (el profesional necesita una cuenta).
- [[EP-003-catalogos-del-sistema]] (catálogos de especialidades y sedes).

## Historias de usuario

- [[HU-010-registrar-profesional]]
- [[HU-011-activar-desactivar-profesional]]

## Criterio de completitud de la épica

- [ ] HU-010 y HU-011 están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- No identificado.
