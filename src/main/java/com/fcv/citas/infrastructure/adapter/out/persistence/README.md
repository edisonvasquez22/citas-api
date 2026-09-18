# Adaptadores de persistencia

Implementados sobre el modelo 3FN de `citas-api/docs/db-design/MODELO_3FN.md`:

- `RolJpaEntity` / `RolJpaRepository` — catálogo `roles` (RF-05).
- `UsuarioJpaEntity` / `UsuarioJpaRepository` / `UsuarioJpaAdapter` — `users` + `user_roles` (RF-01, RF-02).
- `RefreshTokenJpaEntity` / `RefreshTokenJpaRepository` / `RefreshTokenJpaAdapter` — `refresh_tokens` (RF-02).

`UsuarioJpaAdapter` y `RefreshTokenJpaAdapter` están anotados `@Profile("!test")`: en el perfil `test` (ver `application-test.yml`, que excluye datasource/JPA/Flyway porque no hay MySQL disponible en ese entorno) se usan en su lugar los dobles de prueba en `src/test/java/com/fcv/citas/testsupport/`, importados por `AuthFlowIntegrationTest` vía `InMemoryPersistenceTestConfig`.

El resto de las tablas del modelo (profesionales, agenda, citas, EPS, etc.) existen en `V1__esquema_inicial.sql` pero todavía no tienen `@Entity`/adaptador: se agregan cuando su HU correspondiente (EP-002 en adelante) se apruebe.
