# Payments API

Servicio REST con Spring Boot 3.2, MongoDB y RabbitMQ: alta de pagos, consulta y cambio de estatus con publicación de eventos a un exchange fanout para múltiples consumers.

Paquete base: `com.bancobase.payments` (aplicación en la raíz; dominio en `payment` con `controller`, `service`, `repository`, `dto`, `model`, `exception`).

## Requisitos

- Java 17+ (desarrollo probado con JDK 21 y `release` 17 en Maven).
- Docker (opcional, para MongoDB, RabbitMQ y/o la aplicación empaquetada).

## Arranque rápido (infra en Docker, app local)

```text
docker compose up -d mongodb rabbitmq
```

Variables por defecto en `application.yml` apuntan a `localhost:27017` y `localhost:5672`. Luego:

```text
set JAVA_HOME=<tu JDK>
mvnw.cmd spring-boot:run
```

En Linux/macOS: `chmod +x mvnw` (una vez) y `./mvnw spring-boot:run`.

## Todo en Docker

```text
docker compose up --build
```

La API queda en `http://localhost:8080`. RabbitMQ Management: `http://localhost:15672` (usuario/contraseña `guest`/`guest`).

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/payments` | Alta de pago (JSON con concepto, cantidadProductos, pagador, beneficiario, montoTotal, estatus). |
| GET | `/api/payments/{id}` | Consulta del pago (incluye estatus). |
| PATCH | `/api/payments/{id}/status` | Cambio de estatus (`{ "estatus": "COMPLETADO" }`). Si cambia, se publica evento en RabbitMQ. |

## Errores (RFC 7807)

Las respuestas de error usan **`application/problem+json`** (`ProblemDetail`) con:

- `type`, `title`, `status`, `detail`, `instance`
- **`errors`**: lista de `{ "field", "message", "rejectedValue" }` para validación Bean Validation, JSON inválido / enum incorrecto (`HttpMessageNotReadableException`), parámetros faltantes o tipos incorrectos, y `ConstraintViolationException`.

Los **404** por pago inexistente incluyen el mismo formato con `errors` vacío.

## Entregables en el repositorio

- Esquema JSON del documento: [docs/schemas/payment-document.schema.json](docs/schemas/payment-document.schema.json)
- Mensajería RabbitMQ: [docs/rabbitmq-messaging.md](docs/rabbitmq-messaging.md)
- Colección Postman: [postman/Payments-API.postman_collection.json](postman/Payments-API.postman_collection.json)

## Tests

```text
mvnw.cmd test
```
