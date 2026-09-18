---
id: HU-002
tipo: historia-de-usuario
titulo: "Iniciar sesión y gestionar sesión JWT"
estado: "En desarrollo"
epica: "[[EP-001-autenticacion-y-cuentas]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 1 (S2)"
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-006-precarga-de-catalogos-fijos]]"
relacionadas:
  - "[[HU-003-recuperar-contrasena]]"
---

# HU-002 — Iniciar sesión y gestionar sesión JWT

## Historia de usuario

**COMO** usuario con cuenta creada (USER, PROFESSIONAL o ADMIN)
**QUIERO** iniciar sesión con email y contraseña y mantener mi sesión vigente de forma segura
**PARA** usar las funcionalidades del sistema según mi rol sin volver a autenticarme en cada solicitud

> Como usuario con cuenta creada, quiero iniciar sesión y mantener mi sesión vigente para usar el sistema según mi rol.

## Contexto y descripción

RF-02 cubre login, emisión de access/refresh token, renovación y revocación/logout. El rol es parte del contexto de autorización de cada request.

## Alcance

- `POST /api/auth/login`: valida credenciales, emite access token (corta duración) y refresh token.
- `POST /api/auth/refresh`: intercambia un refresh token válido por un nuevo access token (y, según diseño, un nuevo refresh).
- `POST /api/auth/logout`: revoca el refresh token de la sesión actual.
- El access token incluye el/los rol(es) del usuario para autorización posterior.

## Fuera de alcance

- Recuperación de contraseña ([[HU-003-recuperar-contrasena]]).
- Autorización fina por endpoint (se define por HU a medida que existan recursos protegidos).

## Reglas de negocio

- Access token de corta duración; refresh token de vida más larga (valores configurables por variable de entorno, ver `.env.example`).
- El refresh y la revocación deben funcionar de forma coherente con el mecanismo de invalidación elegido (denylist o rotación).
- Los roles viajan en el contexto de autorización del token.

## Dependencias y relaciones

- Épica: [[EP-001-autenticacion-y-cuentas]]
- Dependencias: [[HU-001-registrar-usuario]] (debe existir la cuenta), [[HU-006-precarga-de-catalogos-fijos]] (catálogo de roles)
- Relacionadas: [[HU-003-recuperar-contrasena]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** requiere diseño de dos tokens con ciclos de vida distintos, mecanismo de revocación y wiring completo de Spring Security; es la pieza de mayor riesgo transversal del incremento S2.

## Tareas de desarrollo

- [ ] **T-01 — Puertos de aplicación**
  Dificultad: Medio
  Descripción: `TokenProviderPort` (emitir/validar/decodificar), `RefreshTokenStorePort` (persistir/revocar), `AuthenticationPort` (validar credenciales contra hash).
- [ ] **T-02 — Casos de uso `IniciarSesion`, `RenovarSesion`, `CerrarSesion`**
  Dificultad: Alto
  Descripción: orquestan los puertos anteriores; definen explícitamente la estrategia de revocación (documentarla en Notas).
- [ ] **T-03 — Adaptador JWT (`io.jsonwebtoken:jjwt` 0.13.0)**
  Dificultad: Medio
  Descripción: firmar/verificar access y refresh con secretos separados (`JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`), incluir claim de rol(es).
- [ ] **T-04 — Spring Security config + filtro JWT**
  Dificultad: Alto
  Descripción: filtro que autentica cada request por access token; contexto de autorización con roles.
- [ ] **T-05 — Endpoints REST `login`/`refresh`/`logout`**
  Dificultad: Medio
  Descripción: DTOs, códigos de error (401 credenciales inválidas, 401/403 token inválido o expirado).
- [ ] **T-06 — Pruebas**
  Dificultad: Alto
  Descripción: login feliz, credenciales inválidas, refresh válido, refresh revocado/expirado, logout invalida el refresh usado.

## Criterios de aceptación

### CA-01 — Login exitoso

**Dado** una cuenta existente con contraseña correcta
**Cuando** el usuario envía email y contraseña a `/api/auth/login`
**Entonces** recibe un access token y un refresh token válidos, y el access token incluye su(s) rol(es).

### CA-02 — Credenciales inválidas

**Dado** un email inexistente o una contraseña incorrecta
**Cuando** se intenta login
**Entonces** el sistema responde `401` sin revelar cuál dato específico fue incorrecto.

### CA-03 — Refresh válido

**Dado** un refresh token vigente y no revocado
**Cuando** se solicita `/api/auth/refresh`
**Entonces** el sistema emite un nuevo access token (y aplica la política de rotación definida para el refresh) sin exigir credenciales de nuevo.

### CA-04 — Refresh inválido, expirado o revocado

**Dado** un refresh token expirado, revocado o manipulado
**Cuando** se solicita `/api/auth/refresh`
**Entonces** el sistema responde `401` y no emite un nuevo access token.

### CA-05 — Logout revoca la sesión

**Dado** una sesión activa con un refresh token válido
**Cuando** el usuario solicita `/api/auth/logout`
**Entonces** ese refresh token queda revocado y una solicitud posterior de refresh con el mismo token falla (CA-04).

## Definition of Done

- [ ] CA-01 a CA-05 validados con evidencia.
- [ ] Secretos JWT leídos únicamente de variables de entorno; nunca hardcodeados ni logueados.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Contrato de los tres endpoints reflejado en `llm-wiki/wiki/contratos.md` / [[HU-024-contrato-rest-citas-api]].
- [ ] Estrategia de revocación documentada en la sección Notas de esta HU.
- [ ] Trazabilidad de esta HU y su épica actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `mvn test` real (2026-09-18): `AuthFlowIntegrationTest`, `IniciarSesionServiceTest.iniciarSesion_conCredencialesValidas_emiteAccessYRefreshToken` — 15/15 tests, BUILD SUCCESS | Ejecutado con JDK 21 (Temurin) + Maven 3.9.16 instalados en la máquina del usuario. |
| CA-02 | Cumple | `mvn test`: `AuthFlowIntegrationTest` (login con password incorrecto → 401), `IniciarSesionServiceTest.iniciarSesion_conEmailInexistente_lanzaCredencialesInvalidas`, `...conPasswordIncorrecto_lanzaCredencialesInvalidas` | Ídem. |
| CA-03 | Cumple | `mvn test`: `AuthFlowIntegrationTest` (refresh válido → 200 con tokens nuevos), `RenovarSesionServiceTest.renovar_conRefreshVigente_rotaElTokenYEmiteUnoNuevo` | Ídem. |
| CA-04 | Cumple | `mvn test`: `AuthFlowIntegrationTest` (reutilizar el refresh ya rotado → 401), `RenovarSesionServiceTest.renovar_conRefreshRevocado_lanzaTokenInvalido` | Ídem. |
| CA-05 | Cumple | `mvn test`: `AuthFlowIntegrationTest` (logout → 204, refresh posterior con ese token → 401) | Ídem. |
| DoD-01 | Parcial | `V1__esquema_inicial.sql` (tabla `refresh_tokens`), `RefreshTokenJpaAdapter`; compila sin errores junto con el resto de `src/main` | Persistencia real implementada y compilando; el flujo JWT completo está 100% verificado con pruebas reales. Falta correr el adaptador contra MySQL real (las pruebas usan el doble en memoria de `testsupport`, sin Docker/MySQL disponible todavía). |

## Evidencia de ejecución real (2026-09-18)

```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
JDK 21.0.12 (Eclipse Temurin) y Apache Maven 3.9.16 instalados en la máquina del usuario. Ver `citas-api/docs/wiki/llm-wiki/wiki/decisiones.md`.

## Historial de validación

- 2026-09-17 — HU creada en estado `Pendiente de aprobación`, propuesta como parte del incremento S2 (condición final del `GOAL_01_GUIADO_SIMPLE`).
- 2026-09-17 — Usuario aprueba explícitamente el alcance de S2 (HU-006, HU-001, HU-002), incluyendo access+refresh+logout. Estado pasa a `En desarrollo`.
- 2026-09-17 — Implementados `IniciarSesionService`, `RenovarSesionService`, `CerrarSesionService`, adaptador JWT (`JwtTokenProviderAdapter`, jjwt 0.13.0) y filtro de seguridad (`JwtAuthenticationFilter`). Estrategia de revocación: **rotación** — cada `/refresh` revoca el refresh usado y emite uno nuevo. Persistencia inicial con adaptador temporal en memoria.
- 2026-09-17 — Usuario aclara que el diseño de datos también lo implementa el agente. Reemplazado el denylist en memoria por `RefreshTokenJpaAdapter` real sobre la tabla `refresh_tokens` (`V1__esquema_inicial.sql`); el doble en memoria se movió a `src/test/.../testsupport/` para pruebas sin MySQL.
- 2026-09-18 — Usuario instala JDK 21 y Maven. Se ejecuta `mvn test` real: BUILD SUCCESS, 15/15 pruebas. CA-01 a CA-05 pasan a `Cumple`.

## Notas y decisiones

- Esta es la HU objetivo de `prompts/goal-loop/GOAL_01_GUIADO_SIMPLE.md`: el `/goal` debe detenerse cuando (1) registro único funcione, (2) password hasheado, (3) login emita access+refresh, (4) refresh genere sesión renovada, (5) casos negativos con pruebas, (6) `mvn test` pase. **Las seis condiciones se cumplieron** el 2026-09-18; solo falta la verificación contra MySQL real (requiere Docker).

### Ejecución formal de GOAL_01 (2026-09-18)

Verificación checkpoint por checkpoint contra el código real (no solo evidencia previa), con `mvn test` corrido de nuevo en el momento:

| # | Condición de parada | Verificado en | Resultado |
|---|---|---|---|
| 1 | Registro USER con email/documento únicos | `RegistrarUsuarioService` (`existePorEmail`/`existePorNumeroDocumento` → `EmailYaRegistradoException`/`DocumentoYaRegistradoException`) | PASS |
| 2 | Password hasheado | `BCryptPasswordHasherAdapter` (`BCryptPasswordEncoder`) | PASS |
| 3 | Login emite access + refresh | `IniciarSesionService.iniciarSesion` | PASS |
| 4 | Refresh válido → sesión renovada | `RenovarSesionService.renovar` (rotación: revoca `jti` usado, emite access+refresh nuevos) | PASS |
| 5 | Casos negativos con pruebas | `RegistrarUsuarioServiceTest` (email/documento duplicado), `IniciarSesionServiceTest` (email inexistente/password incorrecto), `RenovarSesionServiceTest` (refresh revocado/malformado), `AuthFlowIntegrationTest` (400 datos incompletos) | PASS |
| 6 | `mvn test` pasa | Ejecutado en el momento: `Tests run: 15, Failures: 0, Errors: 0` — BUILD SUCCESS | PASS |

**GOAL_01: PASS.** No se necesitaron reintentos (las 6 condiciones ya estaban implementadas de la sesión S2 original; esta ejecución fue de verificación formal, no de implementación nueva). No se tocó UI ni recuperación de contraseña, conforme al alcance del goal.
- Decisión tomada: **rotación de refresh token con denylist persistido** (`RefreshTokenStorePort` → `RefreshTokenJpaAdapter` → tabla `refresh_tokens`). Cada `/refresh` invalida el token anterior (marca `revoked_at`).
