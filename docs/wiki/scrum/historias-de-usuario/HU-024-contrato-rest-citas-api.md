---
id: HU-024
tipo: historia-de-usuario
titulo: "Diseñar y documentar el contrato REST de citas-api"
estado: Borrador
epica: "[[EP-011-contrato-rest]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 1 (continuo)"
dependencias: []
relacionadas: []
---

# HU-024 — Diseñar y documentar el contrato REST de citas-api

## Historia de usuario

**COMO** agente orquestador / equipo de ambos repos
**QUIERO** mantener documentado el contrato REST de `citas-api`
**PARA** que `citas-web` pueda integrarse sin adivinar el comportamiento del backend

> Como agente orquestador, quiero mantener documentado el contrato REST para que citas-web se integre sin adivinar el comportamiento del backend.

## Contexto y descripción

RF-20. Es una historia continua: se revisa y actualiza cada vez que otra HU agrega o cambia un endpoint. No es una entrega única.

## Alcance

- Mantener `citas-api/docs/wiki/llm-wiki/wiki/contratos.md` (o el artefacto OpenAPI generado por springdoc) sincronizado con los endpoints realmente implementados.
- Documentar DTOs de request/response y códigos de error de negocio por endpoint.

## Fuera de alcance

- Implementar un BFF o capa intermedia (prohibido por las restricciones técnicas).

## Reglas de negocio

- RF-20: REST directo entre `citas-web` y `citas-api`, sin Express/BFF.

## Dependencias y relaciones

- Épica: [[EP-011-contrato-rest]]
- Dependencias: ninguna propia; se alimenta de cada HU que expone un endpoint.
- Relacionadas: todas las HU que agregan/cambian un endpoint REST.

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** no es compleja en un momento dado, pero exige disciplina continua de sincronización a lo largo de todo el proyecto.

## Tareas de desarrollo

- [ ] **T-01 — Integrar springdoc-openapi (2.8.17) al proyecto Spring Boot**
  Dificultad: Bajo
  Descripción: expone `/v3/api-docs` y Swagger UI para verificación manual del contrato.
- [ ] **T-02 — Actualizar `llm-wiki/wiki/contratos.md` en cada sprint**
  Dificultad: Bajo
  Descripción: reflejar endpoints nuevos/cambiados de las HU cerradas en ese sprint.
- [ ] **T-03 — Revisión cross-repo antes de cada incremento entregado a `citas-web`**
  Dificultad: Medio
  Descripción: confirmar que el frontend consume exactamente los contratos documentados.

## Criterios de aceptación

### CA-01 — Contrato sincronizado

**Dado** un endpoint nuevo o modificado en `citas-api`
**Cuando** su HU se marca `Completada`
**Entonces** el contrato documentado refleja ese endpoint (método, path, request, response, errores de negocio) en la misma sesión de trabajo.

### CA-02 — Sin discrepancias detectadas

**Dado** el estado actual de `citas-api`
**Cuando** se compara contra `llm-wiki/wiki/contratos.md` (o el OpenAPI generado)
**Entonces** no existen endpoints implementados sin documentar ni documentados sin implementar.

## Definition of Done

- [ ] CA-01 y CA-02 validados con evidencia en cada sprint donde se cierre esta revisión.
- [ ] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple (parcial) | `docs/wiki/llm-wiki/wiki/contratos.md`, actualizado en la misma sesión que HU-001/HU-002 | Cumple para los 4 endpoints de auth implementados hasta ahora; se revalida en cada sprint siguiente. |
| CA-02 | Cumple (parcial) | Comparación manual `AuthController` ↔ `contratos.md` | Sin discrepancias en los endpoints existentes; no hay todavía OpenAPI exportado como artefacto versionado. |
| DoD-01 | No cumple | — | Esta HU no está formalmente `Aprobada` (no fue parte del alcance S2 confirmado explícitamente); el trabajo de sincronización se hizo como efecto colateral de implementar HU-001/HU-002. |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`, con sprint sugerido continuo desde el Sprint 1.
- 2026-09-17 — `docs/wiki/llm-wiki/wiki/contratos.md` actualizado con los 4 endpoints de `/api/auth/**` como efecto colateral de implementar HU-001/HU-002. HU sigue en `Borrador`: no fue aprobada explícitamente por el usuario como parte del alcance S2.

## Notas y decisiones

- A diferencia del resto del backlog, esta HU no se cierra una sola vez: se revalida en cada sprint que agregue o cambie contrato.
