# Deploy e Entrega no Azure

Este documento organiza, passo a passo, como levar o projeto atual para o Azure e como atender os requisitos da entrega de DevOps Tools & Cloud Computing com o menor risco possível de penalizacao.

## Objetivo

A ideia e usar o projeto atual como base, mantendo por enquanto o banco da FIAP para desenvolvimento e testes iniciais, mas preparando a aplicacao para a entrega final em nuvem no Azure com:

- Azure Repos
- Azure Boards
- Azure Pipelines
- Deploy automatico
- Banco em nuvem
- Scripts versionados no repositorio
- Evidencias claras para o video e para a correcao

## Resumo do estado atual do projeto

O repositorio ja possui uma base boa para a entrega:

- microsservicos Java Spring Boot
- Dockerfiles em `/dockerfiles`
- `docker-compose.yml` na raiz
- uso de variaveis de ambiente com `.env` e `.env.example`
- controllers com CRUD em endpoints REST
- migrations Flyway dentro dos servicos

Porem, ainda faltam alguns itens importantes para aderir ao enunciado:

- `azure-pipeline.yml` na raiz
- `script-bd.sql` em `/scripts`
- scripts Azure CLI com prefixo `script-infra`
- documentacao explicita dos CRUDs em JSON no README
- desenho macro da arquitetura
- recursos finais em nuvem no Azure, inclusive banco

## Estrategia recomendada

Para evitar retrabalho, siga em duas fases:

1. Primeiro organizar Azure DevOps, repositorio, boards, branch policies e pipeline.
2. Depois finalizar a infraestrutura em nuvem, trocar o banco da FIAP para Azure e gravar o video.

Essa ordem ajuda porque voce consegue validar o fluxo de DevOps antes de fechar a infraestrutura final.

## Fase 1 - Organizar o projeto no Azure DevOps

### 1. Criar ou importar o repositorio no Azure Repos

Requisito relacionado:

- 2.3 Codigo no Azure Repos
- 3.1 Projeto privado e com Git para versionamento

Passos:

1. Crie uma organizacao no Azure DevOps, se ainda nao existir.
2. Crie um projeto privado.
3. Importe este repositorio para o Azure Repos.
4. Confirme que a estrutura esta preservada:
   - `README.md`
   - `docker-compose.yml`
   - `/dockerfiles`
   - `/scripts`
   - os quatro microsservicos

Observacao:

- O professor pode zerar o trabalho se o codigo nao estiver no Azure DevOps. Entao esse e o primeiro passo obrigatorio.

### 2. Criar a tarefa inicial no Azure Boards

Requisito relacionado:

- 2.4 Azure Boards
- 3.2 Azure Boards vinculado ao Repos

Passos:

1. Entre em `Boards > Work Items`.
2. Crie uma task inicial com um nome claro, por exemplo:
   `Subir OffPay no Azure com CI/CD e deploy automatizado`
3. Guarde o numero da task porque ele sera usado em branch, commits e PRs.

Boa pratica:

- Use o ID da task no nome da branch e nas mensagens de commit.

### 3. Criar a branch de trabalho

Requisito relacionado:

- 3.2 Azure Boards vinculado ao Repos
- 3.18 Nao criou uma nova Branch para a tarefa

Passos:

1. Crie uma branch a partir da `main`.
2. Use um nome padrao, por exemplo:
   `feature/123-offpay-azure`

Boa pratica:

- O numero `123` deve ser o ID real da task no Boards.

### 4. Proteger a branch principal

Requisito relacionado:

- 3.3 Branch principal protegida
- 3.17 Branch principal desprotegida

Passos:

1. Acesse `Repos > Branches`.
2. Na branch `main`, abra `Branch policies`.
3. Configure:
   - `Minimum number of reviewers = 1`
   - `Check for linked work items = required`
   - `Automatically include reviewers = seu RM`
   - `Allow requestors to approve their own changes = enabled`

Por que isso importa:

- O enunciado pede revisor obrigatorio.
- O enunciado pede vinculacao com Work Item.
- O enunciado pede revisor padrao.
- O enunciado aceita que o aluno aprove a propria PR como simulacao.

### 5. Definir a regra de integracao com a main

Requisito relacionado:

- 2.5 Pipeline de Build
- 3.4 Build deve ser acionado somente apos Merge na Main via PR

Como interpretar corretamente:

- Um trecho do enunciado fala que a CI deve rodar automaticamente a cada commit na branch principal.
- Outro trecho fala que o build deve ser acionado somente apos merge na main via PR.

Forma mais segura de atender os dois:

- Bloqueie push direto na `main`.
- Permita entrada na `main` somente via Pull Request.
- Configure a pipeline para disparar na `main`.

Resultado:

- Todo commit que entrar na `main` tera vindo de um merge via PR.
- Portanto o build automatico sera, na pratica, sempre pos-merge.

## Fase 2 - Fechar os arquivos obrigatorios do repositorio

### 6. Garantir os Dockerfiles

Requisito relacionado:

- 3.12 Dockerfiles na pasta `/dockerfiles`

Situacao atual:

- Ja atendido.

Arquivos encontrados:

- `dockerfiles/signal-auth-service.Dockerfile`
- `dockerfiles/signal-sales-service.Dockerfile`
- `dockerfiles/signal-payment-service.Dockerfile`
- `dockerfiles/signal-analytics-ai-service.Dockerfile`

### 7. Criar o arquivo `script-bd.sql`

Requisito relacionado:

- 3.10 Arquivo `script-bd.sql` na pasta `/scripts`

Situacao atual:

- Ainda nao existe.

O que fazer:

1. Criar `scripts/script-bd.sql`.
2. Consolidar nele o DDL das tabelas mais importantes da demonstracao.
3. Priorizar pelo menos as tabelas usadas no CRUD do video.

Recomendacao:

- Use `product_category` e `product` como foco.
- Se quiser reforcar a entrega, inclua tambem tabelas de carteira ou pedidos.

Observacao importante:

- Nao confie apenas nas migrations Flyway dentro dos microsservicos. O enunciado pede explicitamente um arquivo SQL na pasta `/scripts`.

### 8. Criar scripts Azure CLI com prefixo `script-infra`

Requisito relacionado:

- 3.9 Scripts de infraestrutura no repositorio
- 3.11 Scripts Azure CLI com prefixo `script-infra` na pasta `/scripts`

Situacao atual:

- Ainda nao existem.

O que fazer:

1. Criar scripts com nomes como:
   - `scripts/script-infra-acr.sh`
   - `scripts/script-infra-apps.sh`
   - `scripts/script-infra-rabbitmq.sh`
   - `scripts/script-infra-db.sh`
2. Todos devem usar Azure CLI.
3. Eles devem criar os recursos mostrados no video.

Boa pratica:

- Se quiser, pode haver tambem versao `.ps1`, mas mantenha os arquivos com prefixo `script-infra`.

### 9. Criar o `azure-pipeline.yml` na raiz

Requisito relacionado:

- 2.5 Pipeline de Build
- 3.13 Arquivo `azure-pipeline.yml` na raiz

Situacao atual:

- Ainda nao existe.

O que fazer:

1. Criar `azure-pipeline.yml` na raiz.
2. Configurar pipeline para:
   - disparar na `main`
   - usar Java 21
   - rodar testes
   - publicar resultados JUnit
   - publicar artefatos
   - opcionalmente buildar e enviar imagens para o ACR

Observacao:

- A ausencia desse arquivo pode gerar penalizacao grave se a implementacao escolhida for YAML.

### 10. Completar o README com CRUD em JSON

Requisito relacionado:

- 3.14 CRUD exposto em JSON no README do projeto
- penalidade por ausencia dos JSONs de CRUD para APIs sem Swagger

Situacao atual:

- O README esta bom como visao geral, mas precisa ficar mais aderente ao enunciado.

O que adicionar:

- exemplos de `POST`, `GET`, `PUT` e `DELETE`
- payload JSON de categoria
- payload JSON de produto
- sequencia recomendada para executar os testes

Mesmo havendo Swagger:

- Vale a pena documentar no README para evitar discussao na correcao.

### 11. Reforcar o uso de variaveis de ambiente

Requisito relacionado:

- 3.15 Utilizar variaveis de ambiente e proteger dados sensiveis

Situacao atual:

- Ja existe base com `.env` e `.env.example`.

O que manter:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GROQ_API_KEY`

No Azure DevOps e Azure:

- nunca deixar essas informacoes fixas no codigo
- usar variaveis secretas
- usar Application Settings nos Web Apps

## Fase 3 - Arquitetura recomendada no Azure

### 12. Escolher a estrategia de deploy

Requisito relacionado:

- 3.7 Deploy pode ser feito por Container ou Web App
- 3.8 Banco em Container ou Servico PaaS

Melhor opcao para este projeto:

- `Azure Web App for Containers` para os microsservicos

Motivo:

- o projeto ja esta pronto para Docker
- os Dockerfiles ja existem
- a demonstracao costuma ficar mais simples

Arquitetura sugerida:

- `Azure Container Registry (ACR)` para armazenar imagens
- `4 Web Apps` para os microsservicos
- `1 recurso para RabbitMQ` em container
- `1 banco na Azure`

### 13. Sobre o banco da FIAP

Ponto critico:

- Usar o banco da FIAP pode servir temporariamente para desenvolvimento.
- Porem, isso nao atende o requisito final de banco em nuvem criado para a entrega.

Conclusao:

- voce pode seguir com o banco da FIAP agora para ganhar velocidade
- mas antes do video final e da entrega, precisa migrar para uma opcao no Azure

Opcoes:

- `Oracle em container na Azure`
- `Azure SQL`

Recomendacao para este projeto:

- como a aplicacao ja esta preparada para Oracle, a rota menos traumática tende a ser Oracle em container na Azure

## Fase 4 - Pipeline de Build e Release

### 14. Montar a Pipeline de Build

Requisito relacionado:

- 2.5 Pipeline de Build
- 3.4 Build somente apos merge na main via PR

O que a pipeline deve fazer:

1. Baixar o codigo da `main`.
2. Preparar Java 21.
3. Executar build dos quatro microsservicos.
4. Rodar testes.
5. Publicar resultados JUnit.
6. Publicar artefatos.
7. Se a estrategia for container, buildar e publicar imagens no ACR.

Artefatos recomendados:

- `.jar`
- relatorios de teste
- opcionalmente um pacote com scripts e manifests

### 15. Montar a Pipeline de Release

Requisito relacionado:

- 2.6 Pipeline de Release
- 3.6 Release executa automaticamente apos novo artefato

Objetivo:

- sempre que a CI gerar um novo artefato valido, o deploy deve iniciar automaticamente

Voce pode implementar de dois jeitos:

- `Build + Release` separados
- `YAML multi-stage`

Melhor escolha para aderencia visual ao enunciado:

- separar `Build` e `Release`, porque fica mais facil mostrar no video

Fluxo esperado:

1. Merge da PR na `main`
2. CI dispara automaticamente
3. Artefato e testes sao publicados
4. Release dispara automaticamente
5. Aplicacao e atualizada na Azure

## Fase 5 - CRUD para a demonstracao

### 16. Escolher duas tabelas para CRUD completo

Requisito relacionado:

- demonstrar CRUD em pelo menos duas tabelas
- provar persistencia diretamente no banco com `SELECT`

Melhor escolha no projeto atual:

- `product_category`
- `product`

Motivo:

- ja existem controllers claros para essas entidades
- o fluxo e simples de explicar
- reduz risco na gravacao

Arquivos-chave:

- `signal-sales-service/src/main/java/br/com/signal/signal_sales_service/catalog/controller/ProductCategoryController.java`
- `signal-sales-service/src/main/java/br/com/signal/signal_sales_service/catalog/controller/ProductController.java`

### 17. Roteiro de CRUD para o video

Para cada uma das duas tabelas, demonstre:

1. `Create`
2. `Read`
3. `Update`
4. `Delete`
5. `SELECT` no banco para provar persistencia

Atencao:

- o enunciado pede prova no banco
- nao use apenas `GET` para comprovar persistencia
- execute `SELECT` diretamente no banco em nuvem

## Fase 6 - Desenho da arquitetura

### 18. Criar o desenho macro

Requisito relacionado:

- 3.16 Incluir desenho macro da arquitetura

O desenho deve mostrar:

- usuario
- Azure DevOps
- Azure Repos
- Azure Boards
- Azure Pipelines
- Azure Container Registry
- microsservicos
- RabbitMQ
- banco
- fluxo entre auth, sales, payment e analytics

Onde colocar:

- no README
- no video
- opcionalmente no PDF final

## Fase 7 - Sequencia exata para gravar o video

### 19. Ordem recomendada da apresentacao

Requisito relacionado:

- secao inteira de gravacao do video

Siga nesta ordem:

1. Mostrar o README e explicar a proposta.
2. Mostrar o desenho da arquitetura.
3. Mostrar no Portal Azure os recursos criados pelos scripts.
4. Mostrar o Azure DevOps e criar uma nova task no Boards.
5. Criar uma nova branch vinculada a task.
6. Fazer uma alteracao real em codigo fonte.
7. Fazer commit vinculado ao Work Item.
8. Abrir Pull Request.
9. Aprovar a propria PR, se estiver simulando individualmente.
10. Fazer merge na `main`.
11. Mostrar a CI disparando automaticamente.
12. Mostrar artefatos e testes publicados.
13. Mostrar a Release disparando automaticamente.
14. Mostrar a aplicacao atualizada em nuvem.
15. Executar CRUD de duas tabelas.
16. Abrir o banco e executar `SELECT` para provar persistencia.
17. Voltar no Boards e mostrar a task com links de commit, branch e PR.

### 20. Cuidados para nao perder ponto no video

- Grave em 720p ou superior.
- Use audio claro.
- Fale narrando o processo.
- Evite cortes bruscos.
- Nao simule mudanca apenas no README.
- Nao deixe aplicacao ou banco em localhost.

## Fase 8 - Checklist final de conformidade

### 21. Itens que ja estao encaminhados

- projeto com Git
- estrutura de microsservicos
- Dockerfiles em `/dockerfiles`
- variaveis de ambiente com `.env` e `.env.example`
- endpoints REST para CRUD

### 22. Itens que ainda precisam ser feitos

- criar `azure-pipeline.yml`
- criar `scripts/script-bd.sql`
- criar scripts `script-infra*`
- completar README com JSON de CRUD
- criar desenho de arquitetura
- configurar Boards, Repos e policies
- subir recursos na Azure
- migrar banco final para Azure
- configurar CI
- configurar Release automatica

## Ordem pratica de execucao

Se voce quiser fazer sem se perder, siga exatamente esta ordem:

1. Importar o repositorio para Azure Repos.
2. Criar projeto privado no Azure DevOps.
3. Criar task inicial no Boards.
4. Criar branch nova para o trabalho.
5. Configurar protecao da `main`.
6. Criar `azure-pipeline.yml`.
7. Criar `scripts/script-bd.sql`.
8. Criar scripts `script-infra`.
9. Atualizar README com arquitetura e CRUD em JSON.
10. Criar recursos Azure por script.
11. Configurar CI.
12. Configurar Release automatica.
13. Fazer deploy dos servicos.
14. Migrar o banco para Azure.
15. Validar CRUD de duas tabelas na nuvem.
16. Ensaiar a gravacao.
17. Gravar o video completo.
18. Montar o PDF com os links finais.

## Observacoes finais

### Sobre o uso temporario do banco da FIAP

Pode usar agora para desenvolvimento, mas nao feche a entrega assim. Para a nota, o banco precisa estar em nuvem dentro da estrategia aceita pelo professor.

### Sobre a melhor demonstracao de CRUD

A escolha mais segura hoje e usar `categoria` e `produto`, porque o projeto ja tem o CRUD mais claro nessas entidades.

### Sobre a melhor interpretacao da pipeline

A configuracao mais segura e:

- branch `main` protegida
- merge apenas via PR
- pipeline disparando na `main`

Assim voce atende a CI automatica e tambem a regra de build somente apos merge.

### Sobre o que mais derruba nota

Os maiores riscos hoje sao:

- faltar `azure-pipeline.yml`
- faltar `script-bd.sql`
- faltar scripts `script-infra`
- manter banco final fora da Azure
- nao mostrar CRUD completo em duas tabelas
- nao mostrar persistencia com `SELECT`
- nao proteger a branch principal

## Proximo passo sugerido

Depois deste documento, o proximo passo ideal e implementar os arquivos obrigatorios que faltam no repositorio, nesta ordem:

1. `azure-pipeline.yml`
2. `scripts/script-bd.sql`
3. scripts `script-infra*`
4. atualizacao do `README.md`

Esse conjunto ja deixa o projeto muito mais aderente ao enunciado antes mesmo da subida final para a Azure.
