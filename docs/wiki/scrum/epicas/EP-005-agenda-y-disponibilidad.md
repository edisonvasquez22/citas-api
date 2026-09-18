---
id: EP-005
tipo: epica
titulo: "Agenda y disponibilidad"
estado: Borrador
historias:
  - "[[HU-012-gestionar-bloques-de-disponibilidad]]"
  - "[[HU-013-consultar-disponibilidad]]"
dependencias:
  - "[[EP-004-gestion-de-profesionales]]"
  - "[[EP-003-catalogos-del-sistema]]"
---

# EP-005 — Agenda y disponibilidad

## Objetivo

Que el profesional publique bloques de disponibilidad y que el usuario pueda consultarlos filtrando por sede, tipo de cita, especialidad, profesional y fecha.

## Valor esperado

Es el núcleo operativo que hace posible el agendamiento (RF-08, RF-09, RF-10).

## Actores

- PROFESSIONAL.
- USER.

## Alcance

- CRUD de bloques de disponibilidad: crear múltiples bloques por día, elegir sede por bloque, editar/eliminar bloques futuros sin citas comprometidas, discretización en slots de 30 minutos.
- Consulta de disponibilidad con filtros (sede, tipo general/especializada, especialidad, profesional, fecha), mostrando solo horarios que puedan completar toda la duración requerida.

## Fuera de alcance

- La creación de la cita en sí (ver [[EP-006-cita-general]] y [[EP-007-cita-especializada-y-aprobacion]]).

## Reglas de negocio

- No crear bloques en el pasado (RN-06).
- No solapar bloques del mismo profesional.
- El profesional debe estar habilitado en la sede del bloque (RN-07).
- Slots consecutivos cuando la duración de la especialidad es de 60 minutos (RN-05).

## Dependencias

- [[EP-004-gestion-de-profesionales]] (profesional activo, con sede/especialidad asignada).
- [[EP-003-catalogos-del-sistema]] (duración de la especialidad, catálogo de sedes).

## Historias de usuario

- [[HU-012-gestionar-bloques-de-disponibilidad]]
- [[HU-013-consultar-disponibilidad]]

## Criterio de completitud de la épica

- [ ] HU-012 y HU-013 están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- Riesgo de doble reserva al momento de retener slots; se prueba explícitamente en S3 (ver `llm-wiki/wiki/riesgos.md`).
