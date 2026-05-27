# SIGNAL

SIGNAL é uma plataforma offline-first criada para permitir que pequenos comerciantes continuem operando mesmo durante falhas de internet, apagões ou instabilidade de rede.

A proposta do projeto é manter a operação comercial funcionando em cenários críticos. O vendedor consegue operar com um dispositivo autorizado, o cliente consegue montar pedidos mesmo sem conexão, e as vendas podem ser sincronizadas posteriormente quando a internet voltar.

O diferencial do projeto é permitir que a venda continue acontecendo mesmo quando cliente, vendedor ou ambos estiverem offline.

---

# Objetivo

Permitir continuidade operacional para pequenos comércios através de:

- autenticação segura
- operação offline para vendedor
- operação offline para cliente
- dispositivo fixo autorizado para o vendedor
- sessão offline para montagem de pedidos
- vendas pendentes de sincronização
- sincronização posterior
- validação de transações offline
- gateway de pagamento simulado
- carteira financeira simulada
- auditoria das operações
- mensageria assíncrona
- arquitetura baseada em microserviços

---

# Estrutura Geral

```
signal/
├── docker-compose.yml
├── signal-auth-service/
├── signal-sales-service/
├── signal-payment-service/
├── signal-ai-service/
└── signal-mobile/
```

Observação: atualmente o serviço mais avançado é o `signal-auth-service`. Os demais microsserviços ainda serão desenvolvidos conforme a evolução do projeto.

---

# Stack Geral

## Backend

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Flyway
- Oracle Database
- RabbitMQ
- Swagger/OpenAPI
- HATEOAS

## Mobile

- React Native
- Expo
- Expo Router
- SQLite
- SecureStore

## Infraestrutura Local

- Docker Compose
- RabbitMQ com painel de gerenciamento

---

# Microsserviços

## signal-auth-service

Status: em desenvolvimento avançado / MVP praticamente fechado.

Responsável por:

- cadastro de vendedores e clientes
- autenticação com JWT
- controle de permissões por perfil
- vínculo entre vendedor e loja
- vínculo entre vendedor e dispositivo fixo
- ativação de sessão offline do vendedor
- futura ativação de sessão offline do cliente

No MVP, cada vendedor possui apenas um dispositivo fixo autorizado. Esse dispositivo representa o caixa oficial da loja.

O cliente também deverá possuir uma sessão offline própria, não como device fixo, mas como autorização temporária para montar pedidos e gerar QR Code mesmo sem internet.

---

## signal-sales-service

Status: ainda será desenvolvido.

Responsável por:

- produtos
- catálogo da loja
- carrinho
- geração de pedidos
- registro de vendas offline
- vendas pendentes de sincronização
- validação inicial das vendas
- publicação de eventos para mensageria

Fluxo esperado:

```
pedido/venda criado no mobile
-> venda fica pendente localmente quando necessário
-> conexão retorna
-> mobile sincroniza com sales-service
-> sales-service valida a venda
-> evento é publicado no RabbitMQ
```

---

## signal-payment-service

Status: ainda será desenvolvido.

Responsável por:

- gateway financeiro simulado
- criação de intenção de pagamento
- aprovação ou rejeição fake de transações
- confirmação de pagamento
- carteira financeira simulada
- saldo do vendedor
- extrato
- saque fake

Este serviço será o responsável por simular o comportamento de um gateway de pagamento real dentro do projeto.

Fluxo esperado:

```
pedido aprovado ou pagamento confirmado
-> payment-service processa transação fake
-> carteira do vendedor é atualizada
-> transação aparece no extrato
```

---

## signal-ai-service

Status: ainda será desenvolvido.

Responsável por:

- resumos operacionais
- análise de vendas pendentes
- alertas inteligentes
- apoio ao dashboard do vendedor

Exemplo esperado:

```
"Sua loja possui vendas pendentes de sincronização e o modo offline expira em poucas horas."
```

---

## audit-service .NET

Status: ainda será desenvolvido.

Responsável por:

- auditoria das operações
- logs estruturados
- rastreabilidade
- validação complementar de integridade
- modelo NoSQL em JSON

---

# Conceito Principal de Operação

O SIGNAL trabalha com dois tipos de autorização offline:

## Vendedor

O vendedor precisa de autorização offline porque ele é quem confirma oficialmente a venda.

```
vendedor ativa modo offline
-> backend gera offlineToken do vendedor
-> mobile salva offlineToken e expiração
-> vendedor pode confirmar vendas sem internet
```

## Cliente

O cliente precisa conseguir montar pedidos mesmo sem internet.

```
cliente ativa ou possui sessão offline
-> mobile salva token/sessão offline do cliente
-> cliente acessa catálogo cacheado
-> cliente monta carrinho
-> cliente gera QR Code do pedido
```

A venda só será considerada definitiva depois da validação feita pelos serviços centrais.

---

# Cenários de Conexão

O projeto considera quatro cenários principais de conexão.

---

## 1. Cliente offline e vendedor offline

Este é o cenário principal do projeto.

Mesmo sem internet para nenhum dos lados, a venda não é perdida.

Fluxo:

```
vendedor já ativou modo offline anteriormente
-> cliente possui catálogo cacheado/local
-> vendedor mostra QR da loja
-> cliente escaneia QR
-> cliente acessa catálogo local
-> cliente monta carrinho
-> cliente gera QR do pedido
-> vendedor escaneia QR do pedido
-> vendedor confirma venda
-> venda é salva no SQLite
-> venda fica pendente de sincronização
-> internet volta depois
-> backend valida
-> cliente realiza pagamento ou pagamento fica pendente conforme regra do gateway fake
-> vendedor recebe confirmação após sincronização
```

Resultado:

```
a venda fica preservada mesmo sem internet
o pagamento não é confirmado na hora
a transação será validada depois
```

Regra:

```
ambos offline = venda pendente e pagamento pendente
```

---

## 2. Cliente online e vendedor offline

Neste cenário, o cliente tem internet, mas o vendedor não.

O cliente pode pagar diretamente pelo gateway falso, mas o vendedor só receberá a confirmação quando sincronizar.

Fluxo:

```
vendedor está offline
-> cliente acessa catálogo
-> cliente monta carrinho
-> cliente paga pelo gateway fake
-> payment-service gera confirmação fake de pagamento
-> cliente gera QR do pedido pago
-> vendedor escaneia QR
-> vendedor confirma venda localmente
-> venda é salva no SQLite do vendedor
-> venda fica pendente de sincronização
-> vendedor reconecta depois
-> backend valida pagamento e venda
-> carteira do vendedor recebe saldo
```

Resultado:

```
cliente já paga
vendedor salva a venda offline
confirmação final acontece depois da sincronização do vendedor
```

Regra:

```
cliente online + vendedor offline = cliente pode pagar, vendedor sincroniza depois
```

---

## 3. Cliente offline e vendedor online

Neste cenário, o cliente não tem internet, mas o vendedor está conectado.

O cliente gera o pedido offline, e o vendedor pode iniciar o pagamento ou registrar a cobrança imediatamente.

Fluxo:

```
cliente acessa catálogo cacheado
-> cliente monta carrinho offline
-> cliente gera QR do pedido
-> vendedor online escaneia QR
-> vendedor confirma venda
-> backend recebe venda imediatamente
-> payment-service gera cobrança/pagamento pendente
-> cliente escaneia QR de pagamento quando possível
-> pagamento fica pendente até o cliente conseguir pagar
```

Resultado:

```
venda chega ao backend na hora
pagamento pode ficar pendente do lado do cliente
```

Regra:

```
cliente offline + vendedor online = vendedor registra a venda, mas pagamento pode ficar pendente para o cliente
```

---

## 4. Cliente online e vendedor online

Este é o cenário normal de operação.

Fluxo:

```
cliente acessa catálogo
-> cliente monta carrinho
-> cliente paga pelo gateway fake
-> cliente gera QR do pedido pago
-> vendedor escaneia QR
-> vendedor confirma venda
-> backend valida imediatamente
-> payment-service aprova ou rejeita
-> carteira do vendedor é atualizada
```

Resultado:

```
transação ocorre normalmente
validação acontece na hora
carteira é atualizada após aprovação
```

Regra:

```
ambos online = fluxo completo imediato
```

---

# Regras de Pagamento

O projeto utilizará um gateway de pagamento simulado no `signal-payment-service`.

Nenhum pagamento real será processado no MVP.

O gateway fake será responsável por:

- criar intenção de pagamento
- aprovar transação simulada
- rejeitar transação simulada
- gerar status de pagamento pendente
- confirmar pagamento fake
- atualizar carteira simulada do vendedor

Estados possíveis de pagamento:

```
PENDING_PAYMENT
PAID
APPROVED
REJECTED
PENDING_SYNC
```

---

# Regras Offline

Uma venda offline não é considerada pagamento confirmado automaticamente.

Ela pode representar:

```
pedido pendente
pagamento pendente
pagamento já feito pelo cliente online
venda aguardando sincronização do vendedor
```

A validação posterior deve verificar:

- vendedor existe
- loja existe
- dispositivo do vendedor estava autorizado
- sessão offline do vendedor estava válida
- cliente existe
- sessão offline do cliente estava válida quando necessário
- produtos existem
- total da venda está correto
- pedido não foi duplicado
- pagamento fake existe quando informado
- status de pagamento é compatível com o cenário

---

# Mensageria

O projeto utiliza RabbitMQ para comunicação assíncrona entre os microsserviços.

No momento, o RabbitMQ já está preparado via Docker Compose. A mensageria será usada principalmente quando os serviços de vendas e pagamento forem implementados.

Fluxo esperado:

```
sales-service publica evento de venda aprovada
-> payment-service consome evento
-> carteira do vendedor é atualizada
```

Outro fluxo possível:

```
payment-service confirma pagamento
-> sales-service recebe status atualizado
-> venda muda de PENDING_PAYMENT para PAID
```

---

# Banco de Dados

O projeto utiliza Oracle Database com Flyway para versionamento da estrutura relacional.

Tabelas atuais do `signal-auth-service`:

```
TB_USERS
TB_STORES
TB_DEVICES
```

Tabelas previstas para os próximos serviços:

```
TB_CUSTOMER_OFFLINE_SESSIONS
TB_PRODUCTS
TB_SALES
TB_SALE_ITEMS
TB_SYNC_BATCHES
TB_PAYMENT_INTENTS
TB_WALLETS
TB_WALLET_TRANSACTIONS
TB_WITHDRAW_REQUESTS
```

---

# Requisitos de Banco Relacional

Os requisitos de PL/SQL serão distribuídos entre os serviços.

## Auth Service

Responsável por regras ligadas a usuário, loja, dispositivo e sessões offline.

Itens planejados:

```
PRC_DISABLE_DEVICE
FN_OFFLINE_SESSION_VALID
TRG_USERS_NORMALIZE_EMAIL
```

Possível extensão para o novo fluxo do cliente:

```
FN_CUSTOMER_OFFLINE_SESSION_VALID
```

## Sales Service

Responsável pela maior parte das regras de venda offline.

Itens planejados:

```
PKG_SIGNAL_SALES
PRC_REGISTER_OFFLINE_SALE
PRC_PROCESS_PENDING_SALES
FN_CALCULATE_SALE_TOTAL
FN_VALIDATE_SALE_LIMIT
TRG_SALE_VALIDATE_TOTAL
TRG_PRODUCT_PREVENT_NEGATIVE_PRICE
```

## Payment Service

Responsável por regras de carteira simulada e gateway fake.

Itens planejados:

```
PRC_CREDIT_WALLET
PRC_CREATE_PAYMENT_INTENT
FN_GET_AVAILABLE_BALANCE
TRG_PREVENT_NEGATIVE_WALLET
```

## Audit Service .NET

Responsável pelo modelo NoSQL em JSON e rastreabilidade das operações.

---

# Como subir a infraestrutura local

Na raiz do projeto, execute:

```
docker compose up -d
```

Esse comando sobe o RabbitMQ localmente.

Painel RabbitMQ:

```
http://localhost:15672
```

Credenciais padrão:

```
guest
guest
```

---

# Como executar o auth-service

Entrar na pasta do serviço:

```
cd signal-auth-service
```

Executar no Linux/macOS:

```
./mvnw spring-boot:run
```

Executar no Windows:

```
mvnw spring-boot:run
```

Swagger do serviço:

```
http://localhost:8081/swagger-ui.html
```

---

# Variáveis de Ambiente

Configure as variáveis necessárias para o serviço Java.

```
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

Exemplo de configuração local:

```
DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha

JWT_SECRET=sua_chave_base64
JWT_EXPIRATION_MINUTES=120

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

---

# Portas Previstas

```
signal-auth-service: 8081
signal-sales-service: 8082
signal-payment-service: 8083
signal-ai-service: 8084
RabbitMQ: 5672
RabbitMQ Management: 15672
```

---

# Status Atual

Atualmente o projeto possui:

- estrutura inicial de microserviços
- Docker Compose com RabbitMQ
- auth-service em estágio avançado
- autenticação com JWT
- cadastro de vendedores e clientes
- vínculo entre vendedor, loja e dispositivo fixo
- ativação de sessão offline do vendedor
- Oracle configurado
- Flyway configurado
- Swagger/OpenAPI
- Spring Security
- HATEOAS básico
- tratamento de exceptions

---

# Ajustes Necessários no Auth Service

Para suportar completamente o fluxo em que cliente e vendedor podem operar offline, o `auth-service` ainda deverá receber:

```
TB_CUSTOMER_OFFLINE_SESSIONS
POST /customer/offline/activate
GET /customer/offline/me
```

Esse ajuste permitirá que o cliente também possua uma sessão offline válida para gerar pedidos por QR Code.

---

# Próximos Passos

## Próximo foco técnico

Finalizar o `auth-service` para contemplar sessão offline do cliente.

Prioridades:

- tabela de sessão offline do cliente
- endpoint para ativar sessão offline do cliente
- endpoint para consultar sessão offline do cliente
- resposta clara para o mobile salvar token e expiração

## Depois

Desenvolver o `signal-sales-service`.

Prioridades:

- produtos
- catálogo
- carrinho
- vendas offline
- pedidos pendentes
- itens de venda
- sincronização posterior
- publicação de eventos no RabbitMQ

## Depois

Desenvolver o `signal-payment-service`.

Prioridades:

- gateway fake
- intenção de pagamento
- status de pagamento
- carteira simulada
- saldo
- extrato
- aprovação/rejeição fake
- crédito após venda aprovada

## Depois

Desenvolver o `signal-ai-service`.

Prioridades:

- resumo operacional
- análise simples de vendas pendentes
- apoio ao dashboard

## Depois

Desenvolver o `audit-service` em .NET.

Prioridades:

- auditoria
- logs NoSQL
- validação complementar de integridade

---

# Observação

O projeto ainda está em evolução.

A regra principal do SIGNAL é que a venda não deve ser perdida por falta de internet. Dependendo da conexão de cada lado, o pagamento pode ocorrer imediatamente, ficar pendente ou ser confirmado somente após sincronização.

O gateway de pagamento será simulado em um microsserviço separado.
