---
id: HU-012
tipo: historia-de-usuario
titulo: "Gestionar bloques de disponibilidad del profesional"
estado: "En desarrollo"
epica: "[[EP-005-agenda-y-disponibilidad]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-010-registrar-profesional]]"
  - "[[HU-009-administrar-catalogo-especialidades]]"
relacionadas:
  - "[[HU-013-consultar-disponibilidad]]"
---

# HU-012 — Gestionar bloques de disponibilidad del profesional

## Historia de usuario

**COMO** PROFESSIONAL activo
**QUIERO** crear, editar y eliminar bloques de disponibilidad por día y sede
**PARA** que el sistema pueda ofrecer horarios reales a los usuarios

> Como PROFESSIONAL activo, quiero gestionar mis bloques de disponibilidad para que el sistema ofrezca horarios reales.

## Contexto y descripción

RF-08. Ejemplo de un día válido: 08:00–12:00 HIC y 14:00–17:00 HIC. Cada bloque se discretiza en slots de 30 minutos.

## Alcance

- Crear múltiples bloques por día, cada uno con su sede.
- Editar/eliminar bloques futuros que no tengan citas comprometidas.
- Consultar el propio calendario de bloques.
- Discretización automática de cada bloque en slots de 30 minutos.

## Fuera de alcance

- Consulta de disponibilidad desde la perspectiva de USER ([[HU-013-consultar-disponibilidad]]).

## Reglas de negocio

- No crear bloques en el pasado (RN-06).
- No solapar bloques del mismo profesional.
- El profesional debe estar habilitado en la sede del bloque (RN-07).
- No editar/eliminar un bloque futuro que ya tenga una cita comprometida.

## Dependencias y relaciones

- Épica: [[EP-005-agenda-y-disponibilidad]]
- Dependencias: [[HU-010-registrar-profesional]] (profesional activo con sede asignada), [[HU-009-administrar-catalogo-especialidades]]
- Relacionadas: [[HU-013-consultar-disponibilidad]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** discretización en slots, validación de solapamiento y de sede habilitada, y protección de bloques con citas comprometidas.

## Tareas de desarrollo

- [ ] **T-01 — Modelo de dominio `BloqueDisponibilidad` + discretización en slots de 30 min**
  Dificultad: Alto
  Descripción: sin dependencias de Spring/JPA; expone los slots resultantes.
- [ ] **T-02 — Casos de uso crear/editar/eliminar bloque**
  Dificultad: Alto
  Descripción: valida RN-06, RN-07, no solapamiento y ausencia de citas comprometidas antes de editar/eliminar.
- [ ] **T-03 — Endpoints REST + migración Flyway**
  Dificultad: Alto
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-04 — Pruebas**
  Dificultad: Alto
  Descripción: bloque válido, bloque en el pasado, bloques solapados, sede no habilitada, edición/eliminación de bloque con cita comprometida.

## Criterios de aceptación

### CA-01 — Creación de bloque válido

**Dado** un profesional activo habilitado en una sede
**Cuando** crea un bloque futuro en esa sede sin solaparse con otro bloque propio
**Entonces** el bloque queda creado y discretizado en slots de 30 minutos.

### CA-02 — Bloque en el pasado

**Dado** una fecha/hora en el pasado
**Cuando** el profesional intenta crear un bloque ahí
**Entonces** el sistema rechaza la creación.

### CA-03 — Solapamiento

**Dado** un bloque existente del profesional
**Cuando** intenta crear otro bloque que se solapa en el tiempo
**Entonces** el sistema rechaza la creación.

### CA-04 — Sede no habilitada

**Dado** un profesional no habilitado en una sede
**Cuando** intenta crear un bloque en esa sede
**Entonces** el sistema rechaza la creación.

### CA-05 — Bloque con cita comprometida

**Dado** un bloque futuro con al menos un slot ya reservado por una cita
**Cuando** el profesional intenta editarlo o eliminarlo
**Entonces** el sistema rechaza la operación.

## Definition of Done

- [x] CA-01 a CA-05 validados con evidencia.
- [x] Migración Flyway coherente con el diseño 3FN aprobado.
- [x] `mvn test` pasa para los módulos afectados, incluida al menos una prueba de solapamiento.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `GestionarBloquesDisponibilidadServiceTest.crear_bloqueValido_quedaCreado`, `BloqueDisponibilidadTest.generarSlots_discretizaEnBloquesDe30Minutos` | — |
| CA-02 | Cumple | `GestionarBloquesDisponibilidadServiceTest.crear_enElPasado_seRechaza`, `BloqueDisponibilidadTest.crear_enElPasado_lanzaValidacionNegocio` | — |
| CA-03 | Cumple | `GestionarBloquesDisponibilidadServiceTest.crear_solapadoConOtroBloquePropio_seRechaza`, `BloqueDisponibilidadTest.seSolapaCon_*` | — |
| CA-04 | Cumple | `GestionarBloquesDisponibilidadServiceTest.crear_enSedeNoHabilitada_seRechaza` | — |
| CA-05 | Cumple | `GestionarBloquesDisponibilidadServiceTest.eliminar_bloqueConCitaComprometida_seRechaza`, `.editar_bloqueConCitaComprometida_seRechaza` | — |
| DoD-01 | Cumple | `mvn test`: 76/76, `BUILD SUCCESS` (2026-09-25) | Sin verificar aún contra MySQL real (Docker pendiente) |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-25 — Aprobada explícitamente por el usuario como parte del alcance de S3; implementada y validada. Estado → `En desarrollo`.

## Notas y decisiones

- Candidata natural para `GOAL_02_GUIADO_AVANZADO.md` en S3 por su alcance cross-capas (dominio + persistencia + REST).
