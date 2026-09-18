---
tipo: wiki
actualizado: 2026-09-17
---

# Contratos REST — `citas-api` ↔ `citas-web`

Se actualiza junto con cada HU que agrega/cambia un endpoint (HU-024). Estado tras el incremento S2.

## Convenciones fijadas

- JSON sobre HTTP; sin Express/BFF de por medio.
- Errores de autenticación/autorización: `401` (credenciales/token inválido) vs `403` (autenticado sin permiso — todavía sin casos de uso que lo disparen).
- Errores de negocio (email/documento duplicado): `409`.
- Errores de validación de entrada: `400`, con `ApiError.detalles` listando `campo: mensaje`.
- Todos los endpoints de `/api/auth/**` son públicos (`permitAll` en `SecurityConfig`); el resto de la API (todavía inexistente) requiere `Authorization: Bearer <accessToken>`.

## Endpoints implementados (HU-001, HU-002)

| Método | Path | Body | Respuesta | Errores |
|---|---|---|---|---|
| POST | `/api/auth/register` | `AuthDtos.RegisterRequest` (nombres, apellidos, tipoDocumento, numeroDocumento, email, telefono, password) | `201` `AuthDtos.RegisterResponse` (usuarioId, email) | `409` email/documento duplicado; `400` validación |
| POST | `/api/auth/login` | `AuthDtos.LoginRequest` (email, password) | `200` `AuthDtos.TokenResponse` (accessToken, refreshToken) | `401` credenciales inválidas |
| POST | `/api/auth/refresh` | `AuthDtos.RefreshRequest` (refreshToken) | `200` `AuthDtos.TokenResponse` (nuevo access + nuevo refresh; rotación) | `401` refresh inválido/expirado/revocado |
| POST | `/api/auth/logout` | `AuthDtos.LogoutRequest` (refreshToken) | `204` sin cuerpo | Idempotente: nunca falla por token ya inválido |

Fuente de verdad detallada: `citas-api/src/main/java/com/fcv/citas/infrastructure/adapter/in/web/`. Swagger UI (springdoc 2.8.17) disponible en `/swagger-ui.html` una vez el backend esté corriendo.

## Pendiente

- El resto de los 20 endpoints del backlog (EP-002 a EP-011), a medida que se implementen.
- Publicar el JSON/YAML de OpenAPI exportado como artefacto versionado (opcional; hoy se sirve dinámico vía springdoc).

## Relacionado

[[arquitectura]] · [[dominio]]
