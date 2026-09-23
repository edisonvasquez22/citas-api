# AGENTS.md — `citas-api`

> Generado a partir de `../prompts/agents/PROMPT_AGENT_CITAS_API.md` una vez inicializado el proyecto Spring Boot real (2026-09-17). Reemplaza a `AGENTS.md.template`. Revisado/depurado contra evidencia real del repo el 2026-09-21 (sin cambios de código desde el 17; solo se confirmó vigencia).

## Responsabilidad de este repo

- Java 21, Spring Boot 3.5.16, Maven.
- Arquitectura hexagonal (`domain` / `application` / `infrastructure`).
- REST/JSON.
- Spring Security + JWT access/refresh (implementado en HU-002 con rotación de refresh token).
- MySQL 8.4 + Spring Data JPA + Flyway, con esquema real (`V1__esquema_inicial.sql`, `V2__seed_catalogos_fijos.sql`; ver `docs/db-design/MODELO_3FN.md`).
- Reglas de negocio del PRD, HU por HU.
- Pruebas de dominio, aplicación e integración.

**Estado verificado (2026-09-21):** `mvn test` da `BUILD SUCCESS` con **15/15 pruebas** (dominio + aplicación + integración `AuthFlowIntegrationTest`), corrido repetidas veces con JDK 21 + Maven 3.9.16 reales. `GOAL_01_GUIADO_SIMPLE.md` ejecutado como verificación formal de HU-001/HU-002: **PASS** (ver `docs/wiki/scrum/historias-de-usuario/HU-002-login-y-sesion-jwt.md`). Sigue sin verificarse contra MySQL real (Docker instalado pero no operativo todavía) — todo lo anterior corre con los dobles en memoria de pruebas.

## Reglas arquitectónicas (verificadas contra el código real)

- El dominio (`domain/model`, `domain/exception`) no importa `org.springframework.*` ni `jakarta.persistence.*`. Ver `Usuario.java`.
- Los casos de uso viven en `application/usecase` e implementan interfaces de `application/port/in`; dependen solo de puertos (`application/port/out`), nunca de adaptadores concretos.
- `infrastructure/adapter/in/web` traduce HTTP (DTOs + `AuthController`); no concentra reglas de negocio.
- `infrastructure/adapter/out/**` son los únicos lugares con dependencias de Spring Security/JWT/persistencia.
- No acoplar este backend a React/Angular. No editar `citas-web` desde este agente.
- Cambios de esquema requieren migración Flyway y justificación en la HU correspondiente.
- Secretos solo por variables de entorno (`JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `DB_*`); nunca hardcodeados ni logueados.

## Diseño de datos

El modelo 3FN (`docs/db-design/MODELO_3FN.md`, comparado contra la referencia en `COMPARACION_REFERENCIA.md`) lo diseña este agente por decisión explícita del usuario (2026-09-17). `V1__esquema_inicial.sql` ya cubre **todas** las tablas del PRD; no lo regeneres ni lo edites una vez aplicado contra una base real — agrega `V3__...sql` para cualquier cambio.

Solo hay `@Entity`/repositorio/adaptador JPA para lo que las HU aprobadas necesitan (`users`, `roles`, `refresh_tokens` — HU-001/HU-002/HU-006). Antes de implementar una HU nueva (EP-002 en adelante), verifica si la tabla que necesita ya existe en `V1` (probablemente sí) y limítate a agregar el `@Entity`/adaptador correspondiente, no una migración de estructura nueva.

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
