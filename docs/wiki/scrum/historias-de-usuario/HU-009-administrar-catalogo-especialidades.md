---
id: HU-009
tipo: historia-de-usuario
titulo: "Administrar catálogo de especialidades"
estado: "En desarrollo"
epica: "[[EP-003-catalogos-del-sistema]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-006-precarga-de-catalogos-fijos]]"
relacionadas:
  - "[[HU-010-registrar-profesional]]"
  - "[[HU-012-gestionar-bloques-de-disponibilidad]]"
---

# HU-009 — Administrar catálogo de especialidades

## Historia de usuario

**COMO** ADMIN
**QUIERO** crear, editar, activar y desactivar especialidades, definiendo su duración de 30 o 60 minutos
**PARA** que el resto del sistema sepa qué especialidades existen y cuánto dura cada cita

> Como ADMIN, quiero administrar las especialidades y su duración para sostener el resto del agendamiento.

## Contexto y descripción

RF-06 (CRUD) + RF-09 (duración 30/60 min como atributo de la especialidad, no del profesional).

## Alcance

- CRUD de especialidades (crear, editar, activar/desactivar), cada una con duración fija de 30 o 60 minutos.

## Fuera de alcance

- Asignación de especialidades a un profesional concreto ([[HU-010-registrar-profesional]]).

## Reglas de negocio

- Duración de la especialidad: 30 min (1 slot) o 60 min (2 slots consecutivos) (RF-09).
- No se permite borrar físicamente una especialidad referenciada por profesionales o citas; solo desactivar (RN-08 depende de que esté activa para poder reservarse).

## Dependencias y relaciones

- Épica: [[EP-003-catalogos-del-sistema]]
- Dependencias: [[HU-006-precarga-de-catalogos-fijos]] (rol ADMIN).
- Relacionadas: [[HU-010-registrar-profesional]], [[HU-012-gestionar-bloques-de-disponibilidad]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** CRUD de catálogo con una regla de dominio simple (duración de valor cerrado 30/60).

## Tareas de desarrollo

- [ ] **T-01 — Casos de uso CRUD de especialidades**
  Dificultad: Bajo
  Descripción: valida que la duración sea exactamente 30 o 60 minutos.
- [ ] **T-02 — Endpoints REST + migración Flyway**
  Dificultad: Bajo
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: creación con duración válida, duración inválida, desactivación, intento de borrado físico referenciado.

## Criterios de aceptación

### CA-01 — CRUD básico con duración válida

**Dado** un ADMIN autenticado
**Cuando** crea o edita una especialidad con duración 30 o 60 minutos
**Entonces** el catálogo refleja el cambio inmediatamente.

### CA-02 — Duración inválida

**Dado** un ADMIN autenticado
**Cuando** intenta crear o editar una especialidad con una duración distinta de 30 o 60 minutos
**Entonces** el sistema rechaza la solicitud con un error de validación.

### CA-03 — Sin borrado físico si está referenciada

**Dado** una especialidad asignada a algún profesional o usada en alguna cita
**Cuando** se intenta eliminarla físicamente
**Entonces** el sistema lo rechaza y solo permite desactivarla.

## Definition of Done

- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración Flyway coherente con el diseño 3FN aprobado.
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AdministrarEspecialidadesServiceTest.crear_conDuracionValida_quedaEnElCatalogo`, `EspecialidadTest` | — |
| CA-02 | Cumple | `AdministrarEspecialidadesServiceTest.crear_conDuracionInvalida_seRechaza`, `EspecialidadTest.crear_conDuracionInvalida_lanzaValidacionNegocio` | — |
| CA-03 | Cumple | No existe endpoint `DELETE` en `AdminEspecialidadesController` — solo `PATCH .../status` (desactivar); la ausencia del endpoint satisface la regla por diseño | — |
| DoD-01 | Cumple | `mvn test`: 76/76, `BUILD SUCCESS` (2026-09-25) | Sin verificar aún contra MySQL real (Docker pendiente) |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-25 — Aprobada explícitamente por el usuario como parte del alcance de S3; implementada y validada. Estado → `En desarrollo`.

## Notas y decisiones

- Ninguna.
