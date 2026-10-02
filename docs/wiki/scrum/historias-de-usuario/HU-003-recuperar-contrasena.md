---
id: HU-003
tipo: historia-de-usuario
titulo: "Recuperar contraseña"
estado: Terminada
epica: "[[EP-001-autenticacion-y-cuentas]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-001-registrar-usuario]]"
relacionadas:
  - "[[HU-002-login-y-sesion-jwt]]"
---

# HU-003 — Recuperar contraseña

## Historia de usuario

**COMO** usuario que olvidó su contraseña
**QUIERO** solicitar su recuperación por email y definir una nueva contraseña con un token temporal
**PARA** recuperar el acceso a mi cuenta sin depender de un administrador

> Como usuario que olvidó su contraseña, quiero recuperarla con un token temporal de un solo uso para volver a acceder al sistema.

## Contexto y descripción

RF-03. El envío real de correo es opcional en este laboratorio: en desarrollo el token puede exponerse de forma segura en log/respuesta controlada. Explícitamente fuera de alcance de S2 según `GOAL_01_GUIADO_SIMPLE.md`; planificada para S4.

## Alcance

- `POST /api/auth/password-reset/request`: genera un token temporal de un solo uso asociado al email si la cuenta existe.
- `POST /api/auth/password-reset/confirm`: valida el token, define la nueva contraseña (hasheada) e invalida/consume el token.
- En desarrollo, exponer el token de forma seguraa (log estructurado o respuesta controlada) en vez de depender de SMTP real.

## Fuera de alcance

- Integración SMTP real (no obligatoria, ver PRD sección 9).
- Notificación por SMS/WhatsApp.

## Reglas de negocio

- El token es temporal y de un solo uso.
- Cambiar la contraseña invalida/consume el token inmediatamente.
- No revelar si un email existe o no en el mensaje de respuesta de `password-reset/request` (mismo mensaje genérico en ambos casos).

## Dependencias y relaciones

- Épica: [[EP-001-autenticacion-y-cuentas]]
- Dependencias: [[HU-001-registrar-usuario]]
- Relacionadas: [[HU-002-login-y-sesion-jwt]]

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** dos endpoints con manejo de expiración/consumo de token; sin integración SMTP obligatoria el alcance queda acotado.

## Tareas de desarrollo

- [x] **T-01 — Modelo de dominio `TokenRecuperacion`**
  Dificultad: Bajo
  Descripción: invariantes de expiración y de un solo uso.
- [x] **T-02 — Casos de uso `SolicitarRecuperacion` y `ConfirmarRecuperacion`**
  Dificultad: Medio
  Descripción: generación segura del token, validación de expiración/consumo, nuevo hash de contraseña.
- [x] **T-03 — Adaptador REST + migración Flyway de la tabla de tokens de recuperación**
  Dificultad: Medio
  Descripción: `password_reset_tokens` ya existía en `V1__esquema_inicial.sql` (esquema de referencia adoptado
  2026-09-23) — no hizo falta migración nueva. Endpoints `POST /api/auth/password-reset/request|confirm`.
- [x] **T-04 — Pruebas**
  Dificultad: Medio
  Descripción: solicitud sobre email existente/inexistente (mismo mensaje), confirmación con token válido/expirado/ya usado.

## Criterios de aceptación

### CA-01 — Solicitud de recuperación

**Dado** un email registrado
**Cuando** se solicita recuperación de contraseña
**Entonces** se genera un token temporal de un solo uso y la respuesta es el mismo mensaje genérico que si el email no existiera.

### CA-02 — Confirmación exitosa

**Dado** un token de recuperación válido y no usado
**Cuando** se envía la nueva contraseña con ese token
**Entonces** la contraseña queda actualizada (hasheada) y el token queda consumido.

### CA-03 — Token expirado o ya usado

**Dado** un token expirado o previamente consumido
**Cuando** se intenta confirmar una nueva contraseña con él
**Entonces** el sistema rechaza la operación sin modificar la contraseña.

## Definition of Done

- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración Flyway coherente con el diseño 3FN aprobado (tabla ya existía en V1, sin migración nueva).
- [x] El token nunca se loguea en texto plano fuera del mecanismo de exposición controlada de desarrollo.
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `RecuperarContrasenaServiceTest.solicitar_conEmailExistente_generaUnTokenParaEseUsuario` / `solicitar_conEmailInexistente_noGeneraNingunToken` | El controller responde el mismo `AuthDtos.PasswordResetRequestRequest`→mensaje genérico en ambos casos; la diferencia de comportamiento (token creado o no) es interna, nunca se refleja en la respuesta HTTP. |
| CA-02 | Cumple | `RecuperarContrasenaServiceTest.confirmar_conTokenValido_actualizaLaPasswordYConsumeElToken` | Verifica el nuevo hash y que el mismo token ya no sirve una segunda vez. |
| CA-03 | Cumple | `confirmar_conTokenExpirado_seRechazaSinModificarLaPassword`, `confirmar_conTokenYaUsado_seRechazaSinModificarLaPassword`, `confirmar_conTokenInexistente_seRechaza` | 401 vía `TokenInvalidoException` (mismo mapeo que refresh token inválido). |
| DoD-01 | Cumple | `mvn test`: 133/133 BUILD SUCCESS | Incluye las 22 pruebas nuevas de HU-003/004/005/007/008. |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`; planificada para el incremento de S4 según `GUIA_SESIONES_S2_S6.md`.
- 2026-09-30 — Implementada: `TokenRecuperacion` (dominio), `RecuperarContrasenaService`, `PasswordResetTokenJpaAdapter`
  sobre `password_reset_tokens` (ya existía en el esquema), endpoints `POST /api/auth/password-reset/request|confirm`.
  El token en claro solo se expone por log estructurado del servidor (nunca en la respuesta HTTP), para no romper
  CA-01. Pasa de `Borrador` a `En desarrollo`. `mvn test`: 133/133.

## Notas y decisiones

- No confundir con [[HU-002-login-y-sesion-jwt]]: esta HU no requiere sesión activa.
