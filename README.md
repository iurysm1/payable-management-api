# Payable Management API

API para gestão de fornecedores, contas a pagar, importações assíncronas por CSV e relatórios de pagamentos.

O arquivo [`contas-teste-casos.zip`](contas-teste-casos.zip) contém CSVs de importação para testes.

## Requisitos técnicos

- Java 21.
- Docker e Docker Compose para executar a aplicação, o PostgreSQL, o RabbitMQ e o Redocly CLI.
- Maven Wrapper incluído no repositório.

O projeto usa Spring Boot 3.5, Springdoc OpenAPI 2.8, OpenAPI 3.1, Swagger UI e Redocly CLI 2.40.
O Redocly é executado pela imagem Docker versionada; não é necessário instalar Node.js ou npm.

## Inicialização

Defina as credenciais HTTP Basic:

```bash
export APP_SECURITY_USERNAME=payable
export APP_SECURITY_PASSWORD=change-me
```

Inicie todo o ambiente com Docker Compose. Ele sobe a aplicação e as dependências de infraestrutura
(PostgreSQL e RabbitMQ), já configuradas para se comunicarem entre si:

```bash
docker compose up --build
```

A API estará disponível em `http://localhost:8080/management/rest`.

Exemplo autenticado:

```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" \
  http://localhost:8080/management/rest/payable
```

## Documentação da API

A API possui documentação Swagger. Acesse `http://localhost:8080/management/rest/swagger-ui/index.html`.

## Catálogo funcional

### Fornecedores

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/supplier` | Listar fornecedores com paginação e ordenação. |
| `GET` | `/supplier/{id}` | Consultar um fornecedor. |
| `POST` | `/supplier` | Cadastrar um fornecedor. |
| `PUT` | `/supplier/{id}` | Atualizar um fornecedor usando o ID da rota. |
| `DELETE` | `/supplier/{id}` | Excluir fornecedor sem contas vinculadas. |

O nome é obrigatório. A exclusão retorna `409 Conflict` quando o fornecedor possui contas a pagar.

### Contas a pagar

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/payable` | Listar e filtrar contas com paginação. |
| `GET` | `/payable/{id}` | Consultar uma conta. |
| `POST` | `/payable` | Criar uma conta. |
| `PUT` | `/payable/{id}` | Atualizar uma conta usando o ID da rota. |
| `DELETE` | `/payable/{id}` | Excluir uma conta. |
| `PATCH` | `/payable/{id}/status` | Alterar somente status e data de pagamento. |

Filtros disponíveis em `GET /payable`:

- `description`: trecho da descrição, sem distinção entre maiúsculas e minúsculas.
- `startDate` e `endDate`: período de vencimento; devem ser informados juntos.
- `status`: `PENDENTE`, `PAGO` ou `CANCELADO`, sem distinção entre maiúsculas e minúsculas.
- `page`, `size` e `sort`: paginação padrão do Spring Data.

Regras principais:

- Descrição, valor e fornecedor são obrigatórios.
- O valor não pode ser negativo.
- O status padrão é `PENDENTE`.
- Uma conta `PAGO` precisa de `paymentDate`; os outros status mantêm essa data nula.
- No PATCH, os códigos são `0=PENDENTE`, `1=PAGO` e `2=CANCELADO`.
- Ao mudar para `PAGO` sem data, o sistema utiliza a data atual; se já estava paga, preserva a data existente.

### Importação assíncrona

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/import/payable` | Listar importações. |
| `GET` | `/import/payable/{id}` | Acompanhar uma importação. |
| `GET` | `/import/payable/{id}/items` | Consultar o resultado das linhas. |
| `POST` | `/import/payable` | Enviar um CSV em `multipart/form-data`. |

O `POST` salva o CSV, cria uma importação `PENDING`, publica um evento no RabbitMQ e responde `201 Created`.
Essa resposta confirma a aceitação, não a conclusão do processamento.

Fluxo de status:

```text
PENDING -> PROCESSING -> COMPLETED
                      -> COMPLETED_WITH_ERRORS
                      -> FAILED
```

Itens processados com sucesso recebem `SUCCESS` e o `payableId` criado. Linhas rejeitadas recebem
`ERROR` e uma mensagem, sem interromper as demais. Falhas globais de arquivo ou cabeçalho levam a
importação para `FAILED`.

`COMPLETED_WITH_ERRORS` indica que o arquivo foi processado até o fim, mas uma ou mais linhas foram
rejeitadas. As linhas válidas continuam sendo importadas e recebem `SUCCESS`; as inválidas recebem
`ERROR`, com o motivo da rejeição, e não geram uma conta a pagar. Consulte
`GET /import/payable/{id}/items` para identificar e corrigir somente as linhas com erro antes de
enviá-las novamente. Esse status é diferente de `FAILED`, que representa uma falha global e impede
o processamento completo do arquivo.

O CSV usa UTF-8, vírgula como separador, datas `yyyy-MM-dd` e este cabeçalho exato:

```csv
description,amount,status,expirationDate,paymentDate,supplierId
Pagamento de aluguel,1500.00,PAGO,2026-08-10,2026-08-05,1
Conta de internet,89.90,PENDENTE,2026-08-15,,1
```

São aceitos campos com aspas duplas e aspas escapadas. Linhas em branco são ignoradas.

### Relatório de pagamentos

| Método | Rota | Função |
| --- | --- | --- |
| `GET` | `/report/total-paid` | Consolidar contas pagas em um período. |

`startDate` e `endDate` são obrigatórias e inclusivas. `supplierId` é opcional. O relatório retorna
total pago, quantidade e itens considerados; quando não há contas pagas, retorna `404 Not Found`.

## Testes

```bash
./mvnw test
```

A suíte inclui um teste de contrato que:

- compara estruturalmente o OpenAPI gerado com `docs/openapi.json`;
- exige as 16 operações, 4 tags e o esquema `basicAuth`;
- verifica respostas relevantes, como `409` e `415`;
- confirma que OpenAPI, Swagger UI e Redoc permanecem autenticados.
