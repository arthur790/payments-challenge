# Definición de exchanges, colas y mensajes (RabbitMQ)

## Exchange

| Nombre | Tipo   | Durable | Descripción |
|--------|--------|---------|-------------|
| `payment.status.events` | **fanout** | Sí | Distribuye cada evento de cambio de estatus a **todas** las colas enlazadas. La clave de enrutamiento se ignora en fanout. |

La aplicación declara el exchange al arrancar (`com.bancobase.payments.messaging.RabbitMQConfig`).

## Colas y bindings

Cada cola está enlazada al exchange `payment.status.events` sin routing key (fanout).

| Cola | Consumer de ejemplo | Rol |
|------|---------------------|-----|
| `payment.status.notify.email` | `EmailNotificationConsumer` | Simula notificación (p. ej. email) al cambiar estatus. |
| `payment.status.audit` | `AuditLogConsumer` | Simula trazabilidad / auditoría del cambio. |

Ambas reciben **la misma copia** del mensaje JSON para permitir 2+ tareas independientes.

## Publicación

- **Quién publica:** `PaymentStatusEventPublisher`, invocado desde `PaymentServiceImpl.updateStatus` solo cuando el nuevo estatus es distinto del anterior.
- **Exchange:** `payment.status.events`
- **Routing key:** cadena vacía (`""`) — requerida por la API de RabbitMQ; en fanout no filtra destinos.

## Formato del mensaje

- **Content-Type:** `application/json` (vía `Jackson2JsonMessageConverter`).
- **Cuerpo (JSON):** objeto equivalente a `PaymentStatusChangedEvent`:

```json
{
  "paymentId": "507f1f77bcf86cd799439011",
  "previousStatus": "PENDIENTE",
  "newStatus": "COMPLETADO",
  "occurredAt": "2025-01-15T10:30:00Z",
  "concepto": "Pedido #123"
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `paymentId` | string | Id del pago en MongoDB. |
| `previousStatus` | string | Estatus antes del cambio (`PENDIENTE`, `COMPLETADO`, `CANCELADO`, `FALLIDO`). |
| `newStatus` | string | Estatus después del cambio. |
| `occurredAt` | string (ISO-8601 instant) | Momento del evento. |
| `concepto` | string | Concepto del pago (contexto adicional para consumers). |

## Flujo resumido

1. Cliente llama `PATCH /api/payments/{id}/status` con un `estatus` nuevo.
2. Si cambia respecto al valor en base de datos, se persiste y se publica un mensaje al exchange fanout.
3. Cada cola enlazada recibe el mensaje; sus `@RabbitListener` procesan en paralelo.
