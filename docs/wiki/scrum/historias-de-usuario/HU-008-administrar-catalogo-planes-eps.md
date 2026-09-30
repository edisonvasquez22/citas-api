---
id: HU-008
tipo: historia-de-usuario
titulo: "Administrar catálogo de planes de EPS"
estado: En desarrollo
epica: "[[EP-003-catalogos-del-sistema]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-007-administrar-catalogo-eps]]"
relacionadas:
  - "[[HU-005-asociar-afiliacion]]"
---

# HU-008 — Administrar catálogo de planes de EPS

## Historia de usuario

**COMO** ADMIN
**QUIERO** crear, editar, activar y desactivar planes asociados a una EPS
**PARA** mantener actualizada la oferta de planes que los usuarios pueden elegir al afiliarse

> Como ADMIN, quiero administrar los planes de cada EPS para mantenerlos actualizados.

## Contexto y descripción

RF-06. Cada plan pertenece a exactamente una EPS.

## Alcance

- CRUD de planes de EPS (crear, editar, activar/desactivar), cada plan asociado a una EPS existente.

## Fuera de alcance

- Catálogo de EPS en sí ([[HU-007-administrar-catalogo-eps]]).

## Reglas de negocio

- No se permite borrar físicamente un plan referenciado por afiliaciones existentes; solo desactivar.
- Un plan pertenece a una única EPS.

## Dependencias y relaciones

- Épica: [[EP-003-catalogos-del-sistema]]
- Dependencias: [[HU-007-administrar-catalogo-eps]] (debe existir la EPS).
- Relacionadas: [[HU-005-asociar-afiliacion]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** CRUD de catálogo dependiente de otro catálogo (EPS), sin reglas de negocio adicionales.

## Tareas de desarrollo

- [x] **T-01 — Casos de uso CRUD de planes**
  Dificultad: Bajo
  Descripción: validan que la EPS referenciada exista al crear (ver nota en Notas y decisiones sobre "activa").
- [x] **T-02 — Endpoints REST + migración Flyway**
  Dificultad: Bajo
  Descripción: `eps_plans` ya existía en `V1__esquema_inicial.sql` — solo faltaba el seed demo (`V3`).
  `/api/admin/eps/{epsId}/plans` (CRUD) + `/api/eps/{epsId}/plans` (lectura pública de planes activos).
- [x] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: creación válida, EPS inexistente, código duplicado en la misma EPS, desactivación.

## Criterios de aceptación

### CA-01 — CRUD básico

**Dado** un ADMIN autenticado y una EPS existente
**Cuando** crea, edita o desactiva un plan de esa EPS
**Entonces** el catálogo de planes refleja el cambio inmediatamente.

### CA-02 — Sin borrado físico si está referenciado

**Dado** un plan con al menos una afiliación de usuario asociada
**Cuando** se intenta eliminarlo físicamente
**Entonces** el sistema lo rechaza y solo permite desactivarlo.

## Definition of Done

- [x] CA-01 y CA-02 validados con evidencia.
- [x] Migración Flyway coherente con el diseño 3FN aprobado (tabla ya existía en V1; `V3` solo agrega el seed demo).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AdministrarPlanesEpsServiceTest.crear_paraEpsExistente_quedaEnElCatalogo`, `crear_paraEpsInexistente_seRechaza` (404), `crear_conCodigoDuplicadoEnLaMismaEps_seRechaza`, `cambiarEstado_desactivaElPlan` | — |
| CA-02 | Cumple | `AdminPlanesEpsController` no expone ningún `DELETE` | Mismo razonamiento que HU-007/HU-009: el borrado físico es estructuralmente imposible vía la API. |
| DoD-01 | Cumple | `mvn test`: 133/133 BUILD SUCCESS | — |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-30 — Implementada junto con HU-007 (mismo commit). `eps_plans` ya existía en el esquema;
  `V3__seed_catalogo_eps.sql` agrega el seed demo. El régimen (`regimenId`) se asigna al crear el plan, tomado del
  catálogo fijo `insurance_regimes` (RF-05, precargado por HU-006) — no se expone un endpoint propio para listar
  regímenes porque ninguna HU lo pide; el ADMIN los conoce como constantes fijas (documentadas en contratos.md).
  Pasa de `Borrador` a `En desarrollo`. `mvn test`: 133/133.

## Notas y decisiones

- Ninguna.
