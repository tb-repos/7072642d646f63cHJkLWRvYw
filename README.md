# Shrine Tours Enterprise Backend

This package is a stronger client-demo baseline for the Shrine Tours backend.

## What was upgraded
- Consistent enterprise response envelope across controllers
- Thin controllers with service and service-impl separation
- Global exception handling for validation, malformed JSON, not found, and data conflicts
- Auditing support with `createdAt` and `updatedAt`
- Safer JPA table names, indexes, and unique constraints
- Optional BigQuery analytics configuration guarded by `app.analytics.bigquery.enabled=false`

## Important notes
- Authentication still uses demo tokens for presentation. It is not full JWT security yet.
- BigQuery is configured for analytics only, not as the transactional database.
- Default runtime database is in-memory H2 for fast demo startup.

## Run
```bash
mvn clean install
mvn spring-boot:run
```

## Demo account
- email: `demo@shrinetours.com`
- password: `password123`
- otp: `123456`

## Swagger
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/h2-console`

## JWT Security

This package now includes a full JWT baseline:
- access token generation on login, register, and Google sign-in
- refresh token rotation via `/api/v1/auth/refresh`
- stateless Spring Security filter chain
- bearer token protection on non-auth APIs

### Protected API usage
Send this header on secured endpoints:

```http
Authorization: Bearer <access_token>
```

### Public routes
- `/api/v1/auth/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/h2-console/**`


## Razorpay Payment Flow

Protected endpoints:
- `POST /api/v1/payments/create-order`
- `POST /api/v1/payments/verify`
- `GET /api/v1/payments/orders`
- `GET /api/v1/payments/orders/{id}`

Public webhook endpoint:
- `POST /api/v1/payments/webhook`

Create order body:
```json
{
  "planCode": "monthly_premium"
}
```

Verify body:
```json
{
  "razorpayOrderId": "order_xxx",
  "razorpayPaymentId": "pay_xxx",
  "razorpaySignature": "signature_from_frontend"
}
```

Supported plan codes:
- `monthly_premium`
- `quarterly_premium`
- `yearly_premium`
