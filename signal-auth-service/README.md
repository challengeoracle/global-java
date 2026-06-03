# SIGNAL Auth Service

Serviço de identidade do OffPay. Ele cuida de cadastro, login, JWT e perfil autenticado de vendedor e cliente.

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
| Login | `POST /auth/login` |
| Perfil autenticado | `GET /auth/me` |
| Ativar sessão offline legada do cliente | `POST /customer/offline/activate` |
| Consultar sessão offline legada do cliente | `GET /customer/offline/me` |

---

## Papel no fluxo

O `auth-service` é a porta de entrada da operação:

- cria vendedor e cliente
- entrega JWT para os outros serviços
- informa `role`, `storeId` e `storeName`

O `sales-service`, `payment-service` e `analytics-ai-service` usam esse contexto para identificar quem está fazendo a operação.

---

## JWT e perfis

Perfis usados hoje:

- `SELLER`
- `CUSTOMER`

O `GET /auth/me` devolve o contexto autenticado que o app usa para continuar o fluxo.

No vendedor, a resposta inclui a loja vinculada. O fluxo offline-first não depende de device.

---

## Endpoints legados de offline do cliente

Os endpoints de `/customer/offline/**` continuam no serviço por compatibilidade.

Hoje eles não são pré-requisito para o fluxo principal offline-first do app, mas seguem disponíveis no projeto.

---

## Segurança

Públicos:

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
POST /customer/offline/activate
GET /customer/offline/me
```

---

## Configuração

```yaml
server.port=8081
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
```

## Docker

Este serviço faz parte do `docker-compose` da raiz e sobe com:

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
