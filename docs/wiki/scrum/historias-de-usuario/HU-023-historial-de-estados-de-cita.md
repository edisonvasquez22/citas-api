---
id: HU-023
tipo: historia-de-usuario
titulo: "Registrar y consultar historial de estados de una cita"
estado: Borrador
epica: "[[EP-010-auditoria-de-estados]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 2 (S3)"
dependencias: []
relacionadas:
  - "[[HU-014-solicitar-cita-general]]"
  - "[[HU-015-solicitar-cita-especializada]]"
  - "[[HU-016-aprobar-rechazar-cita-especializada]]"
  - "[[HU-018-cancelar-cita]]"
  - "[[HU-020-aprobar-rechazar-reprogramacion]]"
  - "[[HU-022-marcar-cierre-de-atencion]]"
---

# HU-023 — Registrar y consultar historial de estados de una cita

## Historia de usuario

**COMO** sistema
**QUIERO** registrar cada cambio de estado de una cita con su actor, fuente, fecha/hora y motivo opcional
**PARA** dejar una auditoría verificable de todo el ciclo de vida de la cita

> Como sistema, quiero registrar cada cambio de estado de una cita para dejar una auditoría verificable.

## Contexto y descripción

RF-19. Es un componente transversal usado por todas las HU que producen una transición de estado (EP-006 a EP-009). RN-11 exige transiciones explícitas y verificables; RN-12 exige que la auditoría no se trate como un CRUD normal (no se edita ni se borra).

## Alcance

- Componente/servicio de dominio para registrar una transición: cita, estado nuevo, actor (cuando exista), fuente (`SYSTEM`/`USER`/`ADMIN`), fecha/hora, motivo opcional.
- Exposición de lectura del historial dentro de "mis citas" ([[HU-017-consultar-mis-citas]]), la bandeja administrativa ([[HU-016-aprobar-rechazar-cita-especializada]], [[HU-020-aprobar-rechazar-reprogramacion]]) y la agenda del profesional ([[HU-021-consultar-agenda-profesional]]).

## Fuera de alcance

- Editar o borrar un registro de auditoría ya creado (RN-12).

## Reglas de negocio

- RN-11 (transición explícita y verificable), RN-12 (auditoría inmutable, no es CRUD normal).

## Dependencias y relaciones

- Épica: [[EP-010-auditoria-de-estados]]
- Dependencias: ninguna propia; se activa desde la primera HU que produce una transición real ([[HU-014-solicitar-cita-general]]).
- Relacionadas: todas las HU que cambian el estado de una cita (ver metadata).

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** es un componente transversal invocado desde múltiples casos de uso; su diseño debe ser genérico sin duplicar lógica en cada HU consumidora.

## Tareas de desarrollo

- [ ] **T-01 — Puerto `RegistrarTransicionEstadoPort` y modelo de dominio del registro de auditoría**
  Dificultad: Medio
  Descripción: reutilizable desde cualquier caso de uso que cambie el estado de una cita.
- [ ] **T-02 — Adaptador de persistencia + migración Flyway**
  Dificultad: Medio
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario; la tabla de auditoría no admite `UPDATE`/`DELETE` desde la aplicación.
- [ ] **T-03 — Exposición de lectura del historial en los endpoints consumidores**
  Dificultad: Bajo
  Descripción: se integra en las respuestas de HU-017/HU-016/HU-020/HU-021.
- [ ] **T-04 — Pruebas**
  Dificultad: Medio
  Descripción: verificar que cada transición de las HU consumidoras efectivamente deja un registro; verificar que no existe ningún endpoint de edición/borrado de auditoría.

## Criterios de aceptación

### CA-01 — Registro de cada transición

**Dado** cualquier cambio de estado de una cita producido por una HU consumidora
**Cuando** la transición ocurre
**Entonces** queda un registro con cita, estado nuevo, actor (si existe), fuente, fecha/hora y motivo (si aplica).

### CA-02 — Auditoría inmutable

**Dado** un registro de auditoría existente
**Cuando** se intenta modificarlo o eliminarlo mediante la API
**Entonces** no existe ningún endpoint que lo permita.

### CA-03 — Historial visible según ownership

**Dado** un USER, PROFESSIONAL o ADMIN autenticado
**Cuando** consulta el historial de una cita a la que tiene acceso según su rol
**Entonces** ve las transiciones correspondientes; no ve el historial de citas fuera de su ownership.

## Definition of Done

- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración Flyway coherente con el diseño 3FN aprobado.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Al menos una HU consumidora (p. ej. [[HU-014-solicitar-cita-general]]) demuestra la integración end-to-end.
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

- Se recomienda implementar esta HU en paralelo con [[HU-014-solicitar-cita-general]] (Sprint 2), ya que esa es la primera transición de estado real del sistema.
