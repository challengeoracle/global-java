# SIGNAL Auth Service

Servico de identidade do OffPay. Ele cuida do fluxo principal de cadastro, login, emissao de JWT e perfil autenticado de vendedor e cliente.

API local:

```text
http://localhost:8081
```

Swagger local:

```text
http://localhost:8081/swagger-ui.html
```

## Responsabilidade

O `signal-auth-service` responde por:

| Funcao | Endpoint |
|--------|----------|
| Cadastro de vendedor | `POST /auth/register/seller` |
| Cadastro de cliente | `POST /auth/register/customer` |
| Login | `POST /auth/login` |
| Perfil autenticado | `GET /auth/me` |

## Papel no fluxo

O `auth-service` e a porta de entrada da operacao:

- cria vendedor e cliente
- entrega JWT para os outros servicos
- informa `role`, `storeId` e `storeName`

O `sales-service`, `payment-service` e `analytics-ai-service` usam esse contexto para identificar quem esta fazendo a operacao.

## JWT e perfis

Perfis usados hoje:

- `SELLER`
- `CUSTOMER`

O `GET /auth/me` devolve o contexto autenticado que o app usa para continuar o fluxo.

No vendedor, a resposta inclui a loja vinculada.

## Seguranca

Publicos:

```text
POST /auth/register/seller
POST /auth/register/customer
POST /auth/login
/swagger-ui.html
/v3/api-docs
```

Autenticados:

```text
GET /auth/me
```

## Configuracao

```text
SERVER_PORT=8081
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
```

## Docker

Este servico faz parte do `docker-compose` da raiz e sobe com:

```powershell
docker compose up -d --build signal-auth-service
```

Variaveis esperadas no ambiente:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
