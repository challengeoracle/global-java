# SIGNAL Auth Service

Serviço de identidade do OffPay. Ele cuida do fluxo principal de cadastro, login, emissão de JWT e perfil autenticado de vendedor e cliente.

## Endereço local

```text
http://localhost:8081
```

## Swagger local

```text
http://localhost:8081/swagger-ui/index.html
```

## Responsabilidade

O `signal-auth-service` responde por:

| Função | Endpoint |
|--------|----------|
| Cadastro de vendedor | `POST /auth/register/seller` |
| Cadastro de cliente | `POST /auth/register/customer` |
| Login | `POST /auth/login` |
| Perfil autenticado | `GET /auth/me` |

## Papel no fluxo

O `auth-service` é a porta de entrada da operação:

- cria vendedor e cliente
- entrega JWT para os outros serviços
- informa `role`, `storeId` e `storeName`

O `sales-service`, `payment-service` e `analytics-ai-service` usam esse contexto para identificar quem está fazendo a operação.

## JWT e perfis

Perfis usados hoje:

- `SELLER`
- `CUSTOMER`

O `GET /auth/me` devolve o contexto autenticado que a aplicação usa para continuar o fluxo.

No vendedor, a resposta inclui a loja vinculada.

## Segurança

Públicos:

```text
POST /auth/register/seller
POST /auth/register/customer
POST /auth/login
/swagger-ui/index.html
/v3/api-docs
```

Autenticados:

```text
GET /auth/me
```

## Configuração

```text
SERVER_PORT=8081
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
```

## Execução local

Este serviço faz parte do `docker-compose` da raiz e pode ser iniciado com:

```powershell
docker compose up -d --build signal-auth-service
```

Variáveis esperadas no ambiente:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
