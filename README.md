# citas-api

Backend del sistema ficticio de agendamiento de citas (laboratorio FCV). Java 21 + Spring Boot 3.5.16 + Maven, arquitectura hexagonal.

## Estado actual (S2)

Implementado como primer incremento (HU-001, HU-002; ver `docs/wiki/scrum/`):

- Registro de usuario (`POST /api/auth/register`) con unicidad de email/documento y password hasheado (BCrypt).
- Login (`POST /api/auth/login`) con emisión de access + refresh token (JWT, `io.jsonwebtoken:jjwt` 0.13.0).
- Renovación de sesión (`POST /api/auth/refresh`) con rotación de refresh token.
- Logout (`POST /api/auth/logout`) que revoca el refresh token de la sesión.
- Spring Security stateless con filtro JWT propio (sin `AuthenticationManager`/`UserDetailsService`).
- MySQL + Flyway con esquema real: `V1__esquema_inicial.sql` (modelo 3FN completo del PRD) + `V2__seed_catalogos_fijos.sql` (roles, sedes, regímenes, estados — RF-05/HU-006).
- Persistencia real de usuarios/roles/refresh tokens vía JPA (`UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`).

### Diseño de datos

El modelo relacional 3FN (todas las tablas del PRD, no solo autenticación) está en `docs/db-design/MODELO_3FN.md` (diagrama ER, dependencias funcionales, justificación 1FN→2FN→3FN) y `docs/db-design/COMPARACION_REFERENCIA.md` (comparación contra `database/reference/db.sql`). Diseñado por el agente por decisión explícita del usuario (2026-09-17): la única actividad reservada al usuario en este proyecto es el **prototipado visual** (Stitch/AI Studio).

Solo hay `@Entity`/adaptador JPA para lo que HU-001/HU-002/HU-006 necesitan hoy (usuarios, roles, refresh tokens); el resto de las tablas (profesionales, agenda, citas, EPS...) existen en el esquema y se implementan a medida que sus HU se aprueben (S3/S4).

## Arquitectura hexagonal

```text
com.fcv.citas
├── domain/            entidades e invariantes (sin Spring/JPA)
├── application/        puertos (in/out) + casos de uso
└── infrastructure/
    ├── adapter/in/web/          controladores REST + DTOs
    ├── adapter/out/security/    JWT (JjwtTokenProviderAdapter), BCrypt
    ├── adapter/out/persistence/ JPA real: usuarios/roles/refresh tokens
    └── config/                  Spring Security + filtro JWT
```

## Cómo correr y probar

Este repo asume Java 21 + Maven + MySQL (ver `docker-compose.yml` en la raíz del workspace, servicio `citas-api-dev` + `mysql`, o una instalación local):

```powershell
docker compose up -d mysql   # o una instancia MySQL 8.4 local
mvn test          # pruebas de dominio, aplicación e integración (perfil "test": sin MySQL, ver src/test/resources/application-test.yml)
mvn spring-boot:run   # requiere MySQL corriendo y variables de entorno (.env); aplica V1/V2 automáticamente vía Flyway
```

Swagger UI una vez levantado: `http://localhost:8080/swagger-ui.html`.

**Estado verificado (2026-09-18):** con JDK 21 (Temurin) y Maven 3.9.16 instalados, `mvn test` da `BUILD SUCCESS` con **15/15 pruebas**. Sigue sin verificarse `mvn spring-boot:run` contra MySQL real (falta Docker en esta máquina) — cuando lo levantes, revisa que Flyway aplique `V1`/`V2` sin errores.

## Documentación compartida

- `docs/wiki/scrum/`: épicas/HU (11 épicas, 24 HU).
- `docs/wiki/llm-wiki/`: LLM Wiki global del workspace.
- `automations/n8n/`: JSON exportados en S5/S6 (todavía no aplica).

Lee `../PRD.md` y `../RESTRICCIONES_TECNICAS.md` antes de tocar código.
