---
id: HU-011
tipo: historia-de-usuario
titulo: "Activar/desactivar profesional"
estado: Borrador
epica: "[[EP-004-gestion-de-profesionales]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-010-registrar-profesional]]"
relacionadas: []
---

# HU-011 — Activar/desactivar profesional

## Historia de usuario

**COMO** ADMIN
**QUIERO** activar o desactivar un profesional existente
**PARA** controlar si puede publicar agenda y recibir nuevas citas

> Como ADMIN, quiero activar o desactivar un profesional para controlar su disponibilidad operativa.

## Contexto y descripción

RF-07 (activar/desactivar). Un profesional desactivado no debe poder publicar nuevos bloques ni recibir nuevas citas, pero sus citas ya aprobadas no desaparecen.

## Alcance

- `PATCH /api/professionals/{id}/status`: activa o desactiva un profesional.

## Fuera de alcance

- Cancelar automáticamente las citas ya aprobadas de un profesional desactivado (no descrito en el PRD; si se necesita, se trata como una HU aparte).

## Reglas de negocio

- Un profesional desactivado no puede crear nuevos bloques de disponibilidad ni recibir nuevas citas.

## Dependencias y relaciones

- Épica: [[EP-004-gestion-de-profesionales]]
- Dependencias: [[HU-010-registrar-profesional]]
- Relacionadas: ninguna adicional.

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** cambio de estado simple sobre un agregado ya existente.

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `CambiarEstadoProfesional`**
  Dificultad: Bajo
  Descripción: valida transición activo↔inactivo.
- [ ] **T-02 — Endpoint REST**
  Dificultad: Bajo
  Descripción: autorización exclusiva de ADMIN.
- [ ] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: activar, desactivar, verificar que un profesional inactivo no aparece como opción al publicar disponibilidad nueva.

## Criterios de aceptación

### CA-01 — Desactivación efectiva

**Dado** un profesional activo
**Cuando** ADMIN lo desactiva
**Entonces** el profesional no puede crear nuevos bloques de disponibilidad ni ser seleccionado para nuevas citas.

### CA-02 — Reactivación

**Dado** un profesional inactivo
**Cuando** ADMIN lo reactiva
**Entonces** vuelve a poder publicar disponibilidad y recibir citas.

## Definition of Done

- [ ] CA-01 y CA-02 validados con evidencia.
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
