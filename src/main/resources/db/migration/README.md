# Migraciones Flyway

- `V1__esquema_inicial.sql`: esquema relacional completo (3FN), diseño propio documentado en `citas-api/docs/db-design/MODELO_3FN.md` (diagrama ER, dependencias funcionales, justificación, comparación contra la referencia del trainer en `COMPARACION_REFERENCIA.md`).
- `V2__seed_catalogos_fijos.sql`: datos semilla de los catálogos fijos (RF-05, HU-006): roles, sedes, regímenes, estados de cita/reprogramación/slot.

Solo hay adaptadores JPA reales (`infrastructure/adapter/out/persistence/`) para lo que las HU aprobadas necesitan hoy (usuarios, roles, refresh tokens — HU-001/HU-002). El resto de las tablas (profesionales, agenda, citas, EPS, etc.) existen en el esquema pero todavía no tienen `@Entity`/casos de uso: se implementan progresivamente cuando sus HU (EP-002 en adelante) se aprueben en S3/S4, sin necesidad de otra migración de estructura.

No modifiques `V1`/`V2` una vez aplicadas contra una base real: agrega una nueva migración (`V3__...sql`) para cualquier cambio de esquema.
