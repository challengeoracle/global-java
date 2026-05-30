# OffPay Sales Service

Serviço comercial do OffPay. Responsável por catálogo, categorias, produtos, estoque, pedidos, sincronização offline-first e publicação de eventos para processamento financeiro futuro.

A API roda localmente em:

```text
http://localhost:8082
```

Swagger:

```text
http://localhost:8082/swagger-ui.html
```

## Responsabilidade

O `signal-sales-service` responde por:

| Função                           | Endpoint                       |
| -------------------------------- | ------------------------------ |
| Catálogo da loja                 | `GET /catalog/me`              |
| Catálogo público por loja        | `GET /catalog/store/{storeId}` |
| Sincronização de catálogo        | `POST /catalog/sync`           |
| Categorias                       | `/category/**`                 |
| Produtos                         | `/product/**`                  |
| Pedido online                    | `POST /order`                  |
| Sincronização de pedidos offline | `POST /order/sync`             |
| Histórico de pedidos             | `GET /order/me`                |

O Sales Service não autentica usuários diretamente. Ele consome o Auth Service para validar o JWT e obter o contexto do usuário autenticado.

## Modelo Comercial

### Loja

A loja pertence a um vendedor (`SELLER`) e é identificada por `storeId`.

O `storeId` vem do Auth Service por meio de `GET /auth/me`.

### Catálogo

O catálogo é composto por:

- categorias
- produtos
- estoque
- status ativo/inativo

O catálogo pode ser consultado online ou importado pelo mobile para operação local.

### Pedido

Um pedido representa uma operação comercial entre cliente e vendedor.

Campos principais:

- `localOrderId`: identificador local usado na sincronização offline
- `storeId`: loja onde a venda ocorreu
- `customerId`: cliente que originou o pedido, quando disponível
- `sellerId`: vendedor que confirmou a venda
- `deviceId`: aparelho usado na operação ou sincronização
- `syncStatus`: estado de sincronização
- `paymentStatus`: estado financeiro futuro

## Integração com Auth Service

O Sales Service usa o JWT recebido do mobile para consultar o Auth Service.

Com isso, o Sales consegue:

- identificar o usuário autenticado
- validar se o usuário é `SELLER` ou `CUSTOMER`
- obter o `storeId` do vendedor
- associar catálogo e pedidos à loja correta
- impedir que usuários acessem dados fora do seu contexto

O `deviceId` é usado apenas para rastreio e sincronização. Ele não bloqueia o fluxo offline-first.

## Endpoints de Catálogo

### Buscar catálogo da própria loja

`GET /catalog/me`

Retorna o catálogo da loja do vendedor autenticado.

Requer JWT de vendedor.

### Buscar catálogo por loja

`GET /catalog/store/{storeId}`

Retorna o catálogo de uma loja específica.

Usado pelo cliente para importar catálogo e operar localmente.

### Sincronizar catálogo

`POST /catalog/sync`

Sincroniza alterações locais feitas no mobile.

```json
{
    "deviceId": "device-seller-001",
    "changes": [
        {
            "operationId": "cat-op-001",
            "operation": "CATEGORY_CREATE",
            "categoryId": "11111111-1111-1111-1111-111111111111",
            "name": "Bebidas",
            "description": "Itens gelados e não alcoólicos",
            "localUpdatedAt": "2026-05-30T10:15:00"
        },
        {
            "operationId": "prod-op-001",
            "operation": "PRODUCT_CREATE",
            "productId": "22222222-2222-2222-2222-222222222222",
            "categoryId": "11111111-1111-1111-1111-111111111111",
            "name": "Água 500ml",
            "description": "Garrafa sem gás",
            "price": 4.5,
            "stockQuantity": 30,
            "localUpdatedAt": "2026-05-30T10:16:00"
        }
    ]
}
```

Cada operação usa `operationId` para evitar duplicação.

## Endpoints de Categorias

| Método   | Endpoint         | Finalidade                           |
| -------- | ---------------- | ------------------------------------ |
| `GET`    | `/category/me`   | Lista categorias da loja do vendedor |
| `GET`    | `/category/{id}` | Consulta categoria                   |
| `POST`   | `/category`      | Cria categoria                       |
| `PUT`    | `/category/{id}` | Atualiza categoria                   |
| `DELETE` | `/category/{id}` | Desativa categoria                   |

### Criar categoria

`POST /category`

```json
{
    "name": "Bebidas",
    "description": "Itens gelados e não alcoólicos"
}
```

## Endpoints de Produtos

| Método   | Endpoint                                         | Finalidade                          |
| -------- | ------------------------------------------------ | ----------------------------------- |
| `GET`    | `/product`                                       | Lista produtos ativos               |
| `GET`    | `/product/{id}`                                  | Consulta produto                    |
| `GET`    | `/product/store/{storeId}`                       | Lista produtos de uma loja          |
| `GET`    | `/product/store/{storeId}/category/{categoryId}` | Lista produtos por loja e categoria |
| `GET`    | `/product/category/{categoryId}`                 | Lista produtos por categoria        |
| `POST`   | `/product`                                       | Cria produto                        |
| `PUT`    | `/product/{id}`                                  | Atualiza produto                    |
| `DELETE` | `/product/{id}`                                  | Desativa produto                    |

### Criar produto

`POST /product`

```json
{
    "categoryId": "11111111-1111-1111-1111-111111111111",
    "name": "Água 500ml",
    "description": "Garrafa sem gás",
    "price": 4.5,
    "stockQuantity": 30
}
```

## Endpoints de Pedidos

| Método | Endpoint                       | Finalidade                             |
| ------ | ------------------------------ | -------------------------------------- |
| `GET`  | `/order/me`                    | Lista pedidos do usuário autenticado   |
| `GET`  | `/order/{id}`                  | Consulta pedido por ID                 |
| `GET`  | `/order/store/{storeId}`       | Lista pedidos de uma loja              |
| `GET`  | `/order/customer/{customerId}` | Lista pedidos de um cliente            |
| `POST` | `/order`                       | Cria pedido online                     |
| `POST` | `/order/sync`                  | Sincroniza pedidos confirmados offline |

### Criar pedido online

`POST /order`

```json
{
    "storeId": "33333333-3333-3333-3333-333333333333",
    "deviceId": "device-customer-001",
    "items": [
        {
            "productId": "22222222-2222-2222-2222-222222222222",
            "quantity": 2,
            "unitPrice": 4.5
        }
    ]
}
```

### Sincronizar pedido offline

`POST /order/sync`

```json
{
    "deviceId": "device-seller-001",
    "orders": [
        {
            "localOrderId": "local-order-001",
            "customerId": "44444444-4444-4444-4444-444444444444",
            "offlineCreatedAt": "2026-05-30T10:20:00",
            "items": [
                {
                    "productId": "22222222-2222-2222-2222-222222222222",
                    "quantity": 2,
                    "unitPrice": 4.5
                }
            ]
        }
    ]
}
```

O `localOrderId` evita duplicação caso o mesmo pedido seja enviado mais de uma vez.

## Sincronização Offline-First

O Sales Service recebe dados gerados localmente pelo mobile e garante consistência no backend.

### Sincronização de catálogo

Fluxo:

1. vendedor altera categoria, produto ou estoque no mobile
2. alteração é salva localmente
3. alteração entra em fila local
4. quando houver conexão, o mobile envia `POST /catalog/sync`
5. o Sales Service processa item a item
6. cada item retorna `APPLIED`, `DUPLICATE` ou `REJECTED`

Garantias:

- `operationId` repetido não reaplica a operação
- falhas são retornadas por item
- o lote não deve quebrar por causa de uma única operação inválida

### Sincronização de pedidos

Fluxo:

1. cliente monta pedido no mobile
2. vendedor escaneia o QR Code e confirma a venda
3. pedido é salvo localmente no aparelho do vendedor
4. quando houver conexão, o vendedor envia `POST /order/sync`
5. o Sales Service valida estoque, loja e itens
6. o pedido é registrado no backend
7. o serviço publica evento para processamento financeiro futuro

Garantias:

- `localOrderId` evita pedidos duplicados
- pedido duplicado retorna como operação já sincronizada
- pedido rejeitado retorna motivo para tratamento no mobile

## RabbitMQ

O Sales Service publica eventos para desacoplar a confirmação comercial do processamento financeiro.

Configuração atual:

- exchange: `offpay.sales.exchange`
- routing key: `payment.requested`

Fluxo:

1. pedido online é criado ou pedido offline é sincronizado
2. Sales Service registra o pedido
3. Sales Service publica evento `PaymentRequested`
4. Payment Service futuro poderá consumir esse evento

Se a publicação no RabbitMQ falhar, o pedido continua salvo.

## Pagamento

O Sales Service não processa pagamentos diretamente.

No estado atual:

- pedidos entram com status financeiro pendente
- a confirmação comercial pode acontecer offline
- o processamento financeiro será feito futuramente pelo `payment-service`
- o pagamento deve ocorrer online, após a sincronização do pedido

## Cenários de Conectividade

### Cliente online e vendedor online

- cliente consulta ou importa o catálogo
- cliente monta o pedido
- vendedor confirma
- pedido pode ser sincronizado rapidamente
- evento financeiro pode ser publicado após o registro

### Cliente offline e vendedor online

- cliente usa catálogo salvo localmente
- cliente gera QR Code do pedido
- vendedor confirma
- vendedor sincroniza logo após a confirmação

### Cliente online e vendedor offline

- cliente monta o pedido
- vendedor confirma mesmo sem conexão
- pedido fica salvo localmente no aparelho do vendedor
- sincronização ocorre quando o vendedor voltar a ficar online

### Cliente offline e vendedor offline

- cliente usa catálogo local
- pedido é gerado por QR Code
- vendedor confirma offline
- pedido fica pendente no aparelho do vendedor
- quando houver conexão, o lote é sincronizado

## Segurança

A maioria dos endpoints exige JWT.

Regras principais:

- vendedor gerencia catálogo da própria loja
- vendedor sincroniza pedidos da própria loja
- cliente consulta seus próprios pedidos
- Sales Service usa o Auth Service para validar contexto do usuário

## Configuração

```yaml
server:
port: 8082

auth:
service-url: ${AUTH_SERVICE_URL:http://localhost:8081}

jwt:
secret: ${JWT_SECRET}
expiration-minutes: ${JWT_EXPIRATION_MINUTES:120}

spring:
datasource:
url: ${DB_URL}
username: ${DB_USERNAME}
password: ${DB_PASSWORD}

rabbitmq:
host: ${RABBITMQ_HOST:localhost}
port: ${RABBITMQ_PORT:5672}
username: ${RABBITMQ_USERNAME:guest}
password: ${RABBITMQ_PASSWORD:guest}
```

## Execução

```bash
cd signal-sales-service
mvnw spring-boot:run
```

Porta padrão:

```text
8082
```

Swagger:

```text
http://localhost:8082/swagger-ui.html
```

## Fluxo Recomendado no Mobile

1. O usuário autentica no Auth Service.
2. O mobile armazena o JWT.
3. O vendedor gerencia catálogo e estoque.
4. O cliente importa o catálogo.
5. O cliente monta o pedido e gera QR Code.
6. O vendedor escaneia e confirma a venda.
7. O pedido é salvo localmente.
8. Quando houver conexão, o vendedor sincroniza o pedido.
9. O Sales Service registra a operação e publica evento para pagamento futuro.

O fluxo principal não depende de ativação manual do modo offline.
