# SIGNAL

SIGNAL é uma plataforma offline-first criada para permitir que pequenos comerciantes continuem operando mesmo durante falhas de internet, apagões ou instabilidade de rede.

O sistema registra vendas localmente no dispositivo e sincroniza os dados posteriormente com os serviços centrais da aplicação.

## Objetivo

Permitir continuidade operacional em cenários críticos através de:

- vendas offline
- sincronização posterior
- auditoria das transações
- mensageria assíncrona
- carteira financeira simulada
- arquitetura baseada em microserviços

---

# Estrutura Atual

```
signal/
├── docker-compose.yml
├── signal-auth-service/
├── signal-sales-service/
├── signal-payment-service/
├── signal-ai-service/
└── signal-mobile/
```

---

# Microsserviços

## signal-auth-service

Responsável por:

- autenticação
- JWT
- cadastro de vendedores
- gerenciamento de dispositivos
- sessões offline

### Justificativa

Centraliza autenticação e segurança da plataforma, evitando acoplamento com os demais serviços.

---

## signal-sales-service

Responsável por:

- recebimento de vendas offline
- sincronização
- gerenciamento das transações
- publicação de eventos

### Justificativa

Permite processamento independente das vendas e facilita escalabilidade.

---

## signal-payment-service

Responsável por:

- gateway financeiro simulado
- aprovação/rejeição de transações
- carteira financeira
- saldo do vendedor

### Justificativa

Isola regras financeiras da aplicação principal.

---

## signal-ai-service

Responsável por:

- geração de resumos inteligentes
- interpretação operacional
- análises contextuais

### Justificativa

Mantém a camada de IA desacoplada da lógica principal do sistema.

---

# Mensageria

O projeto utiliza RabbitMQ para comunicação assíncrona entre serviços.

Exemplo:

```
Venda sincronizada
-> sales-service publica evento
-> payment-service processa transação
-> carteira é atualizada
```

---

# Como subir RabbitMQ localmente

Na raiz do projeto:

```
docker compose up -d
```

Painel RabbitMQ:

```
http://localhost:15672
```

Usuário padrão:

```
guest
guest
```

---

# Como executar o auth-service

Entrar na pasta:

```
cd signal-auth-service
```

Executar aplicação:

```
./mvnw spring-boot:run
```

---

# Configurar Variáveis de Ambiente

```
DB_URL=
DB_USERNAME=
DB_PASSWORD=

JWT_SECRET=
JWT_EXPIRATION_MINUTES=

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

---

# Endpoints Atuais

## Autenticação

```
POST /auth/register/seller
POST /auth/login
```

---

# Swagger

Documentação disponível em:

```
http://localhost:8081/swagger-ui.html
```

---

# Status Atual

Atualmente o projeto possui:

- estrutura inicial de microserviços
- autenticação JWT
- integração Oracle
- Flyway configurado
- RabbitMQ preparado
- Swagger/OpenAPI
- segurança com Spring Security
- estrutura offline-first em definição

---

# Próximos Passos

- finalização do fluxo de autenticação
- ativação de dispositivos
- catálogo de produtos
- persistência offline no mobile
- sincronização das vendas
- mensageria entre serviços
- carteira financeira simulada
- integração da camada de IA
