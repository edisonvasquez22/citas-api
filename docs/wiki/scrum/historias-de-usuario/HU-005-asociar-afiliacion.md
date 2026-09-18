---
id: HU-005
tipo: historia-de-usuario
titulo: "Asociar afiliación EPS/plan/régimen"
estado: Borrador
epica: "[[EP-002-perfil-y-afiliacion]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-004-consultar-y-actualizar-perfil]]"
  - "[[HU-007-administrar-catalogo-eps]]"
  - "[[HU-008-administrar-catalogo-planes-eps]]"
relacionadas: []
---

# HU-005 — Asociar afiliación EPS/plan/régimen

## Historia de usuario

**COMO** USER autenticado
**QUIERO** asociar mi EPS, plan y régimen a mi perfil
**PARA** dejar registrada mi afiliación dentro del sistema

> Como USER autenticado, quiero asociar mi EPS, plan y régimen para dejar registrada mi afiliación.

## Contexto y descripción

RF-04 (parte de afiliación). La aplicación debe evitar duplicar EPS/régimen/plan dentro del mismo usuario.

## Alcance

- `POST/PUT /api/users/me/afiliacion`: crea o reemplaza la afiliación activa del usuario (EPS + plan de esa EPS + régimen), validando que el plan pertenezca a la EPS seleccionada.

## Fuera de alcance

- Validación real contra un sistema externo de EPS (fuera de alcance del PRD completo).

## Reglas de negocio

- Un usuario no tiene más de una afiliación activa simultánea (evita duplicar EPS/régimen/plan).
- El plan seleccionado debe pertenecer a la EPS seleccionada y ambos deben estar activos.

## Dependencias y relaciones

- Épica: [[EP-002-perfil-y-afiliacion]]
- Dependencias: [[HU-004-consultar-y-actualizar-perfil]], [[HU-007-administrar-catalogo-eps]], [[HU-008-administrar-catalogo-planes-eps]] (catálogos deben existir).
- Relacionadas: ninguna adicional.

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** valida consistencia cruzada EPS↔plan↔régimen y reemplazo de afiliación única por usuario.

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `AsociarAfiliacion`**
  Dificultad: Medio
  Descripción: valida EPS/plan/régimen activos y consistentes; reemplaza afiliación previa si existe.
- [ ] **T-02 — Endpoint REST + migración Flyway de afiliación**
  Dificultad: Medio
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Pruebas**
  Dificultad: Medio
  Descripción: afiliación válida, plan que no pertenece a la EPS seleccionada, EPS/plan inactivo.

## Criterios de aceptación

### CA-01 — Afiliación válida

**Dado** una EPS activa con un plan activo que le pertenece y un régimen válido
**Cuando** el usuario asocia esa afiliación
**Entonces** queda registrada como su afiliación activa, reemplazando cualquier afiliación previa.

### CA-02 — Plan no perteneciente a la EPS

**Dado** un plan que no pertenece a la EPS indicada
**Cuando** el usuario intenta asociar esa combinación
**Entonces** el sistema rechaza la solicitud con un error de validación.

### CA-03 — EPS o plan inactivo

**Dado** una EPS o un plan desactivado
**Cuando** el usuario intenta asociarlo
**Entonces** el sistema rechaza la solicitud.

## Definition of Done

- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración Flyway coherente con el diseño 3FN aprobado.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| CA-03 | Pendiente | — | — |
| DoD-01 | Pendiente | — | — |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.

## Notas y decisiones

- Ninguna.
