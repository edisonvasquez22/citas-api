---
id: HU-010
tipo: historia-de-usuario
titulo: "Registrar profesional y asignar especialidades/sedes"
estado: Borrador
epica: "[[EP-004-gestion-de-profesionales]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 2 (S3)"
dependencias:
  - "[[HU-006-precarga-de-catalogos-fijos]]"
  - "[[HU-009-administrar-catalogo-especialidades]]"
relacionadas:
  - "[[HU-011-activar-desactivar-profesional]]"
  - "[[HU-012-gestionar-bloques-de-disponibilidad]]"
---

# HU-010 — Registrar profesional y asignar especialidades/sedes

## Historia de usuario

**COMO** ADMIN
**QUIERO** crear un profesional ficticio con código profesional, matrícula, especialidades y sedes
**PARA** habilitarlo a publicar agenda y recibir citas

> Como ADMIN, quiero registrar un profesional con sus especialidades y sedes para habilitarlo en el sistema.

## Contexto y descripción

RF-07. El profesional es un usuario especializado: primero existe como cuenta, luego se le asignan especialidades (una primaria) y una o ambas sedes fijas.

## Alcance

- Crear la cuenta `PROFESSIONAL` (código profesional y matrícula ficticia).
- Asignar una o varias especialidades, marcando exactamente una como primaria.
- Asignar una o ambas sedes (HIC/ICV).

## Fuera de alcance

- Activar/desactivar (ver [[HU-011-activar-desactivar-profesional]]).
- Publicación de bloques de agenda (ver [[HU-012-gestionar-bloques-de-disponibilidad]]).

## Reglas de negocio

- Especialidad primaria única por profesional.
- Solo se pueden asignar especialidades activas ([[HU-009-administrar-catalogo-especialidades]]).
- Nombres y matrícula son sintéticos.

## Dependencias y relaciones

- Épica: [[EP-004-gestion-de-profesionales]]
- Dependencias: [[HU-006-precarga-de-catalogos-fijos]] (rol PROFESSIONAL, sedes), [[HU-009-administrar-catalogo-especialidades]]
- Relacionadas: [[HU-011-activar-desactivar-profesional]], [[HU-012-gestionar-bloques-de-disponibilidad]]

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** combina creación de cuenta con dos relaciones N:M (especialidades, sedes) y una regla de unicidad (especialidad primaria).

## Tareas de desarrollo

- [ ] **T-01 — Caso de uso `RegistrarProfesional`**
  Dificultad: Medio
  Descripción: crea la cuenta, valida especialidades activas y unicidad de la especialidad primaria, valida sedes.
- [ ] **T-02 — Endpoint REST + migración Flyway de profesional/especialidad/sede**
  Dificultad: Alto
  Descripción: bloqueada hasta contar con el diseño 3FN aprobado del usuario.
- [ ] **T-03 — Pruebas**
  Dificultad: Medio
  Descripción: alta válida, especialidad inactiva, dos especialidades primarias, sede inexistente.

## Criterios de aceptación

### CA-01 — Alta exitosa

**Dado** un ADMIN autenticado con datos válidos
**Cuando** registra un profesional con al menos una especialidad primaria y una sede
**Entonces** el profesional queda creado y habilitado en esa(s) sede(s)/especialidad(es).

### CA-02 — Especialidad primaria única

**Dado** un intento de marcar más de una especialidad como primaria
**Cuando** se envía la solicitud
**Entonces** el sistema la rechaza con un error de validación.

### CA-03 — Especialidad inactiva

**Dado** una especialidad desactivada
**Cuando** se intenta asignarla a un profesional
**Entonces** el sistema rechaza la asignación.

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
