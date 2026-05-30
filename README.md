# OffPay

O OffPay é um aplicativo offline-first para continuidade de vendas em cenários de conectividade limitada. Este repositório contém a API de apoio responsável por autenticação, sincronização, validação e persistência remota das operações geradas no mobile.

O fluxo principal nasce no aplicativo: catálogo, pedidos e confirmações são salvos localmente primeiro. Quando houver conexão, a API recebe esses dados, valida as regras de negócio e mantém a consistência com o backend.

## Objetivo

O OffPay resolve o problema de interrupção de vendas em ambientes com internet instável. A proposta é permitir que a operação comercial continue no aplicativo mesmo sem conexão constante.

A API não substitui a operação local. Ela funciona como camada de sincronização, validação e persistência remota dos dados criados no mobile.

Na prática, isso significa:

- salvar dados localmente antes de depender do backend
- permitir importação e consulta de catálogo no aparelho
- permitir criação e confirmação de pedidos em cenários offline
- sincronizar catálogo e pedidos posteriormente
- manter consistência entre estado local e remoto com deduplicação e logs
- separar a confirmação comercial offline do processamento financeiro online

## Conceito Offline-First

No estado atual do OffPay, o fluxo principal não depende de ativação do modo offline.

Regras principais:

- o mobile salva dados localmente primeiro
- a API recebe sincronizações quando houver conectividade
- o JWT continua sendo a base de identidade entre mobile e backend
- o `deviceId` é usado para rastreabilidade do aparelho (talvez seja removido)
- pedidos offline podem ser sincronizados depois sem perder o contexto da venda

## Arquitetura

A API atual é organizada em microsserviços Spring Boot:

- `signal-auth-service`: identidade, autenticação, JWT e contexto de acesso
- `signal-sales-service`: sincronização, validação, catálogo, produtos, pedidos e publicação de eventos

Infraestrutura de apoio:

- Oracle Database para persistência relacional
- Flyway para versionamento de banco
- RabbitMQ para comunicação assíncrona entre serviços

A API atua como camada de sincronização e consistência. O produto principal é o aplicativo mobile, onde a operação comercial acontece localmente antes de ser enviada ao backend.

O nome oficial do projeto é OffPay. Os nomes das pastas dos serviços permanecem como estão no repositório, que eram a ideia original, "Signal".

## Microsserviços Atuais

## Auth Service

Responsável por:

- cadastro de vendedores e clientes
- autenticação com JWT
- identificação do usuário autenticado
- associação entre vendedor e loja
- exposição de `storeId`, `role` e `deviceId` para os demais serviços
- atualização opcional do identificador local do aparelho

Fluxo de integração:

- o mobile autentica no Auth Service
- o JWT é armazenado localmente
- o Sales Service consulta o contexto do usuário a partir do token
- o `storeId` do vendedor é usado para associar catálogo e pedidos

### Endpoints principais

| Método  | Endpoint                  | Finalidade                                     |
| ------- | ------------------------- | ---------------------------------------------- |
| `POST`  | `/auth/register/seller`   | Cadastra vendedor com loja                     |
| `POST`  | `/auth/register/customer` | Cadastra cliente                               |
| `POST`  | `/auth/login`             | Autentica usuário                              |
| `GET`   | `/auth/me`                | Retorna o contexto autenticado a partir do JWT |
| `PATCH` | `/device/me`              | Atualiza o `deviceId` do vendedor              |

### Exemplos de JSON

`POST /auth/register/seller`

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

`POST /auth/register/customer`

```json
{
    "name": "Maria Souza",
    "email": "maria.customer@email.com",
    "password": "123456",
    "cpf": "10987654321",
    "phone": "11988887777"
}
```

`POST /auth/login`

```json
{
    "email": "mateus.seller@email.com",
    "password": "123456"
}
```

Observações:

- `deviceId` no cadastro do vendedor é opcional
- `PATCH /device/me` continua disponível para atualizar o identificador local do aparelho
- endpoints legados de ativação offline ainda podem existir por compatibilidade, mas não fazem parte do fluxo principal

## Sales Service

Responsável por receber sincronizações do mobile, validar regras de negócio, persistir o estado remoto de catálogo e pedidos, controlar estoque e publicar eventos para processamento financeiro futuro.

Principais responsabilidades:

- categorias
- produtos
- catálogo da loja
- controle de estoque
- criação de pedidos online
- sincronização de pedidos offline
- sincronização de alterações de catálogo
- validações de negócio
- deduplicação de sincronizações
- publicação de eventos para processamento financeiro futuro

O `signal-sales-service` não é o ponto de início da operação comercial offline. A operação nasce no mobile. O serviço atua como sincronizador remoto, validador de regras de negócio e registrador final dos dados quando houver conexão.

## Endpoints de Catálogo

| Método   | Endpoint                                         | Finalidade                                       |
| -------- | ------------------------------------------------ | ------------------------------------------------ |
| `GET`    | `/category/me`                                   | Lista categorias da loja do vendedor autenticado |
| `GET`    | `/category/{id}`                                 | Consulta categoria da loja do vendedor           |
| `POST`   | `/category`                                      | Cria categoria                                   |
| `PUT`    | `/category/{id}`                                 | Atualiza categoria                               |
| `DELETE` | `/category/{id}`                                 | Desativa categoria                               |
| `GET`    | `/product`                                       | Lista produtos ativos                            |
| `GET`    | `/product/{id}`                                  | Consulta produto                                 |
| `GET`    | `/product/store/{storeId}`                       | Lista produtos de uma loja                       |
| `GET`    | `/product/store/{storeId}/category/{categoryId}` | Lista produtos por loja e categoria              |
| `GET`    | `/product/category/{categoryId}`                 | Lista produtos por categoria                     |
| `POST`   | `/product`                                       | Cria produto                                     |
| `PUT`    | `/product/{id}`                                  | Atualiza produto                                 |
| `DELETE` | `/product/{id}`                                  | Desativa produto                                 |
| `GET`    | `/catalog/me`                                    | Retorna catálogo da loja do vendedor autenticado |
| `GET`    | `/catalog/store/{storeId}`                       | Retorna catálogo de uma loja                     |
| `POST`   | `/catalog/sync`                                  | Sincroniza alterações locais de catálogo         |

### Exemplos de JSON

`POST /category`

```json
{
    "name": "Bebidas",
    "description": "Itens gelados e não alcoólicos"
}
```

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

`POST /catalog/sync`

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

## Endpoints de Pedidos

| Método | Endpoint                       | Finalidade                             |
| ------ | ------------------------------ | -------------------------------------- |
| `GET`  | `/order/me`                    | Lista pedidos do usuário autenticado   |
| `GET`  | `/order/{id}`                  | Consulta pedido por ID                 |
| `GET`  | `/order/store/{storeId}`       | Lista pedidos da loja                  |
| `GET`  | `/order/customer/{customerId}` | Lista pedidos do cliente               |
| `POST` | `/order`                       | Cria pedido online                     |
| `POST` | `/order/sync`                  | Sincroniza pedidos confirmados offline |

### Exemplos de JSON

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

## Papel da API no Fluxo Offline

A API não é o produto final usado diretamente pelo comerciante. O foco do OffPay está no aplicativo mobile.

A API atua como camada de apoio para:

- autenticar usuários
- validar contexto de loja, cliente e vendedor
- receber sincronizações do mobile
- aplicar regras de negócio
- evitar duplicidades
- manter persistência remota
- publicar eventos para etapas futuras de pagamento

O mobile continua sendo o centro da operação offline-first. A API entra quando há conectividade ou quando é necessário consolidar dados localmente gerados.

## Sincronização Offline e Online

## Sincronização de Catálogo

Fluxo:

1. o vendedor altera categorias, produtos ou estoque no mobile
2. a alteração é persistida localmente
3. a alteração entra em uma fila local
4. quando houver conexão, o mobile envia `POST /catalog/sync`
5. o Sales Service aplica, rejeita ou marca cada operação como duplicada
6. o mobile atualiza o estado local conforme o resultado

Garantias:

- cada alteração possui `operationId`
- `operationId` repetido não reaplica a operação
- falhas ficam registradas para nova tentativa ou tratamento manual

## Sincronização de Pedidos

Fluxo:

1. o cliente monta o pedido no aparelho
2. o vendedor confirma comercialmente a venda
3. o pedido é salvo localmente com `localOrderId`
4. quando houver conexão, o vendedor envia `POST /order/sync`
5. o Sales Service valida o lote, registra os pedidos e retorna o resultado item a item
6. pedidos sincronizados disparam evento para o fluxo financeiro futuro

Garantias:

- cada pedido offline usa `localOrderId` como referência de deduplicação
- pedidos duplicados não são recriados
- pedidos rejeitados mantêm feedback para tratamento no mobile

## Fluxo de Catálogo e Pedidos

Fluxo principal do catálogo:

1. o vendedor autentica e obtém o contexto da loja
2. o catálogo pode ser consultado por `GET /catalog/me`
3. o mobile salva localmente o catálogo e pode gerar QR Code da loja
4. o cliente importa esse catálogo para consulta local

Fluxo principal do pedido:

1. o cliente importa o catálogo da loja
2. o cliente monta um pedido
3. o aplicativo gera um QR Code do pedido
4. o vendedor escaneia e confirma a venda
5. o pedido fica salvo localmente
6. quando houver conexão, o pedido é sincronizado com o Sales Service
7. o backend valida a operação e publica evento para processamento financeiro futuro

## RabbitMQ e Comunicação Entre Serviços

O OffPay usa RabbitMQ para desacoplar o domínio comercial do domínio financeiro.

Configuração atual no `sales-service`:

- exchange: `offpay.sales.exchange`
- routing key: `payment.requested`

Fluxo atual:

1. um pedido online é criado ou um pedido offline é sincronizado
2. o `signal-sales-service` publica um evento `PaymentRequested`
3. o processamento financeiro fica desacoplado da confirmação comercial

Se a publicação do evento falhar, o pedido continua salvo. Isso preserva a operação comercial e evita perda de venda.

## Cenários de Conectividade

## Cliente online e vendedor online

- o cliente importa ou consulta o catálogo com conexão
- o pedido pode ser criado e validado rapidamente
- o backend recebe o registro sem depender de uma fila local longa
- o evento financeiro pode ser publicado logo após a criação do pedido

## Cliente offline e vendedor online

- o cliente usa o catálogo previamente importado
- o cliente monta o pedido localmente e gera o QR Code
- o vendedor escaneia e confirma a venda
- como o vendedor está online, o pedido pode ser sincronizado logo após a confirmação

## Cliente online e vendedor offline

- o cliente consegue consultar catálogo e montar o pedido
- o vendedor escaneia e confirma a venda mesmo sem conexão
- o pedido fica salvo localmente no dispositivo do vendedor
- a sincronização ocorre quando o vendedor voltar a ter conexão

## Cliente offline e vendedor offline

- o cliente usa o catálogo salvo localmente
- o pedido é gerado por QR Code
- o vendedor confirma a venda sem depender do backend naquele momento
- o pedido fica pendente até a reconexão
- quando a internet voltar, o vendedor sincroniza o lote no `sales-service`

Em todos os cenários, o princípio do OffPay é o mesmo: a confirmação comercial pode acontecer localmente; o processamento financeiro depende do fluxo online posterior.

## Payment Service

O próximo passo planejado é o `payment-service`.

Esse serviço ainda não está implementado como parte funcional do fluxo atual. Quando for introduzido, será responsável por:

- atuar como gateway de pagamento simulado
- consumir eventos publicados pelo `signal-sales-service`
- processar o fluxo financeiro apenas online
- atualizar o status financeiro das operações após a sincronização do pedido

Essa separação mantém duas responsabilidades distintas:

- confirmação comercial da venda, que pode ocorrer offline
- processamento financeiro, que acontece depois, em ambiente online

## Infraestrutura Local

Na raiz de `global-java`, o arquivo `docker-compose.yml` sobe o RabbitMQ local.

```bash
docker compose up -d
```

Painel de administração:

```text
http://localhost:15672
```

Credenciais padrão:

```text
guest
guest
```

## Execução dos Serviços

## Auth Service

```bash
cd signal-auth-service
mvnw spring-boot:run
```

- porta padrão: `8081`
- Swagger: `http://localhost:8081/swagger-ui.html`

## Sales Service

```bash
cd signal-sales-service
mvnw spring-boot:run
```

- porta padrão: `8082`
- Swagger: `http://localhost:8082/swagger-ui.html`

## Variáveis de Ambiente

Auth Service:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

Sales Service:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION_MINUTES=120
AUTH_SERVICE_URL=http://localhost:8081
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```
