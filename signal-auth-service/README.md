# SIGNAL Auth Service

Este documento explica como testar os principais fluxos atuais do serviço de autenticação do SIGNAL.

A API roda localmente em:

```
http://localhost:8081
```

A documentação Swagger fica disponível em:

```
http://localhost:8081/swagger-ui.html
```

---

# Fluxo 1: Cadastro de vendedor

O vendedor é o usuário responsável por uma loja.  
Ao se cadastrar, ele cria também a loja inicial.

## Endpoint

```
POST /auth/register/seller
```

## Body

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

## Resposta esperada

```
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "Mateus Lima",
"email": "mateus.seller@email.com",
"cpf": "12345678901",
"phone": "11999999999",
"role": "SELLER",
"storeName": "Mercado Signal"
}
}
```

---

# Fluxo 2: Cadastro de cliente

O cliente é o usuário comum que acessa lojas, monta carrinhos e gera pedidos.

## Endpoint

```
POST /auth/register/customer
```

## Body

```
{
"name": "João Silva",
"email": "joao.customer@email.com",
"password": "123456",
"cpf": "98765432100",
"phone": "11988888888"
}
```

## Resposta esperada

```
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "João Silva",
"email": "joao.customer@email.com",
"cpf": "98765432100",
"phone": "11988888888",
"role": "CUSTOMER",
"storeName": null
}
}
```

---

# Fluxo 3: Login

O login autentica o usuário e retorna apenas o JWT e os dados básicos do usuário.

O modo offline não é ativado no login.  
O token offline é gerado somente no fluxo de dispositivo.

## Endpoint

```
POST /auth/login
```

## Body para vendedor

```
{
"email": "mateus.seller@email.com",
"password": "123456"
}
```

## Body para cliente

```
{
"email": "joao.customer@email.com",
"password": "123456"
}
```

## Resposta esperada

```
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "Mateus Lima",
"email": "mateus.seller@email.com",
"cpf": "12345678901",
"phone": "11999999999",
"role": "SELLER",
"storeName": "Mercado Signal"
}
}
```

---

# Fluxo 4: Consultar usuário autenticado

Este endpoint serve para testar se o JWT está funcionando.

## Endpoint

```
GET /auth/me
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_AQUI
```

## Resposta esperada

```
{
"id": "uuid-do-usuario",
"name": "Mateus Lima",
"email": "mateus.seller@email.com",
"cpf": "12345678901",
"phone": "11999999999",
"role": "SELLER",
"storeName": "Mercado Signal",
"\_links": {
"self": {
"href": "http://localhost:8081/auth/me"
}
}
}
```

---

# Fluxo 5: Ativar modo offline

A ativação offline só pode ser feita por vendedores.

Este fluxo gera um `offlineToken` temporário para o dispositivo informado.  
Esse token deve ser salvo pelo mobile e usado futuramente para registrar vendas offline.

## Endpoint

```
POST /devices/{deviceId}/offline/activate
```

## Exemplo de URL

```
POST /devices/device-seller-001/offline/activate
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Resposta esperada

```
{
"deviceId": "device-seller-001",
"offlineToken": "uuid-token-offline",
"offlineExpiresAt": "2026-05-28T10:30:00",
"active": true
}
```

---

# Fluxo 6: Renovar modo offline

A renovação gera um novo `offlineToken` e uma nova data de expiração.

O token anterior deixa de ser o token válido do dispositivo.

## Endpoint

```
POST /devices/{deviceId}/offline/renew
```

## Exemplo de URL

```
POST /devices/device-seller-001/offline/renew
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Resposta esperada

```
{
"deviceId": "device-seller-001",
"offlineToken": "novo-token-offline",
"offlineExpiresAt": "2026-05-28T10:30:00",
"active": true
}
```

---

# Fluxo 7: Consultar um dispositivo

Este endpoint mostra se um dispositivo está ativo e se o modo offline ainda é válido.

Ele não retorna o `offlineToken`.

## Endpoint

```
GET /devices/{deviceId}
```

## Exemplo de URL

```
GET /devices/device-seller-001
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Resposta esperada

```
{
"deviceId": "device-seller-001",
"active": true,
"offlineEnabled": true,
"expired": false,
"offlineExpiresAt": "2026-05-28T10:30:00"
}
```

---

# Fluxo 8: Listar dispositivos do vendedor

Lista todos os dispositivos vinculados à conta do vendedor autenticado.

## Endpoint

```
GET /devices
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Resposta esperada

```
[
{
"deviceId": "device-seller-001",
"active": true,
"offlineEnabled": true,
"expired": false,
"offlineExpiresAt": "2026-05-28T10:30:00"
}
]
```

---

# Como testar o fluxo completo

1. Cadastre um vendedor.
2. Faça login com o vendedor.
3. Copie o JWT retornado.
4. Use o JWT no header `Authorization`.
5. Chame `/auth/me` para validar a autenticação.
6. Ative o modo offline em `/devices/{deviceId}/offline/activate`.
7. Guarde o `offlineToken` retornado.
8. Consulte `/devices/{deviceId}` para verificar se o modo offline está ativo.
9. Renove em `/devices/{deviceId}/offline/renew` quando precisar de um novo token.

---

# Regras importantes

O JWT autentica o usuário na API.

O `offlineToken` autoriza o dispositivo do vendedor a operar offline por tempo limitado.

O cliente não pode ativar modo offline.

O login não retorna `offlineToken`.

O `offlineToken` só é retornado na ativação ou renovação do modo offline.

As vendas offline ainda não são pagamentos confirmados. Elas serão salvas como pendentes e validadas posteriormente na sincronização.
