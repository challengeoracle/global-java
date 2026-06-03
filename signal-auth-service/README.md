# SIGNAL Auth Service

Servico de identidade do OffPay. Ele cuida de cadastro, login, JWT e perfil autenticado de vendedor e cliente.

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

| Funcao | Endpoint |
|--------|----------|
| Cadastro de vendedor | `POST /auth/register/seller` |
| Cadastro de cliente | `POST /auth/register/customer` |
| Login | `POST /auth/login` |
| Perfil autenticado | `GET /auth/me` |
| Ativar sessao offline legada do cliente | `POST /customer/offline/activate` |
| Consultar sessao offline legada do cliente | `GET /customer/offline/me` |

---

## Papel no fluxo

O `auth-service` e a porta de entrada da operacao:

- cria vendedor e cliente
- entrega JWT para os outros servicos
- informa `role`, `storeId` e `storeName`

O `sales-service`, `payment-service` e `analytics-ai-service` usam esse contexto para identificar quem esta fazendo a operacao.

---

## JWT e perfis

Perfis usados hoje:

- `SELLER`
- `CUSTOMER`

O `GET /auth/me` devolve o contexto autenticado que o app usa para continuar o fluxo.

No vendedor, a resposta inclui a loja vinculada. O fluxo offline-first nao depende de device.

---

## Endpoints legados de offline do cliente

Os endpoints de `/customer/offline/**` continuam no servico por compatibilidade.

Hoje eles nao sao pre-requisito para o fluxo principal offline-first do app, mas seguem disponiveis no projeto.

---

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
POST /customer/offline/activate
GET /customer/offline/me
```

---

## Configuracao

```yaml
server.port=8081
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
```
