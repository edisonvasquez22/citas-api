# Comparación contra `database/reference/db.sql`

> **2026-09-23 — Ver "Decisión" al final, actualizada.** El usuario pidió explícitamente que el esquema quedara exacto a esta referencia, incluyendo las partes ya implementadas (usuarios/refresh tokens). El análisis de esta página (hecho el 2026-09-17, antes de esa decisión) se conserva sin editar como registro histórico de por qué el diseño propio tomó cada camino distinto; ya no describe el estado actual del esquema, ver `MODELO_3FN.md`.

Escrito **después** de terminar `MODELO_3FN.md` y `V1__esquema_inicial.sql`, sin haber consultado antes la referencia (según pide `database/REQUISITOS_NORMALIZACION_3FN.md`).

## Coincidencias (validan el diseño propio)

- Mismas entidades centrales: `roles`/`user_roles`, `users`, `professionals` como extensión 1:1 de `users`, `professional_specialties`/`professional_sites` (llamadas `professional_locations` en la referencia) N:M, `eps`/`eps_plans`, `specialties` con duración 30/60, bloques de disponibilidad expandidos en slots atómicos, `appointment_status_history` solo-insert, `reschedule_requests`.
- Mismo mecanismo anti doble-reserva: slots atómicos con `appointment_id` nullable, reservados vía `UPDATE`/`JOIN` cuando se confirma una cita.
- Mismas dos sedes con la misma dirección pública (HIC/ICV).
- Mismo enfoque para "cita general": `specialties.is_general` en vez de un catálogo de tipo de cita aparte.

## Donde la referencia es más fuerte

| Punto | Referencia | Mío | Aprendizaje |
|---|---|---|---|
| Historial de afiliación | `user_insurance_affiliations` permite múltiples filas por usuario (`is_current`, `valid_from`/`valid_to`) y `appointments.insurance_affiliation_id` referencia la afiliación vigente **al momento de la cita** | `user_affiliations` con `PRIMARY KEY(user_id)`: solo la afiliación actual, sin historial | Mi lectura de "evitar duplicar EPS/plan/régimen" fue "una sola a la vez"; la referencia además preserva cuál cubría cada cita pasada. Si se necesita ese nivel de auditoría, valdría la pena migrar a una tabla con historial. |
| Retención de reprogramación | `reschedule_requests` guarda `requested_start_at`/`requested_end_at` como columnas sueltas, sin atar un slot concreto | `availability_slots.reschedule_request_id` retiene un slot real mientras la solicitud está `PENDING` | Aquí mi diseño es el más fuerte (ver abajo), lo incluyo también como fortaleza propia. |
| Metadato de estado terminal | `appointment_statuses.is_terminal` y `reschedule_request_statuses.is_terminal` | No existe; el "es terminal" quedaría hardcodeado en el código de aplicación | Mejora barata de adoptar en una migración futura (`V3`) si se quiere evitar listas de códigos hardcodeadas. |
| Trazabilidad de decisión del usuario tras rechazo | `reschedule_requests.patient_action_after_rejection` (`KEEP_APPOINTMENT`/`CANCEL_APPOINTMENT`) | No existe; se infiere indirectamente de `appointment_status_history` | Detalle útil para reportes; no es un RF explícito del PRD, así que no es un defecto, pero es un buen dato a considerar. |
| Auditoría rápida de aprobación | `appointments.created_by_user_id` / `approved_by_user_id` denormalizados | Solo vía `appointment_status_history` (hay que buscar la fila con `status=APPROVED`) | La referencia optimiza lectura a costa de un poco de redundancia; válido, pero no indispensable dado que RF-19 ya exige el historial completo. |

## Donde mi diseño toma un camino distinto (no necesariamente peor)

| Punto | Mío | Referencia | Justificación |
|---|---|---|---|
| PK de `users` | `CHAR(36)` UUID generado en el dominio antes de persistir | `BIGINT AUTO_INCREMENT` | El dominio hexagonal (`Usuario.registrarNuevo`) genera el id **antes** de llamar al repositorio (necesario para que el caso de uso no dependa de la infraestructura). Un autoincremental exigiría un `INSERT` primero y reasignar el id después, acoplando el dominio a la persistencia. Costo: claves más pesadas (36 bytes vs. 8) que se propagan a toda tabla hija. |
| Unicidad de solapamiento entre bloques | `availability_slots.professional_id` denormalizado + `UNIQUE(professional_id, start_at)`: **la base de datos** impide solapar slots del mismo profesional entre bloques distintos | `professional_slots` solo tiene `UNIQUE(availability_block_id, start_at)`: la unicidad es por bloque; evitar solapar bloques del mismo profesional queda enteramente a cargo de la validación de aplicación al crear el bloque | Documentado en `MODELO_3FN.md` como la única violación consciente de 3FN estricta del modelo. Es más estricto que la referencia (defensa en profundidad a nivel de base de datos), a costa de una dependencia transitiva justificada. |
| PK de `professionals` | `user_id` (mismo id que `users`, relación 1:1 real) | `id` autoincremental propio + `user_id UNIQUE` | Evita mantener dos espacios de identidad para la misma entidad ("un profesional es un usuario especializado", PRD sección 2); simplifica los FK de `professional_specialties`/`professional_sites`/`appointments`. |
| `requires_admin_approval` | No existe como columna; se deriva de `specialties.is_general` (`is_general = FALSE` ⇒ requiere ADMIN) | Columna explícita, redundante con `is_general` en los datos semilla actuales (siempre es la negación exacta) | Mantener una sola fuente de verdad evita que ambas columnas diverjan por error; si en el futuro una especialidad general requiriera aprobación igual, sí habría que separarlas — no es el caso hoy. |
| Semántica de `refresh_tokens.id` | `id = jti` del JWT (identificador no secreto embebido en el token firmado) | `id` autoincremental + `token_hash` (hash del token completo) | Ambos son válidos para invalidar sesiones. El de la referencia protege mejor si la tabla se filtra (no se puede reconstruir el token desde el hash); el mío es más simple porque el `jti` ya viaja firmado dentro del JWT y no permite falsificar un token nuevo por sí solo. Ver `docs/wiki/llm-wiki/wiki/riesgos.md` si se decide reforzarlo más adelante. |

## Decisión

**Original (2026-09-17):** se mantenía el modelo propio (`V1__esquema_inicial.sql`) como esquema vigente, con las 3 mejoras de la referencia registradas como opcionales para una migración futura.

**Reemplazada (2026-09-23), decisión explícita del usuario:** se adoptó el esquema de `database/reference/db.sql` **exacto**, incluyendo las partes que ya estaban implementadas y probadas (`users`, `refresh_tokens`) — no solo las tablas todavía sin código. Esto significa que las tres "mejoras opcionales" de esta página ya no son opcionales: se implementaron (`is_terminal`, historial de afiliación vía `user_insurance_affiliations`, `patient_action_after_rejection`), y además se adoptaron las diferencias de la fila "Donde mi diseño toma un camino distinto" — incluyendo perder la ventaja documentada ahí para `refresh_tokens.id`/`professional_specialties.is_primary`/retención de slot en reprogramación. Ver `MODELO_3FN.md` sección 8 para el resumen de lo que cambió y qué implicó en el código Java (`Usuario`, `UsuarioJpaAdapter`, `RefreshTokenJpaAdapter`, pruebas). Detalle completo de la decisión en `docs/wiki/llm-wiki/wiki/decisiones.md`, entrada 2026-09-23.
