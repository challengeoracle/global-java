# SIGNAL Sales Service

Serviço de catálogo e pedidos do OffPay. Ele cuida do que o vendedor vende e do que precisa ser sincronizado quando a internet volta.

## Endereço local

```text
http://localhost:8082
```

## Swagger local

```text
http://localhost:8082/swagger-ui/index.html
```

## Responsabilidade

O `signal-sales-service` responde por:

| Função | Endpoint |
|--------|----------|
| Catálogo do vendedor autenticado | `GET /catalog/me` |
| Catálogo público por loja | `GET /catalog/store/{storeId}` |
| Sincronização de catálogo offline | `POST /catalog/sync` |
| Criar categoria | `POST /category` |
| Listar minhas categorias | `GET /category/me` |
| Buscar categoria | `GET /category/{id}` |
| Atualizar categoria | `PUT /category/{id}` |
| Desativar categoria | `DELETE /category/{id}` |
| Criar produto | `POST /product` |
| Listar produtos ativos | `GET /product` |
| Listar produtos paginado | `GET /product/page` |
| Buscar produto | `GET /product/{id}` |
| Produtos por loja | `GET /product/store/{storeId}` |
| Produtos por loja paginado | `GET /product/store/{storeId}/page` |
| Produtos por loja e categoria | `GET /product/store/{storeId}/category/{categoryId}` |
| Produtos por loja e categoria paginado | `GET /product/store/{storeId}/category/{categoryId}/page` |
| Produtos por categoria | `GET /product/category/{categoryId}` |
| Produtos por categoria paginado | `GET /product/category/{categoryId}/page` |
| Atualizar produto | `PUT /product/{id}` |
| Desativar produto | `DELETE /product/{id}` |
| Criar pedido online | `POST /order` |
| Sincronização de pedidos offline | `POST /order/sync` |
| Meus pedidos | `GET /order/me` |
| Minhas vendas | `GET /order/me/sales` |
| Minhas compras | `GET /order/me/purchases` |

## Papel no fluxo

O `sales-service` é o centro da operação comercial:

- guarda o catálogo da loja
- aceita alterações criadas offline no app
- recebe pedidos criados online
- sincroniza pedidos confirmados localmente
- publica eventos para o fluxo de pagamento

Quando vendedor e cliente ficam sem internet, o mobile salva tudo localmente e depois envia para `POST /catalog/sync` e `POST /order/sync`.

## Integração com Auth e Payment

O serviço usa o JWT do `auth-service` para identificar vendedor ou cliente.

Também conversa com o `payment-service` por mensageria:

- publica `PaymentRequested`
- recebe retorno de pagamento processado
- atualiza o status financeiro do pedido

## Segurança

Públicos:

```text
GET /catalog/store/{storeId}
GET /product
GET /product/page
GET /product/{id}
GET /product/store/{storeId}
GET /product/store/{storeId}/page
GET /product/store/{storeId}/category/{categoryId}
GET /product/store/{storeId}/category/{categoryId}/page
GET /product/category/{categoryId}
GET /product/category/{categoryId}/page
/swagger-ui/index.html
/v3/api-docs
```

Autenticados:

```text
/catalog/me
/catalog/sync
/category/**
/product
/order/**
```

## Configuração

```text
SERVER_PORT=8082
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
AUTH_SERVICE_URL=http://localhost:8081
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

## Execução local

Este serviço faz parte do `docker-compose` da raiz e pode ser iniciado com:

```powershell
docker compose up -d --build signal-sales-service
```

Variáveis esperadas no ambiente:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `AUTH_SERVICE_URL`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
