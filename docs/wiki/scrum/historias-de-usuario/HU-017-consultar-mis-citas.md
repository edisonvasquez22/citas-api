---
id: HU-017
tipo: historia-de-usuario
titulo: "Consultar mis citas"
estado: "En desarrollo"
epica: "[[EP-008-mis-citas-cancelacion-reprogramacion]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-014-solicitar-cita-general]]"
  - "[[HU-015-solicitar-cita-especializada]]"
relacionadas:
  - "[[HU-018-cancelar-cita]]"
  - "[[HU-019-solicitar-reprogramacion]]"
---

# HU-017 — Consultar mis citas

## Historia de usuario

**COMO** USER autenticado
**QUIERO** consultar y filtrar mis propias citas por estado y fecha
**PARA** saber qué tengo agendado y en qué situación está cada cita

> Como USER autenticado, quiero consultar mis citas para saber qué tengo agendado.

## Contexto y descripción

RF-13. Debe mostrar como mínimo sede, profesional, especialidad, fecha/hora, duración, estado y motivo de rechazo cuando exista.

## Alcance

- `GET /api/appointments/mine` con filtros de estado y fecha.

## Fuera de alcance

- Acciones sobre la cita (cancelar/reprogramar): [[HU-018-cancelar-cita]], [[HU-019-solicitar-reprogramacion]].

## Reglas de negocio

- Ownership estricto: un usuario solo ve sus propias citas.

## Dependencias y relaciones

- Épica: [[EP-008-mis-citas-cancelacion-reprogramacion]]
- Dependencias: [[HU-014-solicitar-cita-general]], [[HU-015-solicitar-cita-especializada]] (debe existir al menos una cita).
- Relacionadas: [[HU-018-cancelar-cita]], [[HU-019-solicitar-reprogramacion]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** consulta de solo lectura con ownership y filtros simples.

## Tareas de desarrollo

- [x] **T-01 — Caso de uso `ConsultarMisCitas`**
  Dificultad: Bajo
  Descripción: aplica ownership y filtros de estado/fecha.
- [x] **T-02 — Endpoint REST**
  Dificultad: Bajo
  Descripción: incluye motivo de rechazo cuando exista.
- [x] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: listado propio, filtro por estado, intento de ver citas de otro usuario (debe ser imposible por diseño del endpoint).

## Criterios de aceptación

### CA-01 — Listado propio con filtros

**Dado** un USER con varias citas en distintos estados
**Cuando** filtra por estado y/o fecha
**Entonces** recibe únicamente sus propias citas que cumplen el filtro, con sede, profesional, especialidad, fecha/hora, duración, estado y motivo de rechazo cuando exista.

## Definition of Done

- [x] CA-01 validado con evidencia.
- [x] `mvn test` pasa para los módulos afectados.
- [x] Contrato reflejado en [[HU-024-contrato-rest-citas-api]] (ver `docs/wiki/llm-wiki/wiki/contratos.md`).
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `ConsultarMisCitasServiceTest` (4 pruebas: propias, filtro estado, filtro fecha, motivo de rechazo) | El motivo de rechazo se resuelve desde `appointment_status_history` (HU-023), no se duplica en `appointments` |
| DoD-01 | Cumple | `mvn test`: 106/106, `BUILD SUCCESS` (2026-09-29) | Sin verificar aún contra MySQL real (Docker pendiente); sin UI en `citas-web` todavía |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`).
- 2026-09-29 — Backend implementado y validado (`GET /api/appointments/mine`). Estado → `En desarrollo`.

## Notas y decisiones

- Ninguna.
