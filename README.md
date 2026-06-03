# OFFPAY

Projeto desenvolvido para manter a operação de pequenos comércios funcionando mesmo em cenários de instabilidade de rede, com arquitetura baseada em microsserviços, mensageria e abordagem offline-first.

## Integrantes do grupo

- `RM561061` – Arthur Thomas Mariano de Souza
- `RM559873` – Davi Cavalcanti Jorge
- `RM559728` – Mateus da Silveira Lima

## Problema abordado

Pequenos comércios perdem vendas quando a internet falha no momento do pedido, do pagamento ou da consulta ao catálogo. Em cenários de instabilidade, o vendedor fica sem acesso confiável aos pedidos, ao saldo da carteira e ao processamento do pagamento. Isso gera fila, retrabalho e risco operacional.

O projeto OffPay trata esse problema com uma abordagem offline-first. A operação da loja continua mesmo sem conectividade total, registrando pedidos e pagamentos localmente, mantendo evidências para sincronização posterior e reduzindo o impacto de falhas na rede.

## Objetivos da solução

- Permitir que vendedores continuem vendendo offline
- Sincronizar pedidos, catálogo e eventos quando a conectividade retornar
- Registrar pagamentos com rastreabilidade e tratamento assíncrono
- Controlar carteira financeira de clientes e lojas
- Manter trilha operacional via outbox para integração entre serviços
- Armazenar base de conhecimento para IA com chunks pesquisáveis

## Arquitetura da solução

O OffPay é composto por quatro microsserviços principais:

| Serviço | Porta | Papel |
|--------|-------|-------|
| `signal-auth-service` | `8081` | autenticação, login, JWT e identidade |
| `signal-sales-service` | `8082` | catálogo, categorias, produtos, pedidos e sincronização |
| `signal-payment-service` | `8083` | carteira, transações e processamento financeiro |
| `signal-analytics-ai-service` | `8084` | analytics, insights e respostas em linguagem natural |

Infraestrutura local utilizada:

| Componente | Porta | Papel |
|-----------|-------|-------|
| RabbitMQ | `5672` | mensageria entre serviços |
| RabbitMQ Management | `15672` | painel de administração |
| Oracle externo | configurado em `DB_URL` | persistência de dados |

## Justificativa da arquitetura de microsserviços

A arquitetura de microsserviços foi adotada porque o problema do OffPay envolve domínios distintos, com responsabilidades bem separadas e necessidades diferentes de processamento.

- o `signal-auth-service` concentra autenticação, identidade, Spring Security e JWT
- o `signal-sales-service` isola a operação comercial, catálogo, pedidos e sincronização offline-first
- o `signal-payment-service` separa a regra financeira, carteira e processamento assíncrono de pagamentos
- o `signal-analytics-ai-service` fica responsável pela leitura de contexto, consolidação de dados e uso de Spring AI

Essa divisão reduz acoplamento, facilita manutenção, melhora a clareza das responsabilidades e justifica o uso de comunicação entre serviços em uma solução com autenticação, operação comercial, pagamentos e inteligência analítica.

Além disso, a arquitetura atende ao cenário da GS porque:

- permite persistência relacional compartilhando o Oracle como base de dados da solução
- utiliza mensageria com RabbitMQ em pontos que exigem rastreabilidade e processamento assíncrono
- usa clientes HTTP com Feign para integração explícita entre APIs internas
- separa a parte transacional da parte analítica e de IA, evitando misturar regras críticas com processamento contextual

## Recursos técnicos aplicados na solução

O projeto foi estruturado para atender aos requisitos técnicos da disciplina com funcionalidade real, e não apenas com operações CRUD simples.

- API REST com Spring Boot em todos os microsserviços
- persistência em banco de dados relacional Oracle
- controle de acesso com Spring Security e tokens JWT
- documentação com Swagger e OpenAPI
- uso de HATEOAS em endpoints selecionados
- uso de cache onde faz sentido para reduzir custo de leitura
- suporte a CORS para integração com clientes externos
- mensageria com RabbitMQ para eventos e processamento assíncrono
- uso de Feign para comunicação entre microsserviços
- uso de Spring AI no `signal-analytics-ai-service`, com recursos de tooling, MCP e recuperação de contexto em PDF
- funcionalidade real voltada a operação offline-first, sincronização, carteira, pagamentos e geração de insights

## Fluxo resumido

1. O usuário se autentica no `signal-auth-service`
2. O vendedor opera catálogo e pedidos no `signal-sales-service`
3. O `signal-payment-service` processa pagamentos e carteira
4. O `signal-analytics-ai-service` consolida informações e gera insights

Quando há falha de conectividade, a aplicação mantém o registro da operação e sincroniza os dados quando a rede retorna.

## Setup do projeto

Este repositório deve ser executado como solução completa. O microsserviço de IA depende dos demais serviços para funcionar corretamente, então o professor e a equipe precisam subir o projeto inteiro.

### Pré-requisitos

- Docker Desktop instalado
- Docker Desktop em execução
- Git instalado

### Passo a passo

1. Clone o repositório:

```bash
git clone https://github.com/challengeoracle/global-java
```

2. Entre na pasta do projeto:

```bash
cd global-java
```

3. Copie o arquivo de variáveis de ambiente:

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

4. Ajuste no `.env` pelo menos estas variáveis:

- `JWT_SECRET`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `GROQ_API_KEY` caso queira testar o OffPay Insights com IA

Observação:

- o fluxo padrão deste projeto já assume Oracle externo, como o Oracle da FIAP
- para esse cenário, o `RABBITMQ_HOST` continua como `rabbitmq`, porque o RabbitMQ sobe em container e os microsserviços se comunicam com ele pela rede do Docker

5. Suba todos os serviços na raiz do projeto:

```powershell
docker compose up -d --build
```

6. Se preferir, use os atalhos prontos:

No Windows:

```powershell
./scripts/start-local.ps1
```

No Linux/macOS:

```bash
./scripts/start-local.sh
```

7. Acompanhe a inicialização:

```powershell
docker compose logs -f
```

Ou use os atalhos:

No Windows:

```powershell
./scripts/logs.ps1
./scripts/logs.ps1 -Service signal-auth-service
```

No Linux/macOS:

```bash
./scripts/logs.sh
./scripts/logs.sh signal-auth-service
```

8. Acesse os serviços:

- Auth: `http://localhost:8081/swagger-ui.html`
- Sales: `http://localhost:8082/swagger-ui.html`
- Payment: `http://localhost:8083/swagger-ui.html`
- Analytics AI: `http://localhost:8084/swagger-ui.html`
- RabbitMQ Management: `http://localhost:15672`

### Encerramento

```powershell
docker compose down
```

Para remover também o volume do banco local opcional:

```powershell
docker compose down -v
```

## Banco de dados

Por padrão, o projeto está preparado para usar um Oracle externo via `DB_URL`, como o da FIAP.

Se você quiser usar um Oracle local em container, o `docker compose` também suporta isso com o profile `local-db`:

```powershell
docker compose --profile local-db up -d --build
```

Nesse modo, ele sobe um Oracle Free local para testes.

## Estrutura DevOps

- `docker-compose.yml`: orquestração local completa
- `dockerfiles/`: Dockerfiles de cada microsserviço
- `scripts/init-local-oracle.sh`: bootstrap do banco local opcional
- `scripts/start-local.ps1`: execução facilitada no Windows
- `scripts/start-local.sh`: execução facilitada no Linux/macOS
- `scripts/logs.ps1`: visualização de logs no Windows
- `scripts/logs.sh`: visualização de logs no Linux/macOS
- `.env.example`: modelo centralizado de variáveis de ambiente

## Documentação complementar

- O `README` da raiz descreve o projeto OffPay como um todo
- O `README` de [`signal-analytics-ai-service`](C:/Users/mateu/Desktop/GS-26/global-java-fresh/signal-analytics-ai-service/README.md) detalha a entrega da trilha de IA Generativa e o microsserviço OffPay Insights
