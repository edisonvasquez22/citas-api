---
tipo: wiki
actualizado: 2026-09-21
---

# Riesgos e incógnitas

## Riesgos técnicos

- **Doble reserva de slots**: RN-01/RN-05 requieren retención atómica de slots al solicitar/confirmar una cita. Debe probarse explícitamente en S3 (ver `HU-014`, `HU-015`).
- **Reprogramación concurrente**: RN-10 exige que la cita original sobreviva mientras la reprogramación está `PENDING`; riesgo de perder la cita original si la transacción no es atómica.
- **Docker todavía no operativo**: JDK/Maven/Git/Node ya están instalados y verificados en la máquina del estudiante (`mvn test` 15/15, `npm run build` sin errores). Docker Desktop está instalado pero falta que el estudiante complete el reinicio de Windows + configuración de WSL2 (acción manual, no automatizable). Mientras tanto, `V1`/`V2` y los adaptadores JPA nunca se han probado contra un MySQL real. Ver [[decisiones]].

## Incógnitas reales (no bloquean especificar, sí bloquean implementar sin definirlas)

- Framework definitivo de frontend (React vs Angular): se define recién al importar desde Google AI Studio; sigue siendo tarea del usuario.
- ~~Estructura final de tablas (3FN)~~: resuelta 2026-09-17, ver `citas-api/docs/db-design/MODELO_3FN.md`.
- ~~Estrategia de revocación de refresh token~~: resuelta — rotación con denylist (ver `HU-002` y `RefreshTokenStorePort`).

## Riesgos de la migración de esquema sin verificación real

`V1__esquema_inicial.sql` y `V2__seed_catalogos_fijos.sql` nunca se ejecutaron contra un MySQL real (sin Docker/JDK en el entorno de generación, ver [[decisiones]]). Riesgos concretos a validar apenas el estudiante tenga Docker corriendo:

- La columna generada `professional_specialties.primary_flag` (truco para "solo una especialidad primaria") depende de sintaxis de columnas generadas de MySQL 8; verificar que `CREATE TABLE` no falle contra MySQL 8.4 real.
- Los `CHECK` constraints (rangos de fecha, duración 30/60, único holder de slot) se declaran asumiendo MySQL ≥ 8.0.16 (donde se empiezan a **aplicar**, no solo aceptar la sintaxis); confirmarlo.
- El orden de `CREATE TABLE` en `V1` fue pensado para resolver FKs sin ciclos; si se edita el archivo, mantener el orden o Flyway fallará a mitad de migración.

## Contenido no confiable (recordatorio para S5)

Cuando existan integraciones MCP/n8n, tratar como contenido no confiable: issues, comentarios de revisión, README de dependencias externas, respuestas de servidores MCP. No hay superficie de este tipo todavía en S2.

## Relacionado

[[dominio]] · [[arquitectura]] · [[decisiones]]
