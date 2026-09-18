---
id: HU-006
tipo: historia-de-usuario
titulo: "Precarga de catálogos fijos"
estado: "En desarrollo"
epica: "[[EP-003-catalogos-del-sistema]]"
esfuerzo: "Bajo"
sprint_sugerido: "Sprint 1 (S2)"
dependencias: []
relacionadas:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-login-y-sesion-jwt]]"
---

# HU-006 — Precarga de catálogos fijos

## Historia de usuario

**COMO** sistema
**QUIERO** tener precargados los catálogos fijos (roles, estados de cita, estados de reprogramación, regímenes, sedes) al arrancar
**PARA** que el resto de funcionalidades pueda operar sin configuración manual previa

> Como sistema, quiero tener precargados los catálogos fijos para que el resto de funcionalidades pueda operar sin configuración manual previa.

## Contexto y descripción

RF-05. Es la primera pieza fundacional del backlog: casi toda otra HU depende de que exista al menos el catálogo de roles (`USER`, `PROFESSIONAL`, `ADMIN`) y, más adelante, estados de cita/reprogramación y sedes.

## Alcance

- Migración Flyway de seed (datos, no estructura de negocio) con: roles fijos, estados de cita fijos, estados de reprogramación fijos, regímenes fijos, las dos sedes fijas (HIC e ICV con su dirección pública).
- Los catálogos fijos son de solo lectura vía API (sin CRUD de escritura).

## Fuera de alcance

- Catálogos configurables (EPS, planes, especialidades): ver [[HU-007-administrar-catalogo-eps]], [[HU-008-administrar-catalogo-planes-eps]], [[HU-009-administrar-catalogo-especialidades]].
- La estructura final de tablas de estos catálogos: depende del diseño 3FN que entregue el usuario.

## Reglas de negocio

- Estos catálogos son de solo lectura; no se exponen endpoints de escritura sobre ellos.
- No se permite borrar físicamente un catálogo referenciado por transacciones.

## Dependencias y relaciones

- Épica: [[EP-003-catalogos-del-sistema]]
- Dependencias: ninguna (es la HU más fundacional del backlog).
- Relacionadas: [[HU-001-registrar-usuario]], [[HU-002-login-y-sesion-jwt]] (ambas requieren el catálogo de roles).

## Esfuerzo

**Nivel:** Bajo

**Justificación de dificultad:** es una migración de datos semilla sobre tablas de catálogo simples, sin reglas de negocio complejas.

## Tareas de desarrollo

- [ ] **T-01 — Migración Flyway de seed de catálogos fijos**
  Dificultad: Bajo
  Descripción: depende de que el esquema de estas tablas exista según el diseño 3FN aprobado por el usuario; ejecuta el `INSERT` semilla sobre ese esquema.
- [ ] **T-02 — Endpoints de solo lectura de catálogos fijos** (si el frontend los necesita para poblar selects)
  Dificultad: Bajo
  Descripción: `GET /api/catalogos/roles`, `/estados-cita`, `/estados-reprogramacion`, `/regimenes`, `/sedes`.
- [ ] **T-03 — Pruebas**
  Dificultad: Bajo
  Descripción: verificar que los catálogos existen tras migrar y que los endpoints de lectura responden los valores esperados.

## Criterios de aceptación

### CA-01 — Catálogos disponibles tras migrar

**Dado** una base de datos recién migrada
**Cuando** se consulta cualquiera de los catálogos fijos
**Entonces** existen al menos los valores mínimos requeridos por el PRD (roles `USER`/`PROFESSIONAL`/`ADMIN`; sedes HIC/ICV; los estados de cita y de reprogramación usados en el resto del backlog).

### CA-02 — Catálogos de solo lectura

**Dado** cualquier actor autenticado o no
**Cuando** intenta modificar un catálogo fijo por API
**Entonces** no existe ningún endpoint que lo permita (los catálogos fijos no exponen escritura).

## Definition of Done

- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] Migración Flyway de seed presente y coherente con el esquema 3FN aprobado.
- [ ] `mvn test` pasa para los módulos afectados.
- [ ] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | No verificable | `V2__seed_catalogos_fijos.sql` (roles, sites, regimes, appointment_statuses, reschedule_statuses, slot_statuses) | Migración de seed implementada con todos los catálogos fijos del RF-05. No verificable porque no se pudo ejecutar Flyway contra MySQL real en este entorno. |
| CA-02 | Cumple | Inspección de código: no existe ningún `@RestController` de catálogos en `infrastructure/adapter/in/web/` | Verificable por inspección sin necesidad de ejecución: hoy no hay ningún endpoint de catálogos, ni de lectura ni de escritura. |
| DoD-01 | No verificable | `V1__esquema_inicial.sql` + `V2__seed_catalogos_fijos.sql` | Migraciones presentes y coherentes con `docs/db-design/MODELO_3FN.md`; no verificable en ejecución real por falta de MySQL/JDK/Maven en este entorno. |

## Historial de validación

- 2026-09-17 — HU creada en estado `Pendiente de aprobación`, propuesta como prerrequisito técnico del incremento S2.
- 2026-09-17 — Usuario aprueba explícitamente el alcance de S2. Estado pasa a `En desarrollo`.
- 2026-09-17 — Desbloqueo parcial: se modeló `RolNombre` como enum de dominio para permitir que HU-001/HU-002 avanzaran sin esperar el esquema.
- 2026-09-17 — Usuario aclara que el diseño de datos también lo implementa el agente. Completado: `V1__esquema_inicial.sql` crea las tablas de catálogo (`roles`, `sites`, `regimes`, `appointment_statuses`, `reschedule_statuses`, `slot_statuses`) y `V2__seed_catalogos_fijos.sql` las precarga (RF-05). `UsuarioJpaAdapter` ya consulta `roles` vía `RolJpaRepository` al registrar un usuario. Sigue sin existir un endpoint de lectura de catálogos (no era parte del alcance mínimo de esta HU; agregar si el frontend lo necesita para poblar selects).

## Notas y decisiones

- El enum de dominio `RolNombre` sigue siendo la fuente de verdad en código (`RolNombre.name()` debe coincidir con `roles.code`); si se agrega un rol nuevo hay que actualizar ambos lugares.
- No se agregó un endpoint `GET /api/catalogos/**` porque ninguna HU aprobada lo necesita todavía; se agregará cuando `citas-web` lo requiera (p. ej. al construir el formulario de registro/afiliación).
