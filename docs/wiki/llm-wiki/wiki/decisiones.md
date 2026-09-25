---
tipo: wiki
actualizado: 2026-09-17
---

# Decisiones

Cada entrada: fecha, decisión, motivo, quién decide. No declarar una decisión de diseño (datos o UI) en nombre del usuario.

## 2026-09-17 — Entorno de desarrollo sin toolchain local

**HECHO.** La máquina usada para el bootstrap de este proyecto no tenía `git`, `docker`, `java` ni `node` disponibles en `PATH` ni en rutas de instalación comunes de Windows.

**Impacto:** no se pudo ejecutar `scripts\init-repos.ps1`, `scripts\preflight.ps1`, `docker compose`, ni compilar/testear el backend generado.

**Acción pendiente del estudiante:** instalar Git para Windows, Docker Desktop (con integración WSL2) y, opcionalmente, JDK 21 + Maven + Node 24 si prefiere trabajar fuera de los contenedores `citas-api-dev`/`citas-web-dev`. Ver `README.md` sección "Inicio rápido de infraestructura".

## 2026-09-17 — Alcance de HU para el incremento S2

**DECISIÓN, confirmada explícitamente por el usuario.** El backlog completo se generó en `docs/wiki/scrum/`. Alcance aprobado para S2: `HU-006` (precarga de catálogos fijos, prerrequisito técnico), `HU-001` (registrar usuario) y `HU-002` (login + JWT access/refresh/logout completo, sin recortar). `HU-003` (recuperar contraseña) permanece en `Borrador` para S4. Las tres HU pasaron a estado `En desarrollo`.

## 2026-09-17 — Versiones del stack backend (investigación delegada a subagente)

**HECHO**, verificado vía Maven Central/docs.spring.io/documentation.red-gate.com/GitHub:

| Dependencia | Versión fijada | Nota |
|---|---|---|
| `spring-boot-starter-parent` | `3.5.16` | último parche de la línea 3.5.x (EOL OSS 2026-06-30; se mantiene 3.5.x por restricción técnica del curso) |
| Java | 21 LTS | soportado oficialmente por Spring Boot 3.5.x (rango Java 17–25) |
| `com.mysql:mysql-connector-j` | gestionada por el BOM de Spring Boot (`9.7.0`) | no fijar versión explícita en `pom.xml` |
| `flyway-core` / `flyway-mysql` | gestionadas por el BOM (`11.7.2`) | soporte explícito de MySQL 8.4 en la matriz de Redgate no confirmado con certeza; validar migraciones contra MySQL 8.4 real (protocolo compatible con 8.0) |
| JWT | `io.jsonwebtoken:jjwt-api/jjwt-impl/jjwt-jackson` `0.13.0` | preferido sobre `spring-boot-starter-oauth2-resource-server` para access+refresh autoemitido manual |
| `springdoc-openapi-starter-webmvc-ui` | `2.8.17` | usar ≥2.8.9 (versiones ≤2.8.8 rompen con Spring Boot 3.5.x); no usar la línea 3.x (apunta a Spring Boot 4) |

## 2026-09-17 — Backend S2 implementado con adaptadores temporales en memoria

**DECISIÓN de implementación**, consistente con no invadir el diseño de datos reservado al usuario: en vez de esperar el esquema 3FN para dejar HU-001/HU-002 ejecutables, se construyeron `InMemoryUsuarioRepositoryAdapter` e `InMemoryRefreshTokenStoreAdapter`, dos adaptadores de infraestructura (no de dominio) que implementan exactamente los mismos puertos hexagonales que usará el futuro adaptador JPA/MySQL. Esto permite:

- Ejecutar y probar el flujo completo registro→login→refresh→logout sin ninguna tabla.
- Mantener el dominio/aplicación 100% reutilizables cuando el usuario entregue su diseño 3FN: solo cambia la implementación de los puertos, no los casos de uso ni los controladores.

Estrategia de refresh token elegida: **rotación** (cada `/refresh` revoca el token usado y emite uno nuevo), documentada en `HU-002`.

Ver `infrastructure/adapter/out/persistence/README.md` y `src/main/resources/db/migration/README.md` para los pasos de reemplazo cuando el usuario complete la actividad de normalización.

## 2026-09-17 — El usuario acota el alcance reservado solo al diseño visual

**DECISIÓN explícita del usuario**: "lo que yo hago es el diseño gráfico, lo demás impleméntelo". Esto corrige la interpretación anterior (que también reservaba la normalización 3FN). A partir de esta decisión:

- El agente **sí** diseña y mantiene el modelo relacional 3FN (ver más abajo).
- Solo el **prototipado visual** (Stitch → aprobación → Google AI Studio → elegir React/Angular → importar a `citas-web`) sigue siendo responsabilidad exclusiva del usuario.

## 2026-09-17 — Modelo 3FN diseñado e implementado por el agente

Diseño propio en `citas-api/docs/db-design/MODELO_3FN.md`, elaborado **sin consultar** `database/reference/db.sql` primero (para no sesgar el diseño), con comparación posterior en `citas-api/docs/db-design/COMPARACION_REFERENCIA.md`. Implementado en `V1__esquema_inicial.sql` (esquema completo del PRD) y `V2__seed_catalogos_fijos.sql` (catálogos fijos, HU-006).

Se reemplazaron los adaptadores temporales en memoria de HU-001/HU-002 por adaptadores JPA reales (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`, `@Profile("!test")`). Los dobles en memoria se movieron a `src/test/java/.../testsupport/` y solo se usan en pruebas (sin MySQL disponible en el entorno de generación).

## 2026-09-18 — Toolchain instalado; `mvn test` ejecutado con éxito (15/15)

**HECHO**, verificado en la máquina del usuario:

- JDK 21.0.12 (Eclipse Temurin) instalado vía `winget install EclipseAdoptium.Temurin.21.JDK` (agregado automáticamente al PATH de máquina por el instalador MSI).
- Apache Maven 3.9.16 instalado manualmente: `winget` ya no tiene paquete oficial de Maven (`Apache.Maven` no existe en el índice actual); se descargó `apache-maven-3.9.16-bin.zip` desde `dlcdn.apache.org`, se verificó su SHA-512 contra `downloads.apache.org`, y se extrajo a `%USERPROFILE%\tools\apache-maven-3.9.16`. `JAVA_HOME`/`MAVEN_HOME`/`PATH` configurados a nivel de usuario (sin permisos de administrador).
- `mvn test` sobre `citas-api`: **BUILD SUCCESS, 15/15 pruebas** (dominio, casos de uso, integración MockMvc completa de HU-001/HU-002, incluida una prueba nueva de CA-04 que antes no existía). Evidencia actualizada en `HU-001`/`HU-002`.
- Sigue sin Docker/Git en esta máquina: falta ejecutar `V1`/`V2` contra MySQL real y correr `scripts/init-repos.ps1`.

## 2026-09-18 — Segundo export de AI Studio: dominio correcto, alcance recortado a login

**HECHO/DECISIÓN.** El primer export de Google AI Studio para `citas-web` resultó ser un producto no relacionado ("SaludCita", portal español de videoconsultas con dependencias de Gemini API y Express) — descartado, nunca integrado.

El segundo export corrigió el dominio (marca "FCV Citas", `@fcv.org`, sede real de Floridablanca/Santander/Colombia en vez de España/RGPD), pero traía dos problemas nuevos detectados en revisión antes de integrar:

1. **Premisa incorrecta**: se presentaba como un "laboratorio de simulación quirúrgica" para residentes/docentes reservando quirófanos, con cuentas aprovisionadas solo por la Dirección de Docencia — contradice RF-01 del PRD (`USER` = paciente ficticio que se autorregistra).
2. **Alcance mayor al pedido**: traía 5 pantallas completas (login, sesión activa, reserva de quirófano, mis citas, protocolos) con datos mock, cuando solo se pidió la pantalla de login; las otras 4 pantallas corresponden a épicas (EP-005 a EP-009) todavía en `Borrador`, sin aprobar.

**DECISIÓN del usuario** (elegida entre tres opciones presentadas): integrar únicamente la pantalla de login, conservando el diseño visual tal cual lo generó AI Studio (sin tocar layout/estilo), pero (a) conectándola a `POST /api/auth/login`/`POST /api/auth/logout` reales, y (b) corrigiendo solo los textos que contradecían el PRD.

**Implementado por el agente** en `citas-web/front/` (pendiente de aplanar a la raíz del repo y de eliminar los archivos huérfanos, ver más abajo):

- `LoginView`: fetch real a `${VITE_API_URL}/api/auth/login`, manejo de `401`/error de red; se quitó el selector "Modo de prueba" (scaffolding de mocks de AI Studio); textos de audiencia ("personal médico y residentes") y de validación (`@fcv.org` obligatorio) corregidos para reflejar pacientes autorregistrados.
- `SuccessView`: logout real contra `POST /api/auth/logout`; se quitaron campos inventados (rol, departamento, hospitalId) que la API de login no devuelve (no hay endpoint de perfil todavía, EP-002 sin aprobar).
- `RegistrationModal`/`PasswordRecoveryModal`: pasaron de simular una acción falsa a ser placeholders honestos (HU-001 registro existe en backend pero sin pantalla propia aún; HU-003 recuperar contraseña sigue en Borrador).
- `Header`, `SupportModal`, `App.tsx`, `types.ts`: recortados para no referenciar las pantallas fuera de alcance.
- `package.json`/`metadata.json`/`.env.example`: se quitaron `@google/genai`, `express`, `dotenv`, `@types/express`, `lucide-react`, `motion`, `tsx` (sin uso real en el código) y la declaración de capacidad Gemini; `.env.example` ahora usa `VITE_API_URL` en vez de `GEMINI_API_KEY`.
- `citas-web/AGENTS.md` generado (reemplaza `AGENTS.md.template`).

**Pendiente, requiere confirmación del usuario** (bloqueado por el guardrail de acciones destructivas del propio agente, no por una restricción del proyecto): eliminar `BookingMatrixView.tsx`, `MyAppointmentsView.tsx`, `ProtocolsView.tsx`, `data/mockData.ts` (huérfanos, ya no se importan desde `App.tsx` pero siguen physically en el repo y romperían `tsc --noEmit` porque referencian tipos removidos de `types.ts`) y aplanar `citas-web/front/*` a la raíz de `citas-web/` (consistente con `citas-web/.env.example`/`.gitignore` ya existentes en la raíz). Build/typecheck no verificado: no hay Node.js instalado en esta máquina todavía.

## 2026-09-23 — El esquema pasa a ser copia exacta de `database/reference/db.sql`

**DECISIÓN explícita del usuario**, revierte parcialmente la del 2026-09-17 ("el agente sí diseña el modelo 3FN"): el usuario pidió que la base de datos quedara **exacta** a la referencia del trainer (`database/reference/db.sql`), no solo comparada/parcialmente alineada. Se le advirtió explícitamente que esto afecta código ya implementado y probado (HU-001/HU-002), no solo tablas sin construir todavía — el usuario confirmó que quería el alcance completo, incluyendo usuarios/refresh tokens.

**Implementado:**

- `V1__esquema_inicial.sql` reescrito como copia estructural de `db.sql` (mismos nombres de tabla/columna, mismos tipos `UNSIGNED`, mismas constraints/índices), sin las sentencias `CREATE DATABASE`/`USE`/`SET` (las gestiona la conexión).
- `V2__seed_catalogos_fijos.sql` reescrito con los seeds de catálogo de `db.sql` (roles, regímenes, estados de cita, estados de reprogramación, sedes, especialidades). **Deliberadamente no** se copiaron los seeds sintéticos de operación de `db.sql` (profesionales/pacientes/citas/disponibilidad de ejemplo): no hay código ni HU aprobada que los use todavía.
- Cambios de código Java para seguir el nuevo esquema:
  - `users.id`: de UUID generado en `Usuario.registrarNuevo` a `BIGINT AUTO_INCREMENT` asignado por MySQL. El dominio ahora construye el agregado con `id = null`; `UsuarioRepositoryPort.guardar` devuelve el `Usuario` con el id ya poblado tras persistir.
  - `UsuarioJpaEntity`/`UsuarioJpaRepository`: `id` pasa de `String` a `Long` con `@GeneratedValue(IDENTITY)`.
  - `refresh_tokens`: de `id = jti` en claro a `id` autoincremental (surrogate) + `token_hash` (SHA-256 hexadecimal del `jti`, calculado en `RefreshTokenJpaAdapter`) como clave de búsqueda real.
  - `InMemoryUsuarioRepositoryAdapter` (doble de prueba): ahora simula la asignación autoincremental de id al guardar, para que las pruebas reflejen el mismo contrato que el adaptador real.
  - Pruebas ajustadas: `UsuarioTest` (el id es `null` hasta guardar), `RegistrarUsuarioServiceTest` (el mock de `guardar` simula la asignación de id), `IniciarSesionServiceTest`/`RenovarSesionServiceTest` (usan `Usuario.reconstruir` con un id fijo en vez de `Usuario.registrarNuevo`, porque representan un usuario ya persistido).
- `MODELO_3FN.md` reescrito para describir el esquema adoptado (ya no es un diseño propio); `COMPARACION_REFERENCIA.md` conservado como registro histórico, con la sección "Decisión" actualizada para reflejar el cambio.

**Diferencias funcionales reales frente al diseño anterior** (documentadas en `MODELO_3FN.md` sección 8, para que quien retome el proyecto las conozca): `professional_specialties.is_primary` perdió la garantía de unicidad a nivel de base de datos; los slots de disponibilidad perdieron el catálogo de estados y la retención real durante una reprogramación pendiente (RN-10 quedará como responsabilidad de la aplicación, no de la base, cuando se implemente EP-008); a cambio, se ganó historial de afiliación EPS y columnas de auditoría rápida en `appointments`.

Evidencia: `mvn test` corrido tras el cambio — ver `wiki/log.md` para el resultado.

## 2026-09-25 — Alcance de S3 aprobado explícitamente por el usuario

**DECISIÓN, confirmada explícitamente por el usuario.** Se presentaron las 9 HU que el backlog ya proponía para "Sprint 2 (objetivo S3)" — HU-009, HU-010, HU-011, HU-012, HU-013, HU-014, HU-015, HU-016 y HU-023 — y el usuario aprobó el alcance completo tal como estaba, sin recortes. Pasan de `Borrador` a `En desarrollo`. Coincide exactamente con la "Funcionalidad objetivo" de `GUIA_SESIONES_S2_S6.md` S3 (profesionales, disponibilidad, cita general auto-aprobada, cita especializada + aprobación) más HU-009 (dependencia formal de HU-010: solo se pueden asignar especialidades activas) y HU-023 (auditoría transversal que HU-014/015/016 necesitan para RN-11/RN-12).

## 2026-09-25 — Backend de S3 implementado: profesionales, disponibilidad y flujo de citas

**HECHO.** Implementadas las 9 HU aprobadas en `citas-api`, siguiendo el mismo patrón hexagonal que HU-001/002 (dominio sin dependencias de Spring, puertos in/out, adaptadores JPA reales `@Profile("!test")` + dobles en memoria para pruebas). Piezas nuevas relevantes:

- **Reserva atómica de horario (RN-01)**: `SlotRepositoryPort.reservarAtomicamente` — el adaptador JPA hace un `UPDATE professional_slots SET appointment_id = :citaId WHERE id IN (:ids) AND appointment_id IS NULL`, que toma bloqueo de fila real en MySQL/InnoDB; el doble en memoria de pruebas simula la misma atomicidad con compare-and-set por slot. Probado con hilos concurrentes reales (no mocks): 10 solicitudes simultáneas sobre el mismo horario, exactamente 1 gana (`SolicitarCitaGeneralServiceTest`/`SolicitarCitaEspecializadaServiceTest`). Es la prueba de "doble reserva" que pide explícitamente `GUIA_SESIONES_S2_S6.md` para S3.
- **Motivo de rechazo de cita especializada**: `appointments` (copia exacta de `database/reference/db.sql`) no tiene columna propia para el motivo de un rechazo — se audita en `appointment_status_history.reason` (HU-023) en vez de duplicarse en la tabla de citas. La respuesta inmediata de `POST /api/admin/appointments/{id}/reject` sí lo devuelve (viene del parámetro de la petición, no de un roundtrip a la base).
- **Autorización por rol**: `/api/admin/**` exige `ADMIN`, `/api/professionals/me/**` exige `PROFESSIONAL` (vía `hasRole` en `SecurityConfig`, usando los `ROLE_*` que ya emite `JwtAuthenticationFilter`). Al escribir las pruebas de autorización se encontró que, sin un `authenticationEntryPoint` explícito, una request **sin token** también devolvía `403` en vez de `401` (Spring Security trata al anónimo como "autenticado sin permiso" por defecto en endpoints con `hasRole`). Corregido agregando `exceptionHandling().authenticationEntryPoint(...)` — ahora sí distingue `401` (sin token/token inválido) de `403` (token válido, rol insuficiente), consistente con la convención ya documentada en `contratos.md` desde S2.
- **`professional_specialties.is_primary` sin unicidad a nivel de BD**: a diferencia del diseño propio descartado (ver 2026-09-23), el esquema de referencia no tiene una columna generada que garantice una sola especialidad primaria por profesional — la regla (HU-010 CA-02) se aplica solo en `Profesional.registrar` (dominio). Documentado en `riesgos.md`.
- **HU-009 (catálogo de especialidades)**: no se expone `DELETE` — el catálogo solo permite crear/editar/activar/desactivar; la ausencia misma del endpoint satisface CA-03 (no hay forma de borrar físicamente).

**Evidencia:** `mvn test` — 76 pruebas (67 nuevas de S3 + las 9 de S2), incluyendo la prueba de concurrencia real. Detalle completo en `wiki/log.md`.

**No implementado en esta pasada (ver `AGENTS.md` raíz, regla 11):** ninguna pantalla de `citas-web` para profesionales/disponibilidad/reserva de citas — el diseño visual sigue siendo responsabilidad exclusiva del usuario (Stitch/AI Studio). `GOAL_02_GUIADO_AVANZADO.md` (que la guía sugiere para S3) exige explícitamente que el frontend envíe la solicitud real y muestre la respuesta, así que **no se ejecuta como verificación formal todavía** — solo su mitad backend está completa y probada. Se retomará cuando el usuario traiga el export de AI Studio para "Agendar Cita" (ver pendiente anotado el 2026-09-23) y se pueda reconciliar contra estos endpoints reales, igual que se hizo con login/registro.

**Verificación contra MySQL real:** sigue bloqueada por Docker (ver pendiente de siempre). Todo lo anterior corre y se probó contra los dobles en memoria de `testsupport/`.

## 2026-09-25 — Pantalla "Agendar Cita" reconciliada en citas-web contra el backend real de S3

**HECHO.** El usuario trajo el export de AI Studio para la pantalla de "Agendar Cita" (prompt entregado en la sesión anterior). Se reconcilió con el mismo rigor que login/registro:

- **Descartado** (fuera del alcance pedido/aprobado): pestañas "Mis Citas" (HU-017 sin aprobar), "Resultados" de laboratorio/imágenes (**ninguna HU del backlog de 24 HU cubre esto** — invención de AI Studio, no PRD), "Sedes" y "Ayuda" (sin HU); el `SimStrip` (selector manual de estados, scaffolding de demo); `PatientRibbon`/`PatientModal` con datos de paciente inventados (biometría, número de historia clínica, plan — no existen en la API real, no hay endpoint de perfil).
- **Integrado**: el formulario de agendar cita, con datos 100% reales (`GET /api/specialties`, `GET /api/professionals`, `GET /api/availability`) y envío real (`POST /api/appointments/general`/`specialized`), más las pantallas de confirmación (aprobada / pendiente de auditoría) y de "sin turnos disponibles".
- **Cambio cross-repo en `citas-api`**: se agregó `GET /api/professionals` (lectura pública) porque no existía ningún endpoint que un paciente pudiera usar para ver el nombre de un profesional al reservar — la única alternativa era `/api/admin/professionals` (solo ADMIN). Ver `contratos.md`.
- Campos inventados descartados: el "código de cita" con formato bonito (`FCV-GEN-89421`) no existe en el contrato real — se usa el `citaId` numérico real; el campo `prep` (preparación previa) tampoco existe en `specialties` — se quitó en vez de inventarlo.

**Evidencia:** `npm run lint`/`npm run build` en `EXIT 0`. **No verificado con navegador real (Playwright)** — esta sesión no tuvo esa herramienta disponible, y de todas formas el backend no puede correr en vivo sin MySQL real (los dobles en memoria de `testsupport/` solo existen dentro de `@SpringBootTest`, no en un servidor real) — mismo bloqueo de Docker de siempre. La cobertura HTTP real más cercana disponible es `S3AuthorizationIntegrationTest`/`AuthFlowIntegrationTest` (MockMvc), que sí ejercitan la capa REST completa.

## Pendiente de diseño reservado al usuario

- **Prototipado visual** (Skill `stitch-design-to-frontend`): pantallas obligatorias, aprobación explícita y handoff a Google AI Studio. No se generará ningún diseño visual ni se elegirá React/Angular en nombre del usuario.

## Relacionado

[[dominio]] · [[arquitectura]] · [[riesgos]]
