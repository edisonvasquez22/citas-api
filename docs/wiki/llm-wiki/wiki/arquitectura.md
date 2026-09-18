---
tipo: wiki
actualizado: 2026-09-17
fuente: "[[../raw/RESTRICCIONES_TECNICAS.md]]"
---

# Arquitectura — `citas-api`

## Stack

Java 21 LTS · Spring Boot 3.5.x · Maven · Spring Data JPA · MySQL 8.4 · Flyway · Spring Security + JWT access/refresh · REST/JSON · Actuator (recomendado).

Ver [[decisiones]] para las versiones exactas fijadas y su fuente de verificación.

## Paquetes hexagonales

```text
com.fcv.citas
├── domain/            entidades e invariantes de negocio, SIN dependencias de Spring/JPA/HTTP
│   ├── model/
│   └── exception/
├── application/        casos de uso + puertos (interfaces)
│   ├── port/in/         casos de uso expuestos (comandos/consultas)
│   ├── port/out/        dependencias hacia infraestructura (repos, hasher, token provider)
│   └── usecase/         implementación de los casos de uso
└── infrastructure/
    ├── adapter/in/web/     controladores REST, DTOs, mapeadores
    ├── adapter/out/persistence/  entidades JPA, repositorios Spring Data, mapeadores dominio↔JPA
    ├── config/              seguridad, CORS, beans
    └── security/            JWT provider, filtros, UserDetails
```

Reglas:
- El dominio no importa `org.springframework.*` ni `jakarta.persistence.*`.
- Los casos de uso dependen de puertos (`interface`), nunca de adaptadores concretos.
- Los controladores traducen HTTP; no concentran reglas de negocio.
- Cambios de esquema requieren migración Flyway y justificación registrada en la HU correspondiente.

## Base de datos

Modelo 3FN diseñado e implementado por el agente (decisión del usuario del 2026-09-17, ver [[decisiones]]): diagrama ER, dependencias funcionales y justificación en `citas-api/docs/db-design/MODELO_3FN.md`; comparación contra `database/reference/db.sql` en `citas-api/docs/db-design/COMPARACION_REFERENCIA.md`. Esquema en `V1__esquema_inicial.sql`, seed de catálogos fijos en `V2__seed_catalogos_fijos.sql`.

## Seguridad mínima

BCrypt/Argon2 para passwords, secretos solo por variables de entorno, JWT access/refresh separados, autorización por rol + ownership, CORS explícito, validación server-side, sin logging de passwords/tokens.

## Relacionado

[[dominio]] · [[contratos]] · [[decisiones]]
