# OffPay Auth Service

Serviço de identidade do OffPay. Responsável por cadastro, login, JWT, perfil do usuário e papéis (`CUSTOMER` / `SELLER`).

A API roda localmente em:

```text
http://localhost:8081
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

## Responsabilidade

O `signal-auth-service` responde por:

| Função                | Endpoint                       |
| --------------------- | ------------------------------ |
| Cadastro de vendedor  | `POST /auth/register/seller`   |
| Cadastro de cliente   | `POST /auth/register/customer` |
| Login com JWT         | `POST /auth/login`             |
| Perfil autenticado    | `GET /auth/me`                 |
| Atualização de device | `PATCH /device/me`             |

O Auth Service não processa vendas, catálogo, pedidos ou pagamentos.

O `signal-sales-service` consome `GET /auth/me` para identificar o usuário autenticado, seu papel, `storeId` e `deviceId` opcional.

## Modelo de Identidade

### JWT

O JWT carrega as informações necessárias para autenticação e autorização entre os serviços.

Claims principais:

- `subject`: e-mail do usuário
- `role`: papel do usuário
- `userId`: identificador do usuário

A expiração é configurável por `jwt.expiration-minutes`.

### Papéis

- `SELLER`: vendedor associado a uma loja
- `CUSTOMER`: cliente final

### deviceId

O `deviceId` é opcional e usado apenas para rastreabilidade do aparelho.

Ele pode ser:

- informado no cadastro do vendedor
- atualizado depois via `PATCH /device/me`
- retornado em `/auth/me`, caso exista

O `deviceId` não bloqueia operações offline-first no Sales Service.

## Endpoints Principais

### Cadastro de vendedor

`POST /auth/register/seller`

Cria um vendedor com loja vinculada.

```json
{
    "name": "Mateus Lima",
    "email": "mateus.seller@email.com",
    "password": "123456",
    "cpf": "12345678901",
    "phone": "11999999999",
    "storeName": "Mercado OffPay",
    "storeCategory": "Mercado",
    "deviceId": "device-seller-001"
}
```

Observação: `deviceId` é opcional.

### Cadastro de cliente

`POST /auth/register/customer`

Cria um cliente final.

```json
{
    "name": "Maria Souza",
    "email": "maria.customer@email.com",
    "password": "123456",
    "cpf": "10987654321",
    "phone": "11988887777"
}
```

### Login

`POST /auth/login`

Autentica o usuário e retorna o JWT.

```json
{
    "email": "mateus.seller@email.com",
    "password": "123456"
}
```

O login não retorna token offline.

### Perfil autenticado

`GET /auth/me`

Requer autenticação via JWT.

```http
Authorization: Bearer <JWT>
```

Retorna o contexto do usuário autenticado, incluindo papel, dados do perfil e loja vinculada quando aplicável.

### Atualizar device

`PATCH /device/me`

Atualiza o identificador local do aparelho do vendedor.

```json
{
    "deviceId": "device-seller-002"
}
```

## Integração com Sales Service

O Sales Service chama `GET /auth/me` usando o JWT recebido do mobile.

Com isso, o Sales consegue:

- identificar o usuário autenticado
- validar se o usuário é `SELLER` ou `CUSTOMER`
- obter o `storeId` do vendedor
- associar catálogo, produtos e pedidos à loja correta
- usar `deviceId` apenas como rastreio quando necessário

Não é necessário ativar modo offline antes de sincronizar pedidos.

## Endpoints Legados de Offline

Alguns endpoints de offline ainda podem existir por compatibilidade, mas não fazem parte do fluxo principal do OffPay.

| Endpoint                          | Descrição                                 |
| --------------------------------- | ----------------------------------------- |
| `GET /device/me`                  | Consulta status do device                 |
| `POST /device/offline/activate`   | Gera token offline legado para vendedor   |
| `PATCH /device/me`                | Atualiza `deviceId`                       |
| `POST /customer/offline/activate` | Gera sessão offline legada para cliente   |
| `GET /customer/offline/me`        | Consulta sessão offline legada do cliente |

Esses endpoints não bloqueiam operações no Sales Service.

## Segurança

Rotas públicas:

```text
POST /auth/register/seller
POST /auth/register/customer
POST /auth/login
/swagger-ui.html
/v3/api-docs
```

Rotas autenticadas:

```text
GET /auth/me
PATCH /device/me
```

Rotas legadas por papel:

```text
SELLER -> /device/**
CUSTOMER -> /customer/offline/**
```

## Configuração

```yaml
jwt:
secret: ${JWT_SECRET}
expiration-minutes: ${JWT_EXPIRATION_MINUTES:120}

offline:
session-expiration-hours: 24
```

A configuração `offline` é mantida apenas para endpoints legados.

## Fluxo Recomendado no Mobile

1. O usuário faz cadastro ou login.
2. O mobile armazena o JWT.
3. O mobile consulta `GET /auth/me`.
4. O app obtém `role`, `storeId` e `deviceId`, quando houver.
5. O Sales Service usa o JWT para validar contexto e permissões.
6. O fluxo offline-first salva dados localmente e sincroniza quando houver conexão.
