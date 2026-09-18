---
tipo: wiki
actualizado: 2026-09-17
fuente: "[[../raw/PRD.md]]"
---

# Dominio — Sistema de agendamiento de citas

## Actores

- **USER**: paciente ficticio, se autorregistra.
- **PROFESSIONAL**: creado por ADMIN; una o varias especialidades, una o ambas sedes; no aprueba citas, gestiona su propia disponibilidad y agenda.
- **ADMIN**: administra profesionales, catálogos configurables (EPS, planes, especialidades), aprueba/rechaza solicitudes especializadas y reprogramaciones.

## Sedes (catálogo fijo, dato público real)

1. Hospital Internacional de Colombia (HIC) — Km 7 Autopista Bucaramanga–Piedecuesta, Valle de Menzulí, Santander.
2. Fundación Cardiovascular de Colombia / Instituto Cardiovascular (ICV) — Calle 155A No. 23-58, Urbanización El Bosque, Floridablanca, Santander.

Todo lo demás (pacientes, profesionales, credenciales, EPS, planes, horarios, citas) es sintético.

## Reglas de negocio esenciales (RN-01 a RN-12)

Ver `PRD.md` sección 5 (copia en `../raw/PRD.md`). Resumen operativo:

- Ninguna cita ocupa slots ya reservados/retenidos (RN-01); doble reserva es un caso de prueba obligatorio en S3.
- Cita general → `APPROVED` automático (RN-02). Cita especializada → `REQUESTED`, requiere ADMIN (RN-03); rechazo exige motivo (RN-04).
- Slots de 30 min; una cita de 60 min ocupa 2 slots **consecutivos** (RN-05).
- No se permiten citas ni bloques en el pasado (RN-06).
- Un profesional solo publica agenda en sedes donde está asignado (RN-07).
- Una especialidad debe estar activa y asociada al profesional para poder reservarse (RN-08).
- Cancelar/rechazar libera las reservas correspondientes (RN-09).
- La reprogramación no destruye la cita original hasta que la nueva es aprobada (RN-10).
- Las transiciones de estado deben ser explícitas y verificables (RN-11) y su auditoría no se trata como CRUD normal (RN-12).

## Máquinas de estado relevantes

- **Cita general**: `APPROVED` (directo) → `COMPLETED` / `NO_SHOW` / `CANCELLED`.
- **Cita especializada**: `REQUESTED` → `APPROVED` | `REJECTED` (motivo obligatorio) → (`APPROVED`) → `COMPLETED` / `NO_SHOW` / `CANCELLED`.
- **Reprogramación**: `PENDING` → `APPROVED` (libera slots antiguos, asigna nuevos) | `REJECTED` (libera la reserva provisional, mantiene la cita original).

## Fuera de alcance del producto

Historia clínica, facturación real, pagos, diagnósticos/tratamientos, datos reales de FCV, integración real con sistemas clínicos, CI/CD obligatorio, SMS/WhatsApp, SMTP obligatorio para recuperación de contraseña.

## Relacionado

[[arquitectura]] · [[decisiones]] · [[riesgos]]
