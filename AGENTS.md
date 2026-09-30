# AGENTS.md — `citas-api`

> Generado a partir de `../prompts/agents/PROMPT_AGENT_CITAS_API.md` una vez inicializado el proyecto Spring Boot real (2026-09-17). Reemplaza a `AGENTS.md.template`. Actualizado tras el incremento S3 (2026-09-25).

## Responsabilidad de este repo

- Java 21, Spring Boot 3.5.16, Maven.
- Arquitectura hexagonal (`domain` / `application` / `infrastructure`).
- REST/JSON.
- Spring Security + JWT access/refresh (implementado en HU-002 con rotación de refresh token); autorización por rol (`hasRole`) para `/api/admin/**` (ADMIN) y `/api/professionals/me/**` (PROFESSIONAL), agregada en S3.
- MySQL 8.4 + Spring Data JPA + Flyway, con esquema real (`V1__esquema_inicial.sql`, `V2__seed_catalogos_fijos.sql`; ver `docs/db-design/MODELO_3FN.md`).
- Reglas de negocio del PRD, HU por HU.
- Pruebas de dominio, aplicación e integración.

**Estado verificado (2026-09-25), incremento S3:** `mvn test` da `BUILD SUCCESS` con **76/76 pruebas** (9 de S2 + 67 nuevas: dominio, casos de uso con dobles en memoria, integración MockMvc incluyendo autorización por rol). Incluye una prueba de concurrencia real (10 hilos disputando el mismo horario) para la retención atómica anti doble-reserva de HU-014/HU-015 (RN-01), demostrada explícitamente en Red→Green (ver `docs/wiki/llm-wiki/wiki/log.md`, 2026-09-25). Sigue sin verificarse contra MySQL real (Docker instalado pero no operativo todavía) — todo corre con los dobles en memoria de `src/test/java/.../testsupport/`.

**Historial:** 2026-09-21 (15/15, HU-001/HU-002, GOAL_01 PASS) → 2026-09-23 (15/15, tras adoptar el esquema exacto de `database/reference/db.sql`) → 2026-09-25 (76/76, S3 completo: HU-009/010/011/012/013/014/015/016/023) → 2026-09-28 (82/82, ver nota HU-011 abajo) → 2026-09-29 (106/106, S4 backend completo: HU-017/018/019/020/021/022, ver nota abajo) → 2026-09-29 (109/109, LOOP_02 agregó `reprogramacion` a `MiCitaResponse`, ver nota abajo) → 2026-09-29 (111/111, LOOP_03 agregó CA-04 de HU-019: una cita no puede tener dos reprogramaciones PENDING a la vez, ver nota abajo).

**2026-09-29 — Backend completo de S4 (HU-017 a HU-022): mis citas, cancelación, reprogramación, agenda del profesional, cierre de atención.** Aprobadas explícitamente por el usuario el 2026-09-28 (ver `docs/wiki/scrum/README.md`, "Alcance aprobado para el incremento S4"). Implementación:
- **HU-017** (`GET /api/appointments/mine?estado=&fecha=`): `ConsultarMisCitasService` filtra por ownership; el motivo de rechazo (para citas `REJECTED`) se resuelve consultando `HistorialEstadoCitaPort` en vez de duplicarlo en `appointments` — consistente con el diseño ya establecido en HU-023.
- **HU-018** (`POST /api/appointments/{id}/cancel`): `Cita.cancelar()` nuevo en el dominio (solo `APPROVED`/`REQUESTED` y futura); libera slots vía `SlotRepositoryPort.liberarSlotsDeCita`. Ownership por 404 (mismo patrón ya usado en `GestionarBloquesDisponibilidadService`: no revelar existencia del recurso a quien no es su dueño).
- **HU-019/HU-020** (`POST /api/appointments/{id}/reschedule`, `GET/POST /api/admin/reschedules/**`): nuevo agregado de dominio `SolicitudReprogramacion` sobre `reschedule_requests`/`reschedule_request_statuses` (ya existían en `V1__esquema_inicial.sql`, sin migración nueva). Diseño clave (RN-10): mientras la solicitud está `PENDING`, tanto la franja horaria antigua como la nueva quedan reservadas bajo el **mismo** `citaId` en `professional_slots` — se distinguen por horario al decidir (`SlotRepositoryPort.listarIdsDeCitaEnRango`, método nuevo), liberando solo la que corresponda (`liberarSlots`, también nuevo). Esto evitó tener que modelar una relación slot↔solicitud que no existe en el esquema de referencia.
- **HU-021** (`GET /api/professionals/me/agenda?sedeId=&desde=&hasta=`): nuevo `AgendaProfesionalController`, filtra citas `APPROVED` propias del profesional.
- **HU-022** (`POST /api/appointments/{id}/complete|no-show`): `Cita.completar()`/`marcarNoShow()` nuevos (solo `APPROVED` y ya pasada); ownership resuelto vía `ProfesionalRepositoryPort.buscarPorUsuarioId`.
- Puertos ampliados sin migración nueva: `CitaRepositoryPort.listarPorPaciente/listarPorProfesional`, `SlotRepositoryPort.listarIdsDeCitaEnRango/liberarSlots`. Nuevo puerto `SolicitudReprogramacionRepositoryPort` + adaptador JPA + doble en memoria.
- `mvn test`: **106/106 BUILD SUCCESS** (24 pruebas nuevas). Sigue sin verificarse contra MySQL real (Docker pendiente).
- **Nota de troubleshooting**: durante esta sesión una corrida de `mvn test` reportó 20 pruebas *no relacionadas* fallando con "Unresolved compilation problems" en clases que no se habían tocado — era caché de compilación incremental corrompido por una interrupción de sesión anterior (`mvn clean` incluso falló al borrar `target/` por un lock de Windows). Se resolvió borrando `target/` a mano y volviendo a correr `mvn test`; no era un defecto de código. Si esto se repite, borrar `target/` manualmente antes de diagnosticar el código.

**2026-09-29 — CA-04 de HU-019 agregada tras ejecutar LOOP_03 (reto independiente, diseñado y aprobado explícitamente por el usuario).** Cerró un hallazgo residual de LOOP_02: nada en el backend impedía que una cita tuviera dos solicitudes de reprogramación `PENDING` simultáneas (solo la UI ocultaba el botón). Solución sin migración nueva: `CitaRepositoryPort.conBloqueoDeEscritura(citaId, accion)` — bloqueo de escritura real sobre la cita (JPA: `@Lock(PESSIMISTIC_WRITE)`/`SELECT ... FOR UPDATE`, vigente hasta el commit de la transacción; en memoria: `ReentrantLock` real por citaId) — combinado con `SolicitudReprogramacionRepositoryPort.existePendientePorCita(citaId)`, usados dentro de `SolicitarReprogramacionService.solicitar()` para serializar "verificar que no hay PENDING + reservar slots + crear la solicitud". Si ya hay una `PENDING`, lanza `TransicionEstadoInvalidaException` (409). Demostrado en Red→Green por el propio Builder (guarda comentada → 2 pruebas nuevas fallan bajo 8 hilos concurrentes reales, cada uno con horario distinto para aislar la regla de RN-01 → restaurada → verde) y confirmado por un Verifier aislado. `mvn test`: **111/111 BUILD SUCCESS**. Detalle completo en `docs/wiki/scrum/historias-de-usuario/HU-019-solicitar-reprogramacion.md`, sección "Ejecución formal de LOOP_03".

**2026-09-29 — `MiCitaResponse.reprogramacion` agregado tras ejecutar `LOOP_02_GUIADO_AVANZADO.md`.** Un Verifier aislado (patrón Builder/Verifier de S4) detectó que `GET /api/appointments/mine` no exponía el desenlace de la propia solicitud de reprogramación del paciente — el `EstadoCita` de la cita no cambia mientras está `PENDING` (RN-10), así que sin este campo el paciente no tenía forma de saber si su solicitud fue aprobada o rechazada (ni el motivo). Se agregó `SolicitudReprogramacionRepositoryPort.buscarUltimaPorCita(citaId)` (adaptador JPA vía `findFirstByAppointmentIdOrderByIdDesc`, + doble en memoria) y `ConsultarMisCitasService` lo resuelve para cada cita, exponiéndolo en `MiCitaResponse.reprogramacion` (`{solicitudId, estado, inicioSolicitado, finSolicitado, motivoDecision}` o `null`). 3 pruebas nuevas en `ConsultarMisCitasServiceTest`. `mvn test`: **109/109 BUILD SUCCESS**. Detalle completo (2 iteraciones de Builder/Verifier con evidencia) en `docs/wiki/scrum/historias-de-usuario/HU-019-solicitar-reprogramacion.md`, sección "Ejecución formal de LOOP_02".

**2026-09-28 — `GET /api/admin/professionals` agregado (cierre de HU-011 del lado backend).** Hasta esta fecha `ProfesionalRepositoryPort` solo tenía `listarActivos()` (usado por el directorio público `/api/professionals` para HU-013/014/015): no existía ninguna forma de que el ADMIN viera profesionales **inactivos** para poder reactivarlos, ni de ver sus datos de contacto (nombre/documento/email/teléfono vienen de `Usuario`, no de `Profesional`). Se agregó `listarTodos()` al puerto (+ implementación JPA `findAll()` + doble en memoria), un caso de uso nuevo `ListarProfesionalesAdminUseCase`/`ListarProfesionalesAdminService` que cruza `Profesional` con `Usuario` (mismo patrón que `ListarProfesionalesService` ya usaba para el directorio público), y el endpoint `GET /api/admin/professionals` en `AdminProfesionalesController` (mismo `hasRole("ADMIN")` que el resto de `/api/admin/**`). Fue necesario para poder construir en `citas-web` la UI de HU-011 (activar/desactivar profesional), que sin este endpoint no tenía forma de listar candidatos. Pruebas nuevas: `ListarProfesionalesAdminServiceTest` (incluye/no incluye inactivos) + 2 casos de autorización en `S3AuthorizationIntegrationTest`.

## Reglas arquitectónicas (verificadas contra el código real)

- El dominio (`domain/model`, `domain/exception`) no importa `org.springframework.*` ni `jakarta.persistence.*`. Ver `Usuario.java`.
- Los casos de uso viven en `application/usecase` e implementan interfaces de `application/port/in`; dependen solo de puertos (`application/port/out`), nunca de adaptadores concretos.
- `infrastructure/adapter/in/web` traduce HTTP (DTOs + `AuthController`); no concentra reglas de negocio.
- `infrastructure/adapter/out/**` son los únicos lugares con dependencias de Spring Security/JWT/persistencia.
- No acoplar este backend a React/Angular. No editar `citas-web` desde este agente.
- Cambios de esquema requieren migración Flyway y justificación en la HU correspondiente.
- Secretos solo por variables de entorno (`JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `DB_*`); nunca hardcodeados ni logueados.

## Diseño de datos

**2026-09-23 — El esquema es ahora una copia estructural exacta de `database/reference/db.sql`** (decisión explícita del usuario, revierte la decisión del 17 de septiembre de diseñar independientemente). Ver `docs/db-design/MODELO_3FN.md` (sección 8) y `COMPARACION_REFERENCIA.md` para el detalle de qué cambió. `V1__esquema_inicial.sql` ya cubre **todas** las tablas del PRD, con los mismos nombres de tabla/columna que la referencia; no lo regeneres ni lo edites una vez aplicado contra una base real — agrega `V3__...sql` para cualquier cambio.

Cambio importante para quien toque `users`/`refresh_tokens`: `users.id` ahora es `BIGINT UNSIGNED AUTO_INCREMENT` (antes UUID generado en dominio) — `Usuario.registrarNuevo` construye el agregado con `id = null`, y `UsuarioRepositoryPort.guardar` es quien devuelve el `Usuario` con el id ya asignado. `refresh_tokens` se identifica por `token_hash` (SHA-256 del `jti`), no por el `jti` en claro.

**2026-09-25 — S3 agrega `@Entity`/adaptador para:** `specialties` (HU-009), `locations` de solo lectura (HU-006/010), `professionals` + `professional_specialties`/`professional_locations` vía `@ElementCollection` (HU-010/011), `availability_blocks` + `professional_slots` (HU-012/013), `appointments` + `appointment_statuses` de solo lectura (HU-014/015/016), `appointment_status_history` (HU-023, solo insert/lectura, sin update/delete — RN-12). La reserva atómica de slots (RN-01) es un `UPDATE professional_slots ... WHERE appointment_id IS NULL` en `ProfessionalSlotJpaRepository.reservarAtomicamente`, no una transacción con `SELECT` previo. `appointments` no tiene columna propia para el motivo de rechazo: vive en `appointment_status_history.reason`.

Antes de implementar una HU nueva (S4 en adelante), verifica si la tabla que necesita ya existe en `V1` (probablemente sí) y limítate a agregar el `@Entity`/adaptador correspondiente, no una migración de estructura nueva.

Los adaptadores JPA (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`, en `infrastructure/adapter/out/persistence/`) están anotados `@Profile("!test")`. Las pruebas usan dobles en memoria de `src/test/java/.../testsupport/` (importados vía `InMemoryPersistenceTestConfig`) porque no hay MySQL disponible para pruebas de integración en este entorno; si el estudiante tiene Docker, considera migrar esas pruebas a un perfil de integración real (p. ej. Testcontainers) en vez de mantener el doble en memoria indefinidamente.

**Lo único reservado al usuario/estudiante en este proyecto es el diseño visual** (Stitch/AI Studio, ver `AGENTS.md` raíz regla 11).

## Modo de trabajo

1. Localiza la HU aprobada (`docs/wiki/scrum/historias-de-usuario/`, estado `En desarrollo` o `Aprobada`) y su DoD.
2. Identifica reglas y contratos afectados (`docs/wiki/llm-wiki/wiki/contratos.md`).
3. Propón un plan antes de editar si el cambio cruza capas o repos.
4. Implementa el mínimo coherente con la arquitectura hexagonal.
5. Ejecuta `mvn test` (dominio + aplicación + integración MockMvc; ver `src/test/resources/application-test.yml` para el perfil sin base de datos real).
6. Verifica arquitectura y DoD; actualiza la matriz de evidencia de la HU.
7. Resume evidencia y deja explícito lo no verificado (p. ej., nada que dependa de MySQL real corriendo).

No mantengas una LLM Wiki propia: la wiki global la mantiene el agente orquestador en `docs/wiki/llm-wiki/` (raíz del workspace, `AGENTS.md`).
