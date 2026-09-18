---
id: HU-008
tipo: historia-de-usuario
titulo: "Administrar catálogo de planes de EPS"
estado: Borrador
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

- [ ] **T-01 — Casos de uso CRUD de planes**
  Dificultad: Bajo
  Descripción: validan que la EPS referenciada exista y esté activa al crear/editar.
- [ ] **T-02 — Endpoints REST + migración Flyway**
  Dificultad: Bajo
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: creación válida, EPS inexistente/inactiva, desactivación, intento de borrado físico referenciado.

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

- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] Migración Flyway coherente con el diseño 3FN aprobado.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| DoD-01 | Pendiente | — | — |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.

## Notas y decisiones

- Ninguna.
