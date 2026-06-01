# SIGNAL Sales Service

Servico de catalogo e pedidos do OffPay. Ele cuida do que o vendedor vende e do que precisa ser sincronizado quando a internet volta.

A API roda localmente em:

```
http://localhost:8082
```

Swagger:

```
http://localhost:8082/swagger-ui.html
```

---

## Responsabilidade

O `signal-sales-service` responde por:

| Funcao | Endpoint |
|--------|----------|
| Catalogo do vendedor autenticado | `GET /catalog/me` |
| Catalogo publico por loja | `GET /catalog/store/{storeId}` |
| Sync de catalogo offline | `POST /catalog/sync` |
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
| Sync de pedidos offline | `POST /order/sync` |
| Meus pedidos | `GET /order/me` |
| Meus pedidos paginado | `GET /order/me/page` |
| Minhas vendas | `GET /order/me/sales` |
| Minhas vendas paginado | `GET /order/me/sales/page` |
| Minhas compras | `GET /order/me/purchases` |
| Minhas compras paginado | `GET /order/me/purchases/page` |
| Buscar pedido | `GET /order/{id}` |
| Pedidos por loja | `GET /order/store/{storeId}` |
| Pedidos por cliente | `GET /order/customer/{customerId}` |

---

## Papel no fluxo

O `sales-service` e o centro da operacao comercial:

- guarda o catalogo da loja
- aceita alteracoes criadas offline no app
- recebe pedidos criados online
- sincroniza pedidos confirmados localmente
- publica eventos para o fluxo de pagamento

Quando vendedor e cliente ficam sem internet, o mobile salva tudo localmente e depois envia para `POST /catalog/sync` e `POST /order/sync`.

---

## Integracao com Auth e Payment

O servico usa o JWT do `auth-service` para identificar vendedor ou cliente.

Tambem conversa com o `payment-service` por mensageria:

- publica `PaymentRequested`
- recebe retorno de pagamento processado
- atualiza o status financeiro do pedido

---

## Seguranca

Publicos:

```
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
/swagger-ui.html
/v3/api-docs
```

Autenticados:

```text
/catalog/me
/catalog/sync
/category/**
/product (POST, PUT, DELETE)
/order/**
```

---

## Configuracao

```yaml
server.port=8082
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
