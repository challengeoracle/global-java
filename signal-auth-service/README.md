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

Ao se cadastrar, ele cria:

- usuário
- loja inicial
- dispositivo fixo vinculado à conta

O modo offline não é ativado no cadastro.  
O token offline só é gerado quando o vendedor ativa o modo offline.

## Endpoint

```
POST /auth/register/seller
```

## Body

```json
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

```json
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "Mateus Lima",
"email": "mateus.seller@email.com",
"cpf": "12345678901",
"phone": "11999999999",
"role": "SELLER",
"storeName": "Mercado Signal",
"deviceId": "device-seller-001"
    }
}
```

---

# Fluxo 2: Cadastro de cliente

O cliente é o usuário comum que acessa lojas, monta carrinhos e gera pedidos.

Clientes não possuem dispositivo offline.

## Endpoint

```
POST /auth/register/customer
```

## Body

```json
{
"name": "João Silva",
"email": "joao.customer@email.com",
"password": "123456",
"cpf": "98765432100",
"phone": "11988888888"
}
```

## Resposta esperada

```json
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "João Silva",
"email": "joao.customer@email.com",
"cpf": "98765432100",
"phone": "11988888888",
"role": "CUSTOMER",
"storeName": null,
"deviceId": null
    }
}
```

---

# Fluxo 3: Login

O login autentica o usuário e retorna apenas o JWT e os dados do usuário.

O modo offline não é ativado no login.  
O `offlineToken` não aparece nessa resposta.

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

```json
{
"email": "joao.customer@email.com",
"password": "123456"
}
```

## Resposta esperada para vendedor

```json
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "Mateus Lima",
"email": "mateus.seller@email.com",
"cpf": "12345678901",
"phone": "11999999999",
"role": "SELLER",
"storeName": "Mercado Signal",
"deviceId": "device-seller-001"
    }
}
```

## Resposta esperada para cliente

```json
{
"token": "jwt-gerado",
"user": {
"id": "uuid-do-usuario",
"name": "João Silva",
"email": "joao.customer@email.com",
"cpf": "98765432100",
"phone": "11988888888",
"role": "CUSTOMER",
"storeName": null,
"deviceId": null
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

## Resposta esperada para vendedor

```json
{
"id": "uuid-do-usuario",
"name": "Mateus Lima",
"email": "mateus.seller@email.com",
"cpf": "12345678901",
"phone": "11999999999",
"role": "SELLER",
"storeName": "Mercado Signal",
"deviceId": "device-seller-001",
"\_links": {
"self": {
"href": "http://localhost:8081/auth/me"
      }
    }
}
```

## Resposta esperada para cliente

```json
{
"id": "uuid-do-usuario",
"name": "João Silva",
"email": "joao.customer@email.com",
"cpf": "98765432100",
"phone": "11988888888",
"role": "CUSTOMER",
"storeName": null,
"deviceId": null,
"\_links": {
"self": {
"href": "http://localhost:8081/auth/me"
    }
  }
}
```

---

# Fluxo 5: Consultar dispositivo do vendedor

Este endpoint retorna o dispositivo fixo vinculado ao vendedor autenticado.

Clientes não podem acessar este endpoint.

## Endpoint

```
GET /device/me
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Resposta esperada antes de ativar modo offline

```json
{
"deviceId": "device-seller-001",
"active": true,
"offlineEnabled": false,
"expired": true,
"offlineExpiresAt": null
}
```

## Resposta esperada depois de ativar modo offline

```json
{
"deviceId": "device-seller-001",
"active": true,
"offlineEnabled": true,
"expired": false,
"offlineExpiresAt": "2026-05-28T10:30:00"
}
```

---

# Fluxo 6: Ativar modo offline

A ativação offline só pode ser feita por vendedores.

Este endpoint gera um `offlineToken` temporário para o dispositivo fixo do vendedor.

Se o vendedor já tiver um token offline, este endpoint substitui o token antigo por um novo.

## Endpoint

```
POST /device/offline/activate
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Body

Não precisa enviar body.

## Resposta esperada

```json
{
"deviceId": "device-seller-001",
"offlineToken": "uuid-token-offline",
"offlineExpiresAt": "2026-05-28T10:30:00",
"active": true
}
```

---

# Fluxo 7: Atualizar dispositivo fixo do vendedor

Este endpoint permite trocar o `deviceId` vinculado ao vendedor.

Ao trocar o dispositivo, o token offline atual é apagado e o vendedor precisa ativar o modo offline novamente.

## Endpoint

```
PATCH /device/me
```

## Header obrigatório

```
Authorization: Bearer SEU_TOKEN_DO_VENDEDOR
```

## Body

```json
{
"deviceId": "novo-device-id"
}
```

## Resposta esperada

```json
{
"deviceId": "novo-device-id",
"active": true,
"offlineEnabled": false,
"expired": true,
"offlineExpiresAt": null
}
```

---

# Como testar o fluxo completo

1. Cadastre um vendedor em `/auth/register/seller`.
2. Faça login em `/auth/login`.
3. Copie o JWT retornado.
4. Use o JWT no header `Authorization`.
5. Chame `/auth/me` para validar a autenticação.
6. Chame `/device/me` para ver o dispositivo fixo do vendedor.
7. Chame `/device/offline/activate` para ativar o modo offline.
8. Guarde o `offlineToken` retornado no mobile.
9. Chame `/device/me` novamente para verificar se o modo offline está ativo.
10. Use `PATCH /device/me` apenas se precisar trocar o dispositivo fixo do vendedor.

---

# Regras importantes

O JWT autentica o usuário na API.

O `offlineToken` autoriza o dispositivo do vendedor a operar offline por tempo limitado.

O cliente não possui dispositivo offline.

O cliente não pode ativar modo offline.

O vendedor possui apenas um dispositivo fixo no MVP.

O login não retorna `offlineToken`.

O `offlineToken` só é retornado em `/device/offline/activate`.

As vendas offline ainda não são pagamentos confirmados.

As vendas offline serão salvas como pendentes e validadas posteriormente na sincronização.
