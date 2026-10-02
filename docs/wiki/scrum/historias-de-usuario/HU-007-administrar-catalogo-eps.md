---
id: HU-007
tipo: historia-de-usuario
titulo: "Administrar catálogo de EPS"
estado: Terminada
epica: "[[EP-003-catalogos-del-sistema]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-006-precarga-de-catalogos-fijos]]"
relacionadas:
  - "[[HU-008-administrar-catalogo-planes-eps]]"
---

# HU-007 — Administrar catálogo de EPS

## Historia de usuario

**COMO** ADMIN
**QUIERO** crear, editar, activar y desactivar EPS
**PARA** mantener actualizado el catálogo que usan los usuarios al afiliarse

> Como ADMIN, quiero administrar el catálogo de EPS para mantenerlo actualizado.

## Contexto y descripción

RF-06. No se permite borrado físico de una EPS referenciada por afiliaciones; se usa activación/desactivación.

## Alcance

- CRUD de EPS (crear, editar, activar/desactivar). Sin borrado físico si hay afiliaciones asociadas.

## Fuera de alcance

- Planes de la EPS (ver [[HU-008-administrar-catalogo-planes-eps]]).

## Reglas de negocio

- No se permite borrar físicamente una EPS referenciada por afiliaciones existentes; solo desactivar.

## Dependencias y relaciones

- Épica: [[EP-003-catalogos-del-sistema]]
- Dependencias: [[HU-006-precarga-de-catalogos-fijos]] (rol ADMIN debe existir para autorizar este CRUD).
- Relacionadas: [[HU-008-administrar-catalogo-planes-eps]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** CRUD de catálogo simple con una única regla de integridad (no borrar si está referenciado).

## Tareas de desarrollo

- [x] **T-01 — Casos de uso CRUD de EPS**
  Dificultad: Bajo
  Descripción: crear/editar/activar/desactivar, con verificación de referencias antes de permitir desactivar si aplica alguna regla adicional.
- [x] **T-02 — Endpoints REST + migración Flyway**
  Dificultad: Bajo
  Descripción: `eps` ya existía en `V1__esquema_inicial.sql` — solo faltaba el seed demo (`V3`, ver [[decisiones]]).
  `/api/admin/eps` (CRUD) + `/api/eps` (lectura pública del catálogo activo).
- [x] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: creación, edición, desactivación, código duplicado.

## Criterios de aceptación

### CA-01 — CRUD básico

**Dado** un ADMIN autenticado
**Cuando** crea, edita o desactiva una EPS
**Entonces** el catálogo refleja el cambio inmediatamente para el resto del sistema.

### CA-02 — Sin borrado físico si está referenciada

**Dado** una EPS con al menos una afiliación de usuario asociada
**Cuando** se intenta eliminarla físicamente
**Entonces** el sistema lo rechaza y solo permite desactivarla.

## Definition of Done

- [x] CA-01 y CA-02 validados con evidencia.
- [x] Migración Flyway coherente con el diseño 3FN aprobado (tabla ya existía en V1; `V3` solo agrega el seed demo).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AdministrarEpsServiceTest.crear_conCodigoNuevo_quedaEnElCatalogo`, `cambiarEstado_desactivaLaEps` | Mismo patrón que HU-009 (especialidades). |
| CA-02 | Cumple | `AdminEpsController` no expone ningún `DELETE` | Igual que el catálogo de especialidades: el borrado físico es estructuralmente imposible vía la API (solo crear/editar/activar/desactivar), no hace falta una guarda activa. |
| DoD-01 | Cumple | `mvn test`: 133/133 BUILD SUCCESS | — |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-30 — Implementada: mismo patrón exacto que HU-009 (Especialidad). `eps` ya existía en el esquema;
  `V3__seed_catalogo_eps.sql` agrega el seed demo que faltaba. `/api/admin/eps` (ADMIN) + `/api/eps` (lectura
  pública del catálogo activo, usada por HU-005). Pasa de `Borrador` a `En desarrollo`. `mvn test`: 133/133.

## Notas y decisiones

- Ninguna.
