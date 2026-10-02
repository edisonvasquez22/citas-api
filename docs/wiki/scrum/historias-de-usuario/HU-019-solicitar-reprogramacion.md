---
id: HU-019
tipo: historia-de-usuario
titulo: "Solicitar reprogramación de cita"
estado: Terminada
epica: "[[EP-008-mis-citas-cancelacion-reprogramacion]]"
esfuerzo: "Alto"
sprint_sugerido: "Sprint 3 (S4)"
dependencias:
  - "[[HU-017-consultar-mis-citas]]"
  - "[[HU-013-consultar-disponibilidad]]"
relacionadas:
  - "[[HU-020-aprobar-rechazar-reprogramacion]]"
---

# HU-019 — Solicitar reprogramación de cita

## Historia de usuario

**COMO** USER autenticado
**QUIERO** solicitar una nueva fecha/hora para una cita ya aprobada
**PARA** ajustar mi cita sin perderla mientras se decide

> Como USER autenticado, quiero solicitar una reprogramación sin perder mi cita original mientras se decide.

## Contexto y descripción

RF-15. Solo una cita `APPROVED` y futura puede solicitar reprogramación; conserva profesional y especialidad. La cita original conserva su franja hasta que ADMIN decida (RN-10).

## Alcance

- `POST /api/appointments/{id}/reschedule`: USER elige nueva fecha/hora disponible para el mismo profesional/especialidad; la nueva franja se retiene mientras la solicitud está `PENDING`.

## Fuera de alcance

- Cambiar de profesional (se trata como una cita nueva, no como reprogramación).
- La decisión de ADMIN (ver [[HU-020-aprobar-rechazar-reprogramacion]]).

## Reglas de negocio

- Solo cita `APPROVED` y futura es reprogramable.
- Conserva profesional y especialidad.
- La nueva franja se retiene mientras la solicitud está `PENDING` (RN-01); la cita original no se toca hasta la decisión (RN-10).
- Una cita no puede tener dos solicitudes de reprogramación `PENDING` a la vez (regla agregada 2026-09-29, ver LOOP_03 más abajo).

## Dependencias y relaciones

- Épica: [[EP-008-mis-citas-cancelacion-reprogramacion]]
- Dependencias: [[HU-017-consultar-mis-citas]], [[HU-013-consultar-disponibilidad]]
- Relacionadas: [[HU-020-aprobar-rechazar-reprogramacion]]

## Esfuerzo

**Nivel:** Alto

**Justificación de dificultad:** coexisten dos reservas (original vigente + nueva retenida) sin que una invalide a la otra hasta la decisión administrativa.

## Tareas de desarrollo

- [x] **T-01 — Caso de uso `SolicitarReprogramacion`**
  Dificultad: Alto
  Descripción: valida estado `APPROVED`/futuro, mismo profesional/especialidad, retiene la nueva franja sin tocar la original.
- [x] **T-02 — Endpoint REST**
  Dificultad: Alto
  Descripción: `reschedule_requests`/`reschedule_request_statuses` ya existen en `V1__esquema_inicial.sql` (esquema de referencia adoptado 2026-09-23) — no hizo falta migración nueva, solo el `@Entity`/adaptador correspondiente (`RescheduleRequestJpaEntity`/`SolicitudReprogramacionJpaAdapter`).
- [x] **T-03 — Pruebas**
  Dificultad: Alto
  Descripción: solicitud válida, cita no `APPROVED`, cita pasada, nuevo horario ya ocupado, verificación de que la cita original sigue intacta mientras está `PENDING`.

## Criterios de aceptación

### CA-01 — Solicitud válida

**Dado** una cita `APPROVED` y futura, y un nuevo horario disponible del mismo profesional/especialidad
**Cuando** el usuario solicita la reprogramación
**Entonces** se crea una solicitud `PENDING`, la nueva franja queda retenida y la cita original conserva su franja intacta.

### CA-02 — Cita no reprogramable

**Dado** una cita que no está `APPROVED` (p. ej. `REQUESTED`, `CANCELLED`) o no es futura
**Cuando** el usuario intenta reprogramarla
**Entonces** el sistema rechaza la solicitud.

### CA-03 — Nuevo horario no disponible

**Dado** un horario nuevo que ya está reservado/retenido
**Cuando** el usuario lo solicita como reprogramación
**Entonces** el sistema rechaza la solicitud sin afectar la cita original.

### CA-04 — Sin dos solicitudes PENDING a la vez (agregada 2026-09-29, LOOP_03)

**Dado** una cita con una solicitud de reprogramación ya en estado `PENDING`
**Cuando** el usuario (o dos solicitudes concurrentes) intenta crear otra solicitud para la misma cita
**Entonces** el sistema rechaza la nueva solicitud (`409`), incluso bajo concurrencia real, y solo queda una `PENDING`.

## Definition of Done

- [x] CA-01 a CA-04 validados con evidencia.
- [x] Entidad/adaptador JPA coherente con `reschedule_requests`/`reschedule_request_statuses` (ya existentes en `V1__esquema_inicial.sql`).
- [x] Transición registrada (ver observación DoD-02 sobre el mecanismo exacto).
- [x] `mvn test` pasa para los módulos afectados.
- [x] Trazabilidad actualizada en `docs/wiki/scrum/`.

## Evidencia de validación

| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_horarioDisponible_quedaPendingSinTocarLaCitaOriginal` | — |
| CA-02 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_citaNoAprobada_seRechaza` | — |
| CA-03 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_nuevoHorarioNoDisponible_seRechazaSinAfectarLaCitaOriginal` | — |
| CA-04 | Cumple | `SolicitarReprogramacionServiceTest.solicitar_conSolicitudPendienteExistente_seRechaza` + `.solicitar_dosSolicitudesConcurrentesSobreLaMismaCita_soloUnaQuedaPending` (8 hilos reales, cada uno con horario nuevo distinto para aislar la regla de RN-01) | Demostrado explícitamente en Red→Green (guarda comentada → 2 pruebas fallan con `expected 1 but was 8` → restaurada → 111/111) — ver LOOP_03 más abajo Verificado además con 10 peticiones HTTP simultáneas contra MySQL 8.4 real (2026-10-02, base temporal): 10 solicitudes sobre la misma cita -> 1 PENDING (200) y 9 conflictos (409). |
| DoD-02 | Cumple | `SolicitudReprogramacion.solicitar()` (estado `PENDING` al crear) | La transición vive en el propio `reschedule_requests.status_id` (y luego `decided_by_user_id`/`decided_at`/`decision_reason` al decidir, ver HU-020), no en `appointment_status_history`: el `EstadoCita` de la cita no cambia mientras la solicitud está pendiente (RN-10), así que no aplica un registro ahí |
| DoD-01 | Cumple | `mvn test`: 111/111, `BUILD SUCCESS` (2026-09-29) | Verificado contra MySQL 8.4 real: con Docker en otro equipo (2026-09-30) y con MySQL nativo en este equipo (2026-10-02). |

## Historial de validación

- 2026-09-17 — HU creada en estado `Borrador`.
- 2026-09-28 — Aprobada explícitamente por el usuario (alcance S4, ver `docs/wiki/scrum/README.md`). Se corrigió T-02/DoD: la migración ya no está bloqueada, el esquema de referencia (adoptado 2026-09-23) ya incluye `reschedule_requests`.
- 2026-09-29 — Backend implementado y validado (`POST /api/appointments/{id}/reschedule`). Nuevo agregado de dominio `SolicitudReprogramacion`; durante `PENDING`, franja antigua y nueva quedan ambas retenidas bajo el mismo `citaId` en `professional_slots`, distinguibles por horario (ver HU-020 para cómo se resuelve al decidir). Estado → `En desarrollo`.
- 2026-09-29 — UI integrada (`MisCitasScreen.tsx`/`ReprogramarCitaModal.tsx` para el USER, `BandejaReprogramacionesScreen.tsx`/`RechazarReprogramacionModal.tsx` para el ADMIN, ver HU-020) y verificada con Playwright.
- 2026-09-29 — Ejecución formal de `LOOP_02_GUIADO_AVANZADO.md` (S4): ver sección siguiente.

### Ejecución formal de LOOP_02 (2026-09-29)

Ejecutado como Builder/Verifier con roles separados, sobre las 6 reglas innegociables del loop ("la cita original sigue vigente mientras PENDING; el nuevo horario queda retenido; APPROVED libera slots anteriores y confirma los nuevos; REJECTED libera la retención nueva y conserva la original; solo ADMIN decide; frontend debe reflejar estado y motivo"). Presupuesto: máximo 4 iteraciones.

**Iteración 1 — VEREDICTO: FAIL** (subagente Verifier aislado, sin capacidad de modificar código). Reglas 1 a 5 confirmadas en PASS con evidencia de archivo:línea y pruebas reales. Regla 6 en **FAIL**: el paciente no tenía ninguna forma persistente de conocer el desenlace de su propia solicitud de reprogramación — `GET /api/appointments/mine` nunca exponía el estado/motivo de una reprogramación (solo `GET /api/admin/reschedules`, ADMIN-only, existía), y `MisCitasScreen.tsx` solo recordaba una solicitud "pendiente" en un `useState` de React que se perdía al recargar la página. El Verifier también encontró que `RechazarReprogramacionModal.tsx` le decía al ADMIN "el motivo queda visible para el paciente" — afirmación falsa dado lo anterior.

**Corrección aplicada (Builder, sin migración nueva):**
- Backend: `SolicitudReprogramacionRepositoryPort.buscarUltimaPorCita(citaId)` (+ adaptador JPA vía `RescheduleRequestJpaRepository.findFirstByAppointmentIdOrderByIdDesc`, + doble en memoria). `ConsultarMisCitasService` resuelve la última solicitud de reprogramación de cada cita (cualquiera sea su estado) y la expone en `ConsultarMisCitasUseCase.Resultado.reprogramacion` / `AppointmentDtos.MiCitaResponse.reprogramacion` (nuevo `ReprogramacionInfo`: solicitudId, estado, inicioSolicitado, finSolicitado, motivoDecision). 3 pruebas nuevas en `ConsultarMisCitasServiceTest`.
- Frontend: `MiCitaApi.reprogramacion` en `types.ts`; `MisCitasScreen.tsx` eliminó el `useState` local y ahora lee `cita.reprogramacion` de cada respuesta del backend, con 3 banners persistentes (PENDING/APPROVED/REJECTED, este último con motivo); tras solicitar, se hace un refetch real (`cargar()`) en vez de guardar el resultado solo en memoria.

**Iteración 2 — VEREDICTO: PASS** (un segundo subagente Verifier, sin contexto de la implementación salvo lo documentado, releyó todo el código y las pruebas desde cero). Confirmó con evidencia de archivo:línea que el criterio 6 ahora se cumple de forma persistente (dato viene siempre del backend, no de estado efímero de React), que las reglas 1-5 no se rompieron, y `mvn test`: **109/109** (antes 106, +3 nuevas), `npm run build`/`npm run lint`: EXIT 0. Verificado además con Playwright por este agente (no el Verifier): el banner de rechazo con motivo sobrevive a un recargo completo de página.

**Hallazgos residuales no bloqueantes** (reportados por el Verifier, fuera del alcance del criterio 6): (1) sin prueba explícita para el caso de dos solicitudes de reprogramación *distintas* sobre la misma cita — la lógica de "última por id" se revisó manualmente y es correcta, pero falta el test; (2) no hay guarda de backend contra una segunda solicitud PENDING concurrente para la misma cita (solo se oculta el botón en la UI) — candidato a HU futura, no introducido por esta iteración. **Resuelto por LOOP_03, ver sección siguiente.**

Log completo de ambas iteraciones (transcripts de los subagentes) disponible en el historial de esta sesión de Claude Code.

### Ejecución formal de LOOP_03 (2026-09-29) — reto independiente

A diferencia de LOOP_01/LOOP_02 (que vienen de archivos `.md` fijos en `prompts/goal-loop/`), este loop lo diseñó el agente orquestador siguiendo el formato exacto de `LOOP_03_RETO_INDEPENDIENTE.md` (10 puntos: disparador, meta verificable, estado observado, alcance del Builder, evidencia del Verifier, presupuesto, condición de parada, condición de escalamiento, log, justificación), presentado y aprobado explícitamente por el usuario antes de ejecutar — la guía exige que el reto lo identifique/diseñe el estudiante, no un agente por su cuenta; aquí el estudiante delegó el diseño explícitamente y lo revisó antes de dar luz verde.

**Disparador**: el hallazgo residual de LOOP_02 (arriba) — `SolicitarReprogramacionService.solicitar()` no validaba si ya existía una solicitud `PENDING` para la misma cita antes de crear otra.

**Meta verificable**: con una `PENDING` ya existente, un segundo intento sobre la misma cita se rechaza (sin crear una segunda fila), incluso bajo concurrencia real. Presupuesto: máximo 3 iteraciones.

**Implementación (Builder, sin migración nueva, sin tocar frontend/RN-01/flujo de aprobar-rechazar)**:
- `CitaRepositoryPort.conBloqueoDeEscritura(citaId, accion)`: serializa una operación sobre una cita entre hilos/transacciones concurrentes. JPA (`CitaJpaAdapter`): `@Lock(LockModeType.PESSIMISTIC_WRITE)` sobre una query dedicada (`SELECT ... FOR UPDATE`, vigente hasta el commit de la transacción del llamador). En memoria (`InMemoryCitaRepositoryAdapter`): `ReentrantLock` real por `citaId`.
- `SolicitudReprogramacionRepositoryPort.existePendientePorCita(citaId)`.
- `SolicitarReprogramacionService.solicitar()`: el check-pendiente + búsqueda de slots + reserva atómica + creación de la solicitud quedan dentro del bloqueo; si ya hay una `PENDING`, lanza `TransicionEstadoInvalidaException` (409, reutiliza el mapeo HTTP ya existente).
- 2 pruebas nuevas (CA-04 arriba), incluida una de concurrencia real con 8 hilos que piden horarios *distintos* entre sí (aísla la regla nueva de RN-01, que protege slots, no PENDING duplicadas).

**Demostración Red→Green** (por el propio Builder, antes de pedir verificación): guarda comentada → `mvn test` de la clase → 2 pruebas nuevas fallan (`expected: 1 but was: 8` en la de concurrencia) → guarda restaurada → 111/111 de nuevo.

**VEREDICTO: PASS en la iteración 1** (subagente Verifier aislado, sin contexto previo de la implementación salvo lo documentado). Confirmó independientemente: el lock JPA cubre toda la operación (no solo el SELECT) porque `conBloqueoDeEscritura` es `@Transactional` con propagación REQUIRED y se une a la transacción del servicio; el `ReentrantLock` en memoria no tiene condición de carrera en su creación (`computeIfAbsent` de `ConcurrentHashMap`); las pruebas nuevas realmente aíslan la regla de RN-01; el alcance se respetó (`git diff --stat`: 10 archivos, ninguno fuera de lo acordado); `mvn test` 111/111 de forma independiente.

**Hallazgos residuales del Verifier (no bloqueantes)**: (1) orden de bloqueo asimétrico entre `solicitar()` (bloquea `appointments` primero, luego `slots`) y `aprobar()`/`rechazar()` (tocan `slots` primero, `appointments` al final vía `guardar()` normal, sin `FOR UPDATE` explícito) — no debería producir un deadlock real hoy dado RN-10, pero es un orden de bloqueo latente e indocumentado, sin prueba que lo ejercite; (2) el lock por cita serializa también intentos concurrentes con horarios *no conflictivos* entre sí sobre la misma cita — tradeoff de rendimiento aceptado a propósito; (3) el mapa de locks del doble en memoria (`InMemoryCitaRepositoryAdapter`, solo test) nunca se purga — riesgo bajo, no es código de producción.

Log completo (transcript del Verifier) disponible en el historial de esta sesión de Claude Code.

## Notas y decisiones

- Ejecutado como LOOP_02 de S4 (ver sección de arriba) — el candidato original que sugería esta nota para el GOAL cross-repo de S3 quedó superado por la ejecución real del loop de S4.
- LOOP_03 (arriba) cierra el hallazgo residual que dejó LOOP_02 sobre solicitudes PENDING duplicadas.
