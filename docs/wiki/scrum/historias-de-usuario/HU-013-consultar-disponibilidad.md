---
id: HU-013
tipo: historia-de-usuario
titulo: "Consultar disponibilidad de horarios"
estado: Borrador
epica: "[[EP-005-agenda-y-disponibilidad]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-012-gestionar-bloques-de-disponibilidad]]"
relacionadas:
  - "[[HU-014-solicitar-cita-general]]"
  - "[[HU-015-solicitar-cita-especializada]]"
---

# HU-013 — Consultar disponibilidad de horarios

## Historia de usuario

**COMO** USER autenticado
**QUIERO** filtrar horarios disponibles por sede, tipo de cita, especialidad, profesional y fecha
**PARA** elegir un horario que efectivamente pueda reservar

> Como USER autenticado, quiero consultar disponibilidad filtrada para elegir un horario reservable.

## Contexto y descripción

RF-10. Solo deben mostrarse horarios que puedan completar toda la duración requerida (1 slot para 30 min, 2 slots consecutivos para 60 min).

## Alcance

- `GET /api/availability` con filtros combinables: sede, tipo general/especializada, especialidad, profesional, fecha.
- El resultado excluye slots ya reservados/retenidos y horarios que no alcancen a completar la duración de la especialidad.

## Fuera de alcance

- La reserva en sí (ver [[HU-014-solicitar-cita-general]] y [[HU-015-solicitar-cita-especializada]]).

## Reglas de negocio

- Solo se muestran horarios que puedan completar toda la duración requerida (RF-10).
- RN-05 (slots consecutivos para 60 min), RN-01 (no incluir slots ya reservados/retenidos).

## Dependencias y relaciones

- Épica: [[EP-005-agenda-y-disponibilidad]]
- Dependencias: [[HU-012-gestionar-bloques-de-disponibilidad]]
- Relacionadas: [[HU-014-solicitar-cita-general]], [[HU-015-solicitar-cita-especializada]]

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** consulta de solo lectura pero con combinación de filtros y cálculo de slots consecutivos disponibles.

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `ConsultarDisponibilidad`**
  Dificultad: Medio
  Descripción: combina filtros y calcula, por especialidad, si hay slots consecutivos suficientes.
- [ ] **T-02 — Endpoint REST `GET /api/availability`**
  Dificultad: Bajo
  Descripción: parámetros de filtro opcionales/combinables.
- [ ] **T-03 — Pruebas**
  Dificultad: Medio
  Descripción: filtro simple, combinación de filtros, especialidad de 60 min sin dos slots consecutivos libres (debe excluirse).

## Criterios de aceptación

### CA-01 — Filtro combinado

**Dado** filtros de sede, especialidad y fecha
**Cuando** el usuario consulta disponibilidad
**Entonces** solo recibe horarios de esa sede, especialidad y fecha que efectivamente puede reservar.

### CA-02 — Duración de 60 minutos

**Dado** una especialidad de 60 minutos con solo un slot suelto disponible (sin el siguiente consecutivo libre)
**Cuando** se consulta disponibilidad para esa especialidad
**Entonces** ese horario no aparece como opción reservable.

## Definition of Done

- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Contrato reflejado en [[HU-024-contrato-rest-citas-api]].
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
