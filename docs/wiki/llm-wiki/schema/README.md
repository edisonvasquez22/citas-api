# Schema de la LLM Wiki

Adaptación del patrón descrito en `prompts/agents/PROMPT_AGENT_ORQUESTADOR.md` (fuente conceptual: LLM Wiki de Karpathy) para el workspace `citas`.

## Carpetas

```text
llm-wiki/
├── raw/     fuentes curadas e inmutables (solo lectura durante ingest)
├── wiki/    páginas Markdown mantenidas por el agente orquestador
└── schema/  este archivo: convenciones y workflows
```

## Reglas de `raw/`

- Solo el agente orquestador agrega snapshots nuevos, y solo cuando el usuario aprobó la fuente (PRD, restricciones, decisiones, contratos).
- Nunca se edita un archivo de `raw/` para "corregirlo"; si la fuente cambió, se agrega una nueva versión fechada o se reemplaza solo si el usuario confirma que la anterior quedó obsoleta.
- `raw/` no contiene secretos, credenciales ni datos reales de FCV.

## Reglas de `wiki/`

- Cada página es una síntesis, no una transcripción. No pegar conversaciones completas.
- `wiki/index.md` es el catálogo: qué página responde qué pregunta. Se lee primero en toda operación QUERY.
- `wiki/log.md` es append-only: una línea por operación (INGEST/QUERY/LEARN/LINT) con fecha, tipo y resumen de una línea.
- Páginas mínimas esperadas: `dominio.md`, `arquitectura.md`, `contratos.md`, `decisiones.md`, `riesgos.md`.

## Operaciones

| Operación | Cuándo | Qué hace |
|---|---|---|
| INGEST | al aprobar una fuente nueva (PRD, restricción, decisión) | copia la fuente a `raw/`, integra el conocimiento en páginas de `wiki/`, actualiza `index.md` y agrega línea a `log.md` |
| QUERY | al responder una pregunta de dominio/arquitectura/decisión | lee `index.md` → páginas relevantes → contrasta contra código/specs reales; separa evidencia de inferencia |
| LEARN | tras una interacción sustancial (sesión S2-S6) | extrae solo conocimiento durable; clasifica como `HECHO`, `DECISIÓN`, `PREFERENCIA` o `PREGUNTA ABIERTA`; verifica antes de persistir |
| LINT | periódicamente o antes de cerrar una sesión | detecta contradicciones, claims obsoletos, duplicados, páginas huérfanas, enlaces rotos, decisiones no aprobadas y contenido sensible |

## Clasificación LEARN

- **HECHO**: verificable en código, PRD o restricciones.
- **DECISIÓN**: elección explícita del usuario/estudiante, con fecha y motivo.
- **PREFERENCIA**: forma de trabajar que el usuario pidió mantener.
- **PREGUNTA ABIERTA**: incógnita real que bloquea o condiciona una decisión futura.

## Prohibido

- Passwords, tokens, secretos JWT/DB/OAuth/MCP.
- PII de laboratorio más allá de lo estrictamente necesario para el ejercicio.
- Datos reales (no públicos) de FCV.
