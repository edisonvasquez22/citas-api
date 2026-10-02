---
id: HU-021
tipo: historia-de-usuario
titulo: "Consultar agenda propia del profesional"
estado: Terminada
epica: "[[EP-009-agenda-profesional-y-cierre]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-014-solicitar-cita-general]]"
  - "[[HU-016-aprobar-rechazar-cita-especializada]]"
relacionadas:
  - "[[HU-022-marcar-cierre-de-atencion]]"
---

# HU-021 — Consultar agenda propia del profesional

## Historia de usuario

**COMO** PROFESSIONAL activo
**QUIERO** consultar mis citas `APPROVED` por día, semana y sede
**PARA** saber a quién debo atender sin ver información de otros profesionales

> Como PROFESSIONAL activo, quiero consultar mi agenda de citas aprobadas para saber a quién debo atender.

## Contexto y descripción

RF-16. El profesional no debe poder ver datos de usuarios fuera de sus propias citas.

## Alcance

- `GET /api/professionals/me/agenda` con filtro por día/semana y sede, mostrando solo citas `APPROVED` propias.

## Fuera de alcance

- Cambiar el estado de la cita (ver [[HU-022-marcar-cierre-de-atencion]]).

## Reglas de negocio

- Ownership estricto: un profesional solo ve sus propias citas `APPROVED` (RF-16).

## Dependencias y relaciones

- Épica: [[EP-009-agenda-profesional-y-cierre]]
- Dependencias: [[HU-014-solicitar-cita-general]], [[HU-016-aprobar-rechazar-cita-especializada]] (deben existir citas `APPROVED`).
- Relacionadas: [[HU-022-marcar-cierre-de-atencion]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** consulta de solo lectura con ownership y filtro de fecha/sede.

## Tareas de desarrollo

- [x] **T-01 — Caso de uso `ConsultarAgendaPropia`**
  Dificultad: Bajo
  Descripción: aplica ownership y filtro de día/semana/sede.
- [x] **T-02 — Endpoint REST**
  Dificultad: Bajo
  Descripción: autorización exclusiva de PROFESSIONAL sobre su propia agenda (`/api/professionals/me/**`, `hasRole("PROFESSIONAL")`).
- [x] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: agenda propia con filtro. El "acceso a agenda ajena" se cubre por diseño del endpoint, no por prueba de negocio — ver CA-02.

## Criterios de aceptación

### CA-01 — Agenda propia filtrada

**Dado** un profesional con citas `APPROVED` en distintas sedes/fechas
**Cuando** filtra por día/semana y sede
**Entonces** ve únicamente sus propias citas `APPROVED` que cumplen el filtro.

### CA-02 — Sin acceso a agenda ajena

**Dado** un profesional autenticado
**Cuando** intenta consultar la agenda de otro profesional
**Entonces** el sistema lo rechaza por falta de autorización.

## Definition of Done

- [x] CA-01 y CA-02 validados con evidencia.
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `ConsultarAgendaPropiaServiceTest.listar_devuelveSoloAprobadasDelProfesionalPropio`, `.listar_filtraPorSede` | — |
| CA-02 | Cumple | `AgendaProfesionalController` (sin `@PathVariable`, el profesional sale de `Authentication`) + `S3AuthorizationIntegrationTest.agendaProfesional_conTokenUser_devuelve403` | El endpoint no tiene forma de pedir la agenda de otro profesional (no recibe ningún id de profesional en la request) — garantía más fuerte que un chequeo de ownership post-hoc |
| DoD-01 | Cumple | `mvn test`: 161/161, `BUILD SUCCESS` (2026-10-02); UI real en `citas-web` (`AgendaProfesionalScreen.tsx`) verificada con Playwright (2026-09-29); stack levantado contra MySQL 8.4 real con Docker en otro equipo (2026-09-30) y con MySQL nativo en este equipo (2026-10-02) | Ya no queda nada pendiente de verificación. |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`).
- 2026-09-29 — Backend implementado y validado (`GET /api/professionals/me/agenda`). Estado → `En desarrollo`.

## Notas y decisiones

- Ninguna.
