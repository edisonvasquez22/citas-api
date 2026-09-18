---
id: HU-004
tipo: historia-de-usuario
titulo: "Consultar y actualizar perfil"
estado: Borrador
epica: "[[EP-002-perfil-y-afiliacion]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-002-login-y-sesion-jwt]]"
relacionadas:
  - "[[HU-005-asociar-afiliacion]]"
---

# HU-004 — Consultar y actualizar perfil

## Historia de usuario

**COMO** USER autenticado
**QUIERO** consultar y actualizar los datos permitidos de mi perfil
**PARA** mantener mi información de contacto correcta

> Como USER autenticado, quiero consultar y actualizar mi perfil para mantener mi información correcta.

## Contexto y descripción

RF-04 (parte de perfil). No incluye la afiliación EPS/plan/régimen, cubierta por [[HU-005-asociar-afiliacion]].

## Alcance

- `GET /api/users/me`: devuelve los datos de perfil del usuario autenticado.
- `PATCH /api/users/me`: actualiza los campos permitidos (p. ej. teléfono, nombres/apellidos); email y documento no son editables libremente por el propio usuario salvo que se defina un flujo aparte.

## Fuera de alcance

- Cambio de email/documento (fuera del PRD explícito; requeriría re-verificación de unicidad y no está descrito).
- Afiliación EPS/plan/régimen ([[HU-005-asociar-afiliacion]]).

## Reglas de negocio

- Solo el propio usuario puede ver/editar su perfil (ownership).
- Validación server-side de los campos editables.

## Dependencias y relaciones

- Épica: [[EP-002-perfil-y-afiliacion]]
- Dependencias: [[HU-002-login-y-sesion-jwt]] (requiere sesión autenticada).
- Relacionadas: [[HU-005-asociar-afiliacion]]

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** CRUD simple de un único agregado con ownership directo.

## Tareas de desarrollo

- [ ] **T-01 — Casos de uso `ConsultarPerfil`/`ActualizarPerfil`**
  Dificultad: Bajo
  Descripción: aplican reglas de ownership y validación de campos editables.
- [ ] **T-02 — Endpoints REST `GET`/`PATCH /api/users/me`**
  Dificultad: Bajo
  Descripción: DTOs de request/response, autorización por ownership.
- [ ] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: consulta propia, actualización válida, intento de editar campo no permitido.

## Criterios de aceptación

### CA-01 — Consultar perfil propio

**Dado** un USER autenticado
**Cuando** solicita `GET /api/users/me`
**Entonces** recibe sus propios datos de perfil, nunca los de otro usuario.

### CA-02 — Actualizar campos permitidos

**Dado** un USER autenticado
**Cuando** actualiza un campo editable con un valor válido
**Entonces** el cambio queda persistido y se refleja en una consulta posterior.

### CA-03 — Campo no editable

**Dado** un USER autenticado
**Cuando** intenta modificar un campo no editable (p. ej. documento)
**Entonces** el sistema rechaza ese cambio o lo ignora explícitamente, sin romper el resto de la actualización.

## Definition of Done

- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Contrato reflejado en [[HU-024-contrato-rest-citas-api]].
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
