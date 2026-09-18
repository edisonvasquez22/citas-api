---
id: EP-008
tipo: epica
titulo: "Mis citas, cancelación y reprogramación"
estado: Borrador
historias:
  - "[[HU-017-consultar-mis-citas]]"
  - "[[HU-018-cancelar-cita]]"
  - "[[HU-019-solicitar-reprogramacion]]"
  - "[[HU-020-aprobar-rechazar-reprogramacion]]"
dependencias:
  - "[[EP-006-cita-general]]"
  - "[[EP-007-cita-especializada-y-aprobacion]]"
  - "[[EP-010-auditoria-de-estados]]"
---

# EP-008 — Mis citas, cancelación y reprogramación

## Objetivo

Que `USER` gestione el ciclo de vida de sus propias citas después de creadas: consultarlas, cancelarlas o solicitar su reprogramación.

## Valor esperado

Da autonomía al paciente sobre citas ya reservadas (RF-13, RF-14, RF-15).

## Actores

- USER.
- ADMIN (aprueba/rechaza reprogramaciones).

## Alcance

- Listar/filtrar "mis citas" por estado/fecha, mostrando sede, profesional, especialidad, fecha/hora, duración, estado y motivo de rechazo cuando exista.
- Cancelar una cita futura no terminal (libera slots, no se reactiva directamente).
- Solicitar reprogramación de una cita `APPROVED` futura: conserva profesional y especialidad, retiene la nueva franja mientras está `PENDING`, conserva la cita original hasta la decisión de ADMIN.
- ADMIN aprueba (libera slots antiguos, asigna los nuevos) o rechaza con motivo (libera la reserva provisional, mantiene la cita original); tras el rechazo, USER puede conservar o cancelar la cita.

## Fuera de alcance

- Cambiar de profesional dentro de una reprogramación (se trata como una cita nueva).

## Reglas de negocio

- RN-09 (liberar reservas al cancelar/rechazar), RN-10 (no destruir la cita original hasta aprobar la reprogramación), RN-01.

## Dependencias

- [[EP-006-cita-general]] y [[EP-007-cita-especializada-y-aprobacion]] (debe existir una cita).
- [[EP-010-auditoria-de-estados]].

## Historias de usuario

- [[HU-017-consultar-mis-citas]]
- [[HU-018-cancelar-cita]]
- [[HU-019-solicitar-reprogramacion]]
- [[HU-020-aprobar-rechazar-reprogramacion]]

## Criterio de completitud de la épica

- [ ] Las cuatro HU están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- Atomicidad de la reprogramación (liberar antigua + asignar nueva) bajo concurrencia; ver `llm-wiki/wiki/riesgos.md`.
