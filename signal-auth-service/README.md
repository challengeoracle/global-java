# SIGNAL Auth Service

Este documento explica os principais fluxos atuais do serviço de autenticação do SIGNAL e como ele dá suporte aos cenários offline do projeto.

A API roda localmente em:

```
http://localhost:8081
```

A documentação Swagger fica disponível em:

```
http://localhost:8081/swagger-ui.html
```

---

# Visão Geral

O `signal-auth-service` é responsável por autenticação, autorização e emissão das permissões necessárias para operação offline.

Ele não processa vendas nem pagamentos.  
A função dele é permitir que os próximos serviços consigam validar se vendedor e cliente estavam autorizados a operar no momento da venda.

O projeto trabalha com dois tipos de autorização offline:

```
SELLER -> token offline do dispositivo do vendedor
CUSTOMER -> token offline da sessão do cliente
```

---

# Autenticação

## Cadastro de vendedor

O vendedor é o usuário responsável por uma loja.

Ao se cadastrar, ele cria:

- usuário
- loja inicial
- dispositivo fixo vinculado à conta

O modo offline não é ativado no cadastro.  
O token offline só é gerado quando o vendedor ativa o modo offline.

Endpoint:

```
POST /auth/register/seller
```

Body:

```
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

---

## Cadastro de cliente

O cliente é o usuário comum que acessa lojas, monta carrinhos e gera pedidos.

O cliente não possui dispositivo fixo, mas pode ativar uma sessão offline temporária para montar pedidos e gerar QR Code sem internet.

Endpoint:

```
POST /auth/register/customer
```

Body:

```
{
  "name": "João Silva",
  "email": "joao.customer@email.com",
  "password": "123456",
  "cpf": "98765432100",
  "phone": "11988888888"
}
```

---

## Login

O login autentica o usuário e retorna o JWT.

O modo offline não é ativado no login.  
O login não retorna `offlineToken`.

Endpoint:

```
POST /auth/login
```

Body:

```
{
  "email": "mateus.seller@email.com",
  "password": "123456"
}
```

---

## Consultar usuário autenticado

Serve para validar o JWT e retornar os dados do usuário logado.

Endpoint:

```
GET /auth/me
```

Header:

```
Authorization: Bearer SEU_TOKEN_AQUI
```

Para vendedores, a resposta inclui o `deviceId` fixo vinculado à conta.  
Para clientes, o `deviceId` vem como `null`.

---

# Modo Offline do Vendedor

O vendedor precisa de um token offline porque ele é quem confirma oficialmente a venda.

No MVP:

```
1 vendedor = 1 dispositivo fixo autorizado
```

Esse dispositivo representa o caixa oficial da loja.

---

## Consultar dispositivo do vendedor

Endpoint:

```
GET /device/me
```

Header:

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

Esse endpoint mostra:

- device vinculado ao vendedor
- se o modo offline está ativo
- se a sessão offline expirou
- data de expiração do modo offline

---

## Ativar modo offline do vendedor

Endpoint:

```
POST /device/offline/activate
```

Header:

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

Body:

```
Não precisa enviar body.
```

Esse endpoint gera ou substitui o token offline do vendedor.

O mobile deve salvar localmente:

```
sellerOfflineToken
sellerOfflineExpiresAt
deviceId
```

---

## Atualizar dispositivo fixo do vendedor

Endpoint:

```
PATCH /device/me
```

Header:

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

Body:

```
{
  "deviceId": "novo-device-id"
}
```

Ao trocar o dispositivo, o token offline atual é apagado.  
O vendedor precisa ativar o modo offline novamente.

---

# Modo Offline do Cliente

O cliente precisa conseguir montar pedidos e gerar QR Code mesmo sem internet.

Por isso, o cliente possui uma sessão offline temporária.

Diferente do vendedor, o cliente não tem device fixo.

---

## Ativar sessão offline do cliente

Endpoint:

```
POST /customer/offline/activate
```

Header:

```
Authorization: Bearer SEU_TOKEN_DO_CLIENTE
```

Body:

```
Não precisa enviar body.
```

Esse endpoint gera ou substitui o token offline do cliente.

O mobile deve salvar localmente:

```
customerOfflineToken
customerOfflineExpiresAt
customerId
```

---

## Consultar sessão offline do cliente

Endpoint:

```
GET /customer/offline/me
```

Header:

```
Authorization: Bearer SEU_TOKEN_DO_CLIENTE
```

Esse endpoint mostra:

- se a sessão offline do cliente está ativa
- se expirou
- data de expiração da sessão offline

---

# Cobertura dos Cenários de Conexão

O auth-service prepara o sistema para os quatro cenários principais de transação.

Ele não executa a venda nem o pagamento.  
Ele apenas fornece os tokens que serão usados depois pelo `sales-service` e pelo `payment-service`.

---

## 1. Cliente offline e vendedor offline

Este é o principal cenário do projeto.

Antes da queda:

```
vendedor ativa /device/offline/activate
cliente ativa /customer/offline/activate
mobile salva os dois tokens localmente
```

Durante a queda:

```
cliente acessa catálogo cacheado
-> monta carrinho
-> gera QR do pedido com customerOfflineToken
-> vendedor escaneia QR
-> confirma venda com sellerOfflineToken
-> venda fica salva no SQLite
-> venda fica pendente de sincronização
```

Quando a internet volta:

```
sales-service valida pedido e venda
payment-service cria ou confirma pagamento fake
carteira do vendedor é atualizada se aprovado
```

Cobertura do auth-service:

```
sellerOfflineToken autoriza o vendedor offline
customerOfflineToken autoriza o cliente offline
```

---

## 2. Cliente online e vendedor offline

Nesse cenário, o cliente pode pagar na hora, mesmo que o vendedor esteja sem internet.

Fluxo:

```
cliente acessa catálogo
-> monta carrinho
-> paga pelo gateway fake
-> gera QR do pedido pago
-> vendedor offline escaneia
-> vendedor salva venda localmente
-> vendedor sincroniza depois
-> backend valida pagamento já realizado
```

Cobertura do auth-service:

```
cliente usa JWT online
vendedor usa sellerOfflineToken
```

Regra:

```
cliente online + vendedor offline = cliente pode pagar, vendedor sincroniza depois
```

---

## 3. Cliente offline e vendedor online

Nesse cenário, o cliente não consegue pagar imediatamente, mas o vendedor consegue registrar a venda no backend.

Fluxo:

```
cliente acessa catálogo cacheado
-> monta carrinho offline
-> gera QR do pedido com customerOfflineToken
-> vendedor online escaneia
-> vendedor confirma venda
-> sales-service registra venda imediatamente
-> payment-service deixa pagamento pendente
-> cliente paga depois quando recuperar conexão
```

Cobertura do auth-service:

```
cliente usa customerOfflineToken
vendedor usa JWT online
```

Regra:

```
cliente offline + vendedor online = venda pode ser registrada, pagamento fica pendente
```

---

## 4. Cliente online e vendedor online

Cenário normal.

Fluxo:

```
cliente monta carrinho
-> cliente paga pelo gateway fake
-> vendedor confirma venda
-> sales-service valida imediatamente
-> payment-service aprova ou rejeita
-> carteira do vendedor é atualizada
```

Cobertura do auth-service:

```
cliente usa JWT
vendedor usa JWT
```

Regra:

```
ambos online = fluxo imediato
```

---

# Regras Importantes

O JWT autentica o usuário online.

O `sellerOfflineToken` autoriza o vendedor a confirmar vendas offline por tempo limitado.

O `customerOfflineToken` autoriza o cliente a montar pedidos offline por tempo limitado.

O login não retorna token offline.

Tokens offline só são gerados nas rotas específicas de ativação offline.

O vendedor possui um dispositivo fixo no MVP.

O cliente não possui dispositivo fixo.

As vendas offline não são pagamentos confirmados automaticamente.

A validação real da venda será feita posteriormente pelo `sales-service`.

O gateway de pagamento fake será implementado no `signal-payment-service`.

---

# Endpoints Atuais

## Auth

```
POST /auth/register/seller
POST /auth/register/customer
POST /auth/login
GET  /auth/me
```

## Offline do vendedor

```
GET   /device/me
POST  /device/offline/activate
PATCH /device/me
```

## Offline do cliente

```
POST /customer/offline/activate
GET  /customer/offline/me
```

---

# Segurança

Rotas públicas:

```
POST /auth/register/seller
POST /auth/register/customer
POST /auth/login
/swagger-ui.html
/v3/api-docs
```

Rotas autenticadas:

```
GET /auth/me
```

Rotas exclusivas para vendedores:

```
GET   /device/me
POST  /device/offline/activate
PATCH /device/me
```

Rotas exclusivas para clientes:

```
POST /customer/offline/activate
GET  /customer/offline/me
```

---

# Como testar o fluxo do vendedor

1. Cadastre um vendedor.
2. Faça login como vendedor.
3. Copie o JWT.
4. Chame `/auth/me` para validar o token.
5. Chame `/device/me` para consultar o dispositivo fixo.
6. Chame `/device/offline/activate`.
7. Salve o `sellerOfflineToken` no mobile.

---

# Como testar o fluxo do cliente

1. Cadastre um cliente.
2. Faça login como cliente.
3. Copie o JWT.
4. Chame `/auth/me` para validar o token.
5. Chame `/customer/offline/activate`.
6. Salve o `customerOfflineToken` no mobile.
7. Chame `/customer/offline/me` para verificar validade da sessão.

---

# Testes de Segurança Recomendados

Verificar:

```
cliente tentando acessar /device/me deve receber 403
cliente tentando ativar /device/offline/activate deve receber 403
vendedor tentando acessar /customer/offline/me deve receber 403
vendedor tentando ativar /customer/offline/activate deve receber 403
token inválido deve receber 401
token expirado deve receber 401
```

---

# Papel dos Próximos Microsserviços

## sales-service

Será responsável por:

```
catálogo
carrinho
pedido
venda
sincronização
validação da venda offline
publicação de eventos
```

## payment-service

Será responsável por:

```
gateway fake
intenção de pagamento
pagamento pendente
pagamento aprovado
pagamento rejeitado
carteira simulada
saldo do vendedor
```

## audit-service .NET

Será responsável por:

```
logs
auditoria
rastreabilidade
modelo NoSQL
validação complementar de integridade
```
