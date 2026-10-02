# Workflows n8n — puesta en marcha

Los JSON de esta carpeta **no contienen secretos**: las credenciales se reasignan en n8n después de importar.

| Archivo | Disparador | ¿Necesita que n8n alcance la API? |
|---|---|---|
| `WF-002-status-notifications.json` | Webhook (lo llama `citas-api`) | No |
| `WF-001-appointment-reminders.json` | Diario 08:00 (Bogotá) | Sí (URL pública) |
| `WF-003-daily-operational-summary.json` | Diario 20:00 (Bogotá, bonus) | Sí (URL pública) |

## Credenciales que se crean en n8n (una sola vez)

1. **Gmail OAuth2 API** — con tu propio cliente OAuth de Google Cloud (permiso mínimo posible, cuenta de laboratorio).
2. **Header Auth `FCV Webhook Secret`** — nombre de cabecera `X-Webhook-Secret`, valor = `N8N_WEBHOOK_SECRET` del `.env`.
3. **Header Auth `FCV Integration Key`** — nombre de cabecera `X-Integration-Key`, valor = `N8N_API_KEY` del `.env`.

## Antes de activar cada workflow

- En los nodos *Code* de configuración, reemplazar `LAB_RECIPIENT_AQUI` por el correo de laboratorio que recibe **todos** los mensajes (los datos son sintéticos; no se escribe a pacientes reales). Mientras siga el valor por defecto, WF-002 responde `503` y WF-001/003 se detienen con un error explícito.
- WF-001 y WF-003: reemplazar `API_BASE_URL_AQUI` por la URL pública de `citas-api`.
- Ejecutar de forma manual una vez y revisar la salida antes de activar.

## Backend (`.env`, nunca en Git)

`N8N_API_KEY`, `N8N_WEBHOOK_SECRET` y `N8N_WEBHOOK_URL` (URL de **producción** del webhook de WF-002, que termina en `/webhook/fcv-citas/status`).

## Contrato de WF-002

`POST` con cabecera `X-Webhook-Secret` y cuerpo JSON `{tipo, citaId, motivo, momento, estadoCita, inicio, fin, pacienteNombre, pacienteEmail, profesionalNombre, sedeNombre, especialidadNombre}`.
Respuestas deterministas: `200` correo enviado, `400` evento inválido, `503` Gmail falló o destinatario sin configurar.
Tipos: `ESPECIALIZADA_APROBADA`, `ESPECIALIZADA_RECHAZADA`, `CITA_CANCELADA`, `REPROGRAMACION_APROBADA`, `REPROGRAMACION_RECHAZADA`.

## Estado de verificación (2026-10-02)

| Workflow | Resultado |
|---|---|
| WF-002 | Probado de punta a punta con `citas-api` real: aprobar, rechazar y cancelar disparan el webhook y llega el correo (`200`); sin secreto `403`; evento inválido `400`; destinatario sin configurar `503`. |
| WF-001 | Ejecución manual en n8n contra la API expuesta con un túnel temporal: consulta con `X-Integration-Key`, correo recibido. La deduplicación se probó localmente; en n8n solo persiste en ejecuciones de producción (no en manuales). |
| WF-003 | Ejecución manual en n8n contra la API expuesta con un túnel temporal: resumen por sede/estado/especialidad recibido, sin datos personales. |

Riesgos residuales: la credencial de Gmail usa el alcance amplio que exige el nodo de n8n (conviene una cuenta de laboratorio); el túnel de pruebas es público mientras está abierto y solo los endpoints `/api/integration/**` aceptan la clave de integración; el destinatario de laboratorio evita escribir a pacientes (los datos son sintéticos).
