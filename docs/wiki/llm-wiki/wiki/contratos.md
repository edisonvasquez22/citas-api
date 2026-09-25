---
tipo: wiki
actualizado: 2026-09-25
---

# Contratos REST — `citas-api` ↔ `citas-web`

Se actualiza junto con cada HU que agrega/cambia un endpoint (HU-024). Estado tras el incremento S3.

## Convenciones fijadas

- JSON sobre HTTP; sin Express/BFF de por medio.
- Errores de autenticación/autorización: `401` (sin token o token inválido/expirado) vs `403` (autenticado pero con un rol sin permiso, p. ej. USER contra `/api/admin/**`). **2026-09-25**: se agregó un `authenticationEntryPoint` explícito en `SecurityConfig` porque, sin él, Spring Security devolvía `403` también para requests sin token — ver `S3AuthorizationIntegrationTest`.
- Errores de negocio (email/documento duplicado, código/matrícula duplicados): `409`.
- Errores de "regla de negocio violada" (duración inválida, especialidad primaria duplicada, sede no habilitada, bloque solapado/pasado/comprometido, especialidad no asociada, motivo de rechazo faltante): `400`.
- Recurso inexistente (especialidad/profesional/bloque/cita): `404`.
- Conflicto de estado (horario perdido por concurrencia RN-01, transición de cita inválida): `409`.
- Errores de validación de entrada: `400`, con `ApiError.detalles` listando `campo: mensaje`.
- Todos los endpoints de `/api/auth/**` son públicos (`permitAll`); `/api/admin/**` exige rol `ADMIN`; `/api/professionals/me/**` exige rol `PROFESSIONAL`; el resto de la API autenticada acepta cualquier rol (`Authorization: Bearer <accessToken>`).

## Endpoints implementados (HU-001, HU-002)

| Método | Path | Body | Respuesta | Errores |
|---|---|---|---|---|
| POST | `/api/auth/register` | `AuthDtos.RegisterRequest` (nombres, apellidos, tipoDocumento, numeroDocumento, email, telefono, password) | `201` `AuthDtos.RegisterResponse` (usuarioId, email) | `409` email/documento duplicado; `400` validación |
| POST | `/api/auth/login` | `AuthDtos.LoginRequest` (email, password) | `200` `AuthDtos.TokenResponse` (accessToken, refreshToken) | `401` credenciales inválidas |
| POST | `/api/auth/refresh` | `AuthDtos.RefreshRequest` (refreshToken) | `200` `AuthDtos.TokenResponse` (nuevo access + nuevo refresh; rotación) | `401` refresh inválido/expirado/revocado |
| POST | `/api/auth/logout` | `AuthDtos.LogoutRequest` (refreshToken) | `204` sin cuerpo | Idempotente: nunca falla por token ya inválido |

## Endpoints implementados (HU-009 a HU-016, HU-023 — S3)

| Método | Path | Body | Respuesta | Errores | Autorización |
|---|---|---|---|---|---|
| GET | `/api/specialties` | — | `200` lista `EspecialidadDtos.Response` (solo activas) | — | autenticado (cualquier rol) |
| GET | `/api/professionals` | — | `200` lista `{profesionalId, nombreCompleto, especialidadIds, sedeIds}` | — | autenticado (cualquier rol) — **agregado 2026-09-25** al reconciliar la pantalla de agendar cita en `citas-web`: no había forma de que un paciente viera el nombre de un profesional (solo existía `/api/admin/professionals`, ADMIN-only) |
| GET | `/api/admin/specialties` | — | `200` lista `Response` (todas) | — | ADMIN |
| POST | `/api/admin/specialties` | `CrearRequest` (codigo, nombre, duracionMinutos, general, requiereAprobacionAdmin) | `201` `Response` | `400` duración≠30/60 o código duplicado | ADMIN |
| PUT | `/api/admin/specialties/{id}` | `EditarRequest` | `200` `Response` | `400`/`404` | ADMIN |
| PATCH | `/api/admin/specialties/{id}/status` | `{activa}` | `200` `Response` | `404` | ADMIN |
| POST | `/api/admin/professionals` | `RegistrarRequest` (datos de cuenta + codigoProfesional, matricula, especialidades[], sedeIds[]) | `201` `Response` (profesionalId, usuarioId, activo) | `409` email/documento/código/matrícula duplicados; `400` especialidad inactiva/sede inexistente/primaria duplicada | ADMIN |
| PATCH | `/api/admin/professionals/{id}/status` | `{activo}` | `200` `Response` | `404` | ADMIN |
| GET | `/api/professionals/me/availability-blocks` | — | `200` lista `BloqueDisponibilidadDtos.Response` | — | PROFESSIONAL |
| POST | `/api/professionals/me/availability-blocks` | `CrearRequest` (sedeId, fecha, horaInicio, horaFin) | `201` `Response` | `400` en el pasado/solapado/sede no habilitada; `404` profesional | PROFESSIONAL |
| PUT | `/api/professionals/me/availability-blocks/{id}` | `EditarRequest` | `200` `Response` | `400` (igual que crear, + bloque comprometido); `404` | PROFESSIONAL |
| DELETE | `/api/professionals/me/availability-blocks/{id}` | — | `204` | `400` bloque comprometido; `404` | PROFESSIONAL |
| GET | `/api/availability?especialidadId=&fecha=&sedeId=&profesionalId=` | — | `200` lista `HorarioResponse` (profesionalId, sedeId, inicio, fin) | `400` filtros incompletos/especialidad inactiva; `404` especialidad | autenticado |
| POST | `/api/appointments/general` | `SolicitarRequest` (profesionalId, sedeId, especialidadId, motivo, fecha, horaInicio) | `201` `Response` (citaId, estado=APPROVED, inicio, fin) | `409` horario no disponible; `400` regla de negocio; `404` | autenticado |
| POST | `/api/appointments/specialized` | `SolicitarRequest` | `201` `Response` (estado=REQUESTED) | `409`/`400`/`404` (igual que general) | autenticado |
| GET | `/api/admin/appointments/requested?sedeId=&profesionalId=&especialidadId=&fecha=` | — | `200` lista `Resumen` | — | ADMIN |
| POST | `/api/admin/appointments/{id}/approve` | — | `200` `Resumen` (estado=APPROVED) | `409` no estaba REQUESTED; `404` | ADMIN |
| POST | `/api/admin/appointments/{id}/reject` | `{motivo}` | `200` `Resumen` (estado=REJECTED, motivoDecision) | `400` sin motivo; `409` no estaba REQUESTED; `404` | ADMIN |

Notas de diseño relevantes para quien consuma esto desde `citas-web`:

- La reserva de horario (general/especializada) es **atómica**: si el horario se lo llevó otro justo antes de confirmar, la API responde `409` (`HorarioNoDisponibleException`), no un error genérico — el frontend debe interpretar `409` en estos dos endpoints como "vuelve a consultar disponibilidad".
- El motivo de rechazo de una cita especializada (HU-016) **no se re-consulta después** por un endpoint propio en S3 (no hay HU-017 "mis citas" todavía): solo viaja en la respuesta inmediata de `POST .../reject`. Queda auditado permanentemente en `appointment_status_history` (HU-023), pero su lectura expuesta por API es trabajo de una HU futura.

Fuente de verdad detallada: `citas-api/src/main/java/com/fcv/citas/infrastructure/adapter/in/web/`. Swagger UI (springdoc 2.8.17) disponible en `/swagger-ui.html` una vez el backend esté corriendo.

## Pendiente

- El resto de los endpoints del backlog (EP-002, EP-008 a EP-011), a medida que se implementen en S4.
- Exponer lectura del historial de auditoría (HU-023) desde HU-017/HU-021 cuando existan.
- Publicar el JSON/YAML de OpenAPI exportado como artefacto versionado (opcional; hoy se sirve dinámico vía springdoc).

## Relacionado

[[arquitectura]] · [[dominio]]
