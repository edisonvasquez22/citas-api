---
id: HU-001
tipo: historia-de-usuario
titulo: "Registrar cuenta de usuario"
estado: "En desarrollo"
epica: "[[EP-001-autenticacion-y-cuentas]]"
esfuerzo: "Medio"
sprint_sugerido: "Sprint 1 (S2)"
dependencias:
  - "[[HU-006-precarga-de-catalogos-fijos]]"
relacionadas:
  - "[[HU-002-login-y-sesion-jwt]]"
---

# HU-001 — Registrar cuenta de usuario

## Historia de usuario

**COMO** visitante no autenticado
**QUIERO** crear una cuenta de tipo USER con mis datos personales y una contraseña
**PARA** poder acceder al sistema y agendar mis propias citas

> Como visitante no autenticado, quiero crear una cuenta de tipo USER para poder acceder al sistema y agendar mis propias citas.

## Contexto y descripción

Primer paso del ciclo de vida de cualquier paciente ficticio. RF-01 exige datos mínimos, unicidad de email/documento, y que la contraseña nunca se guarde en texto plano.

## Alcance

- Endpoint de registro con: nombres, apellidos, tipo/número de documento, email, teléfono, contraseña.
- Asignación automática del rol `USER` (catálogo fijo, ver [[HU-006-precarga-de-catalogos-fijos]]).
- Validación de unicidad de email y documento.
- Hash de contraseña con algoritmo adaptativo compatible con Spring Security.

## Fuera de alcance

- Login ([[HU-002-login-y-sesion-jwt]]).
- Verificación de email por correo (no exigida por el PRD).
- Creación de PROFESSIONAL/ADMIN ([[EP-004-gestion-de-profesionales]]).

## Reglas de negocio

- Email y documento deben ser únicos (RF-01).
- La contraseña nunca se almacena en texto plano (PRD sección 8).
- Validación server-side de todos los campos obligatorios.

## Dependencias y relaciones

- Épica: [[EP-001-autenticacion-y-cuentas]]
- Dependencias: [[HU-006-precarga-de-catalogos-fijos]] (el rol `USER` debe existir antes de poder asignarlo)
- Relacionadas: [[HU-002-login-y-sesion-jwt]]

## Esfuerzo

**Nivel:** Medio

**Justificación de dificultad:** cruza validación de dominio, unicidad a nivel de persistencia y hashing seguro; alcance acotado a un solo agregado (Usuario).

## Tareas de desarrollo

- [ ] **T-01 — Modelo de dominio `Usuario`**
  Dificultad: Bajo
  Descripción: invariantes de registro (formato de email, documento no vacío), sin dependencias de Spring/JPA.
- [ ] **T-02 — Caso de uso `RegistrarUsuario`**
  Dificultad: Medio
  Descripción: orquesta validación de unicidad vía `UsuarioRepositoryPort` y hashing vía `PasswordHasherPort`.
- [ ] **T-03 — Adaptador REST `POST /api/auth/register`**
  Dificultad: Medio
  Descripción: DTO de request/response, mapeo de errores 409 (duplicado) y 400 (validación).
- [ ] **T-04 — Adaptador de persistencia JPA + migración Flyway**
  Dificultad: Alto
  Descripción: bloqueada hasta que el usuario entregue/apruebe su diseño 3FN de `usuarios`/`roles` (ver `llm-wiki/wiki/decisiones.md`).
- [ ] **T-05 — Pruebas**
  Dificultad: Medio
  Descripción: unitarias de dominio/aplicación + integración del endpoint (feliz, email duplicado, documento duplicado, datos incompletos).

## Criterios de aceptación

### CA-01 — Registro exitoso

**Dado** un visitante con datos válidos y únicos
**Cuando** envía la solicitud de registro
**Entonces** se crea una cuenta `USER` con la contraseña hasheada y la cuenta queda en condiciones de autenticarse ([[HU-002-login-y-sesion-jwt]]).

### CA-02 — Email duplicado

**Dado** un email ya registrado
**Cuando** un visitante intenta registrarse con ese email
**Entonces** el sistema rechaza la solicitud con un error de conflicto y no crea una cuenta nueva.

### CA-03 — Documento duplicado

**Dado** un número de documento ya registrado
**Cuando** un visitante intenta registrarse con ese documento
**Entonces** el sistema rechaza la solicitud con un error de conflicto.

### CA-04 — Datos incompletos

**Dado** un payload de registro sin uno o más campos obligatorios
**Cuando** se envía la solicitud
**Entonces** el sistema responde con error de validación 400 detallando el/los campo(s) inválido(s).

## Definition of Done

- [ ] CA-01 a CA-04 validados con evidencia (test o ejecución documentada).
- [ ] Migración Flyway del esquema afectado presente y coherente con el diseño 3FN aprobado por el usuario.
- [ ] La contraseña nunca aparece en logs ni en la respuesta HTTP.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Contrato del endpoint reflejado en `llm-wiki/wiki/contratos.md` / [[HU-024-contrato-rest-citas-api]].
- [ ] Trazabilidad de esta HU y su épica actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `mvn test` real (2026-09-18): `AuthFlowIntegrationTest.flujoCompleto_registroLoginRefreshLogout`, `RegistrarUsuarioServiceTest.registrar_conDatosValidos_creaUsuarioConPasswordHasheado` — 15/15 tests, BUILD SUCCESS | Ejecutado con JDK 21 (Temurin) + Maven 3.9.16 instalados en la máquina del usuario. |
| CA-02 | Cumple | `mvn test`: `AuthFlowIntegrationTest` (segundo `register` con el mismo email → 409), `RegistrarUsuarioServiceTest.registrar_conEmailDuplicado_lanzaExcepcionDeConflicto` | Ídem. |
| CA-03 | Cumple | `mvn test`: `RegistrarUsuarioServiceTest.registrar_conDocumentoDuplicado_lanzaExcepcionDeConflicto` | Ídem. |
| CA-04 | Cumple | `mvn test`: `AuthFlowIntegrationTest.registro_conDatosIncompletos_devuelve400ConDetalleDeCampos` (agregada 2026-09-18) | Verifica `@NotBlank`/`@Email`/`@Size` en `AuthDtos.RegisterRequest` + `GlobalExceptionHandler.handleValidacion` → 400 con detalle de campos. |
| DoD-01 | Parcial | `V1__esquema_inicial.sql` (tabla `users`/`user_roles`), `UsuarioJpaAdapter`; compilación real de los 36 archivos de `src/main` sin errores | El código de persistencia compila y el flujo de autenticación está 100% verificado con pruebas reales. Lo único no verificado: correr la migración Flyway y el adaptador JPA contra un MySQL real (las pruebas usan los dobles en memoria de `testsupport`, no hay Docker/MySQL disponible todavía). |

## Evidencia de ejecución real (2026-09-18)

```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
JDK 21.0.12 (Eclipse Temurin) y Apache Maven 3.9.16 instalados en la máquina del usuario (`winget` para el JDK; descarga oficial verificada por SHA-512 para Maven, que no tiene paquete winget). Ver `citas-api/docs/wiki/llm-wiki/wiki/decisiones.md`.

## Historial de validación

- 2026-09-17 — HU creada en estado `Pendiente de aprobación`, propuesta como parte del incremento S2.
- 2026-09-17 — Usuario aprueba explícitamente el alcance de S2 (HU-006, HU-001, HU-002). Estado pasa a `En desarrollo`.
- 2026-09-17 — Implementado dominio (`Usuario`), caso de uso (`RegistrarUsuarioService`), endpoint (`POST /api/auth/register`) y pruebas (unitarias + integración) con adaptador temporal en memoria.
- 2026-09-17 — Usuario aclara que el diseño de datos también lo implementa el agente (solo el diseño visual queda reservado). T-04 completada: modelo 3FN diseñado (`docs/db-design/MODELO_3FN.md`), migración `V1__esquema_inicial.sql`/`V2__seed_catalogos_fijos.sql`, y adaptador `UsuarioJpaAdapter` real reemplazando al temporal (que se movió a `src/test/.../testsupport/` para las pruebas, que siguen sin MySQL disponible).
- 2026-09-18 — Usuario instala JDK 21 y Maven. Se ejecuta `mvn test` real: BUILD SUCCESS, 15/15 pruebas (se agregó `registro_conDatosIncompletos_devuelve400ConDetalleDeCampos` para cubrir CA-04, que antes no tenía prueba dedicada). CA-01 a CA-04 pasan a `Cumple`.

## Notas y decisiones

- Único pendiente real: ejecutar contra MySQL (requiere Docker) para verificar que `V1`/`V2` aplican limpio y que `UsuarioJpaAdapter` funciona contra la base real, no solo contra el doble en memoria.
