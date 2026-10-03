# citas-api

Backend del sistema ficticio de agendamiento de citas (laboratorio FCV). Java 21 + Spring Boot 3.5.16 + Maven, arquitectura hexagonal, MySQL 8.4 + Flyway.

## Qué cubre

Todas las HU de backend del PRD (RF-01 a RF-20). Detalle por HU en `docs/wiki/scrum/` y contratos en `docs/wiki/llm-wiki/wiki/contratos.md`.

| Área | Endpoints principales |
|---|---|
| Autenticación | `POST /api/auth/register`, `login`, `refresh`, `logout`, `password-reset/request`, `password-reset/confirm` |
| Perfil y afiliación (USER) | `GET/PATCH /api/users/me`, `GET/PUT /api/users/me/afiliacion`, `GET /api/eps`, `GET /api/eps/{id}/plans` |
| Citas (USER) | `GET /api/availability`, `POST /api/appointments/general\|specialized`, `GET /api/appointments/mine`, `POST /api/appointments/{id}/cancel\|reschedule`, `GET /api/appointments/{id}/history` |
| Profesional | `GET/POST/PUT/DELETE /api/professionals/me/availability-blocks`, `GET /api/professionals/me/agenda`, `POST /api/appointments/{id}/complete\|no-show` |
| ADMIN | `/api/admin/appointments/**`, `/api/admin/reschedules/**`, `/api/admin/specialties/**`, `/api/admin/professionals/**` (incl. `PUT /{id}/assignments`), `/api/admin/eps/**` |
| n8n | `GET /api/integration/appointments/reminders`, `GET /api/integration/appointments/daily-summary` (cabecera `X-Integration-Key`) + webhook saliente a `N8N_WEBHOOK_URL` |

Autorización: JWT con roles `USER`/`PROFESSIONAL`/`ADMIN` (401 sin token, 403 con rol incorrecto, 404 fuera de ownership). `/api/integration/**` solo acepta la API key de n8n.

## Arquitectura hexagonal

```text
com.fcv.citas
├── domain/             entidades e invariantes (sin Spring/JPA)
├── application/        puertos (in/out) + casos de uso
└── infrastructure/
    ├── adapter/in/web/            controladores REST + DTOs
    ├── adapter/out/persistence/   JPA
    ├── adapter/out/integration/   consultas JDBC y webhook para n8n
    ├── adapter/out/security/      JWT, BCrypt
    └── config/                    Spring Security, filtros JWT y API key, ADMIN inicial
```

## Cómo correr y probar

Con el `.env.example` de este repo como referencia de variables. El entorno de desarrollo original usa el `docker-compose.yml` de la raíz del workspace (no incluido en este repo); también funciona con un MySQL 8.4 local (verificado con MySQL Community Server nativo en Windows). Con Docker:

```powershell
docker compose up -d                      # mysql + contenedores de desarrollo
docker exec -it fcv-citas-api-dev bash    # dentro: cd citas-api-develop
mvn test                                  # 161 pruebas (perfil "test": sin MySQL)
mvn spring-boot:run                       # aplica Flyway V1..V4 y levanta :8080
```

- **Datos de demostración:** `.\scripts\seed-demo.ps1` (raíz) carga ADMIN, 8 profesionales, 6 pacientes, disponibilidad y citas; contraseña `Demo1234*`. Correrlo después del primer arranque (Flyway ya aplicado).
- **ADMIN inicial sin datos demo:** definir `ADMIN_BOOTSTRAP_EMAIL` y `ADMIN_BOOTSTRAP_PASSWORD` (mín. 12 caracteres) en `.env`; se crea al arrancar solo si no existe ningún ADMIN.
- **Zona horaria:** el contenedor de la API debe correr con `TZ=America/Bogota` (ya está en `docker-compose.yml`); si no, Hibernate corre todas las fechas 5 horas.
- **n8n:** `N8N_API_KEY`, `N8N_WEBHOOK_URL`, `N8N_WEBHOOK_SECRET` (ver `.env.example`). Sin `N8N_WEBHOOK_URL` no se envían notificaciones.

Swagger UI: `http://localhost:8080/swagger-ui.html`.

> `mvn test` usa dobles en memoria. Cambios de persistencia, fechas o seguridad HTTP deben probarse además contra el stack real: ahí aparecieron bugs que las pruebas no detectaban (ver `AGENTS.md`, 2026-09-30).

## Documentación

- `AGENTS.md`: reglas y estado verificado del repo.
- `docs/wiki/scrum/`: épicas e historias de usuario.
- `docs/wiki/llm-wiki/`: wiki global del workspace (arquitectura, contratos, decisiones, riesgos, log).
- `docs/db-design/`: modelo 3FN.
- `automations/n8n/`: especificación de los workflows WF-001/002/003.

Lee `../PRD.md` y `../RESTRICCIONES_TECNICAS.md` antes de tocar código.
