# SIGNAL Auth Service

Serviço de **identidade** do SIGNAL: login, JWT, perfil do usuário e papéis (`CUSTOMER` / `SELLER`).

A API roda localmente em:

```
http://localhost:8081
```

Swagger:

```
http://localhost:8081/swagger-ui.html
```

---

## Responsabilidade

O `signal-auth-service` responde por:

| Função | Endpoint |
|--------|----------|
| Cadastro de vendedor | `POST /auth/register/seller` |
| Cadastro de cliente | `POST /auth/register/customer` |
| Login + JWT | `POST /auth/login` |
| Perfil autenticado | `GET /auth/me` |

O auth **não** processa vendas, catálogo ou pagamentos.  
O `signal-sales-service` consome apenas `GET /auth/me` (JWT) para identificar vendedor, `storeId` e `deviceId` opcional.

---

## Modelo de identidade

### JWT

Claims: `subject` (email), `role`, `userId`.  
Expiração configurável via `jwt.expiration-minutes` (padrão: 120 min).

### Papéis

- `SELLER` — vendedor com loja (`storeId`, `storeName`)
- `CUSTOMER` — cliente final

### deviceId (opcional)

- Pode ser informado no cadastro do vendedor ou depois via `PATCH /device/me` (legado)
- Retornado em `/auth/me` quando registrado; `null` caso contrário
- O sales-service recebe `deviceId` no body do sync para **rastreio**, sem exigir ativação offline

---

## Endpoints principais

### Cadastro de vendedor

```
POST /auth/register/seller
```

```json
{
  "name": "Mateus Lima",
  "email": "mateus.seller@email.com",
  "password": "123456",
  "cpf": "12345678901",
  "phone": "11999999999",
  "storeName": "Mercado Signal",
  "storeCategory": "Mercado",
  "deviceId": "device-seller-001"
}
```

`deviceId` é **opcional**. Sem ele, o vendedor é criado normalmente; o device pode ser registrado depois.

### Cadastro de cliente

```
POST /auth/register/customer
```

### Login

```
POST /auth/login
```

Retorna JWT + perfil. **Não** retorna token offline.

### Perfil autenticado

```
GET /auth/me
Authorization: Bearer <JWT>
```

Resposta para vendedor:

```json
{
  "id": "...",
  "name": "Mateus Lima",
  "email": "mateus.seller@email.com",
  "role": "SELLER",
  "storeId": "...",
  "storeName": "Mercado Signal",
  "deviceId": "device-seller-001"
}
```

Para cliente, `storeId` e `deviceId` vêm `null`.

---

## Integração com Sales

O sales-service chama `GET /auth/me` com o JWT do vendedor:

- Valida papel `SELLER`
- Usa `storeId` para associar pedidos
- `deviceId` do sync vem no body (`OrderSyncRequest.deviceId`) — rastreio, não bloqueio

**Não é necessário** chamar `/device/offline/activate` antes de sincronizar pedidos.

---

## Endpoints legados (offline)

Mantidos por compatibilidade. Tabelas `TB_DEVICES` e `TB_CUSTOMER_OFFLINE_SESSIONS` **não foram removidas**.

| Endpoint | Descrição |
|----------|-----------|
| `GET /device/me` | Status do device + token offline (legado) |
| `POST /device/offline/activate` | Gera `offlineToken` do vendedor (legado) |
| `PATCH /device/me` | Registra/atualiza `deviceId` |
| `POST /customer/offline/activate` | Gera `sessionToken` do cliente (legado) |
| `GET /customer/offline/me` | Status da sessão offline do cliente (legado) |

Esses endpoints **não bloqueiam** operações no sales-service.

---

## Segurança

**Públicas:**

```
POST /auth/register/seller
POST /auth/register/customer
POST /auth/login
/swagger-ui.html
/v3/api-docs
```

**Autenticadas (JWT):**

```
GET /auth/me
```

**Por papel (legado):**

```
SELLER   → /device/**
CUSTOMER → /customer/offline/**
```

---

## Configuração

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration-minutes: ${JWT_EXPIRATION_MINUTES:120}

offline:
  session-expiration-hours: 24   # usado apenas pelos endpoints legados
```

---

## Fluxo recomendado (mobile)

1. `POST /auth/register/seller` ou `/auth/login`
2. Guardar JWT
3. `GET /auth/me` → obter `storeId`, `role`, `deviceId` (se houver)
4. Sync de pedidos no sales com JWT + `deviceId` no body

Sem necessidade de ativação offline.
