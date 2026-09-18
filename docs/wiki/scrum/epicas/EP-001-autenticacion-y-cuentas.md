---
id: EP-001
tipo: epica
titulo: "Autenticación y gestión de cuentas"
estado: Borrador
historias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-login-y-sesion-jwt]]"
  - "[[HU-003-recuperar-contrasena]]"
dependencias:
  - "[[EP-003-catalogos-del-sistema]]"
---

# EP-001 — Autenticación y gestión de cuentas

## Objetivo

Permitir que cualquier visitante cree una cuenta `USER`, inicie sesión de forma segura y pueda recuperar su acceso, estableciendo el contexto de rol/autorización que usa el resto del sistema.

## Valor esperado

Sin esta épica ningún actor (USER, PROFESSIONAL, ADMIN) puede usar el sistema. Es el cimiento de autorización de todo el producto.

## Actores

- USER (se autorregistra e inicia sesión).
- PROFESSIONAL y ADMIN (cuentas creadas por ADMIN, pero autentican por el mismo mecanismo).

## Alcance

- Registro de cuenta `USER` (RF-01).
- Login con emisión de access + refresh token, refresh y logout/revocación (RF-02).
- Recuperación de contraseña con token temporal de un solo uso (RF-03).

## Fuera de alcance

- Creación de cuentas `PROFESSIONAL`/`ADMIN` (ver [[EP-004-gestion-de-profesionales]]).
- Perfil extendido y afiliación (ver [[EP-002-perfil-y-afiliacion]]).

## Reglas de negocio

- Contraseñas nunca se almacenan en texto plano (hash adaptativo).
- Email y número de documento son únicos.
- Los roles forman parte del contexto de autorización del token.
- El token de recuperación es temporal y de un solo uso; cambiar contraseña lo invalida/consume.

## Dependencias

- [[HU-006-precarga-de-catalogos-fijos]] (el rol `USER` debe existir como catálogo fijo antes de poder registrar cuentas).

## Historias de usuario

- [[HU-001-registrar-usuario]]
- [[HU-002-login-y-sesion-jwt]]
- [[HU-003-recuperar-contrasena]]

## Criterio de completitud de la épica

- [ ] HU-001, HU-002 y HU-003 están `Completada`.
- [ ] No quedan dependencias bloqueantes dentro del alcance de la épica.

## Riesgos e incógnitas

- Estrategia exacta de revocación/rotación de refresh token pendiente de decidir en `HU-002` (ver `llm-wiki/wiki/riesgos.md`).
