---
tipo: wiki
actualizado: 2026-09-29
---

# Contratos REST — `citas-api` ↔ `citas-web`

Se actualiza junto con cada HU que agrega/cambia un endpoint (HU-024). Estado tras el incremento S4 (backend).

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
| GET | `/api/admin/professionals` | — | `200` lista `AdminListResponse` (incluye inactivos, datos de contacto reales) | — | ADMIN — **agregado 2026-09-28**, no existía forma de listar profesionales inactivos para HU-011 |
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
- El motivo de rechazo de una cita especializada (HU-016) no vive en `appointments`: se resuelve en `GET /api/appointments/mine` (HU-017) consultando `appointment_status_history` por detrás — el frontend no necesita reconstruirlo, ya viene en `motivoDecision`.

## Endpoints implementados (HU-017 a HU-022 — S4, backend)

| Método | Path | Body | Respuesta | Errores | Autorización |
|---|---|---|---|---|---|
| GET | `/api/appointments/mine?estado=&fecha=` | — | `200` lista `MiCitaResponse` (sedeId, profesionalId, especialidadId, estado, inicio, fin, motivoDecision, reprogramacion) | — | autenticado; ownership estricto (solo las propias) |
| POST | `/api/appointments/{id}/cancel` | — | `200` `CierreResponse` (citaId, estado=CANCELLED) | `409` cita ya terminal; `400` cita pasada; `404` no es propia | autenticado |
| POST | `/api/appointments/{id}/reschedule` | `ReprogramarRequest` (sedeId, fecha, horaInicio) | `200` `ReprogramarResponse` (solicitudId, citaId, estado=PENDING, inicioSolicitado, finSolicitado) | `400` cita no APPROVED/no futura/sede no habilitada; `409` nuevo horario no disponible; `404` no es propia | autenticado |
| GET | `/api/admin/reschedules` | — | `200` lista `Resumen` (solicitudes PENDING) | — | ADMIN |
| POST | `/api/admin/reschedules/{id}/approve` | — | `200` `Resumen` (estado=APPROVED) | `409` no estaba PENDING; `404` | ADMIN |
| POST | `/api/admin/reschedules/{id}/reject` | `{motivo}` | `200` `Resumen` (estado=REJECTED, motivoDecision) | `400` sin motivo; `409` no estaba PENDING; `404` | ADMIN |
| GET | `/api/professionals/me/agenda?sedeId=&desde=&hasta=` | — | `200` lista `CitaAgendaResponse` (citaId, pacienteUsuarioId, sedeId, especialidadId, inicio, fin) — solo `APPROVED` propias | — | PROFESSIONAL |
| POST | `/api/appointments/{id}/complete` | — | `200` `CierreResponse` (estado=COMPLETED) | `400` no APPROVED/no pasada; `404` no es del profesional | autenticado (ownership por profesional) |
| POST | `/api/appointments/{id}/no-show` | — | `200` `CierreResponse` (estado=NO_SHOW) | `400`/`404` (igual que complete) | autenticado (ownership por profesional) |

Notas de diseño de HU-019/HU-020 (reprogramación): durante el `PENDING`, la franja antigua y la nueva quedan **ambas** asignadas al mismo `citaId` en `professional_slots` (RN-10: la cita original no se toca hasta la decisión). `GestionarReprogramacionesService` las distingue por horario (`SlotRepositoryPort.listarIdsDeCitaEnRango`) para liberar solo la que corresponda según la decisión — el frontend no necesita saber esto, solo interpretar los estados `PENDING`/`APPROVED`/`REJECTED` de la solicitud.

`MiCitaResponse.reprogramacion` (agregado 2026-09-29 tras `LOOP_02_GUIADO_AVANZADO.md`, ver `HU-019-solicitar-reprogramacion.md`) trae la **última** solicitud de reprogramación conocida para esa cita (cualquiera sea su estado), o `null` si nunca se pidió una: `{ solicitudId, estado, inicioSolicitado, finSolicitado, motivoDecision }`. Es la única forma en que el USER conoce el desenlace de su propia solicitud — el `estado` de la cita misma no cambia mientras la reprogramación está `PENDING` (RN-10), así que sin este campo el paciente no tenía manera de ver que su solicitud fue aprobada o rechazada (ni el motivo) una vez decidida.

Fuente de verdad detallada: `citas-api/src/main/java/com/fcv/citas/infrastructure/adapter/in/web/`. Swagger UI (springdoc 2.8.17) disponible en `/swagger-ui.html` una vez el backend esté corriendo.

## Pendiente

- El resto de los endpoints del backlog (EP-002, EP-008 a EP-011: perfil/afiliación, catálogo EPS/planes, recuperar contraseña) — fuera del alcance de S4 según `GUIA_SESIONES_S2_S6.md`, quedan para una aprobación aparte.
- UI en `citas-web` para HU-017 a HU-022 (backend implementado, frontend pendiente — mismo patrón que S3: diseño visual reservado al usuario vía Stitch/AI Studio).
- Publicar el JSON/YAML de OpenAPI exportado como artefacto versionado (opcional; hoy se sirve dinámico vía springdoc).

## Relacionado

[[arquitectura]] · [[dominio]]
