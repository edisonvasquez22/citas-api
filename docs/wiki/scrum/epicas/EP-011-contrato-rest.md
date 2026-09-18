---
id: EP-011
tipo: epica
titulo: "Contrato REST y documentación técnica"
estado: Borrador
historias:
  - "[[HU-024-contrato-rest-citas-api]]"
dependencias: []
---

# EP-011 — Contrato REST y documentación técnica

## Objetivo

Mantener un contrato REST documentado y estable entre `citas-api` y `citas-web`.

## Valor esperado

Evita el acoplamiento accidental entre repos y sostiene el trabajo cross-repo del agente orquestador (RF-20).

## Actores

- Agente orquestador.
- Ambos repos (`citas-api` como proveedor, `citas-web` como consumidor).

## Alcance

- Documentar endpoints, DTOs y códigos de error de negocio.
- Mantener el contrato sincronizado cada vez que una HU agregue o cambie un endpoint.

## Fuera de alcance

- Implementar un BFF o capa intermedia (explícitamente prohibido por las restricciones técnicas).

## Reglas de negocio

- RF-20: el frontend consume la API REST de Spring Boot directamente.

## Dependencias

- Transversal: se actualiza junto con cualquier otra épica que exponga o cambie un endpoint.

## Historias de usuario

- [[HU-024-contrato-rest-citas-api]] (historia continua, se revisa cada sprint)

## Criterio de completitud de la épica

- [ ] El contrato documentado en `llm-wiki/wiki/contratos.md` (o el artefacto OpenAPI equivalente) refleja el 100% de los endpoints implementados en cada momento del proyecto.

## Riesgos e incógnitas

- No identificado.
