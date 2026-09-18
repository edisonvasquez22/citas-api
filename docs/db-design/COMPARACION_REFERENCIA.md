# Comparación contra `database/reference/db.sql`

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

Se mantiene el modelo propio (`V1__esquema_inicial.sql`) como esquema vigente: cubre correctamente todos los RF/RN del PRD para el alcance ya aprobado (EP-001/EP-003 parcial) y el resto del backlog. Se registran como mejoras opcionales para una migración futura (`V3`, cuando se aborden las HU correspondientes en S3/S4):

1. `is_terminal` en `appointment_statuses` y `reschedule_statuses`.
2. Evaluar si `user_affiliations` necesita convertirse en tabla con historial (`valid_from`/`valid_to`) cuando se implemente EP-002 (HU-005), si se decide que las citas deben registrar qué afiliación las cubrió.
3. `patient_action_after_rejection` en `reschedule_requests` cuando se implemente HU-020 (EP-008), si se quiere ese detalle en reportes.

Ninguna de las tres es necesaria para que HU-001/HU-002/HU-006 (alcance S2) funcionen correctamente.
