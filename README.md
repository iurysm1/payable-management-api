# Payable Management API

API para gestão de fornecedores, contas a pagar, importações assíncronas por CSV e relatórios de pagamentos.

## Requisitos técnicos

- Java 21.
- Docker e Docker Compose para PostgreSQL, RabbitMQ e Redocly CLI.
- `curl` e `jq` para exportar e comparar o contrato OpenAPI.
- Maven Wrapper incluído no repositório.

O projeto usa Spring Boot 3.5, Springdoc OpenAPI 2.8, OpenAPI 3.1, Swagger UI e Redocly CLI 2.40.
O Redocly é executado pela imagem Docker versionada; não é necessário instalar Node.js ou npm.

## Inicialização

Suba PostgreSQL e RabbitMQ:

```bash
docker compose up -d
```

Defina as credenciais HTTP Basic:

```bash
export APP_SECURITY_USERNAME=payable
export APP_SECURITY_PASSWORD=change-me
```

Inicie a aplicação:

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080/management/rest`.

Exemplo autenticado:

```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" \
  http://localhost:8080/management/rest/payable
```

## Documentação da API

Toda a documentação exige as mesmas credenciais HTTP Basic da API.

| Recurso | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/management/rest/swagger-ui.html` |
| Redoc | `http://localhost:8080/management/rest/redoc.html` |
| OpenAPI JSON | `http://localhost:8080/management/rest/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/management/rest/v3/api-docs.yaml` |

O código e as anotações dos controllers são a fonte do contrato. O arquivo
`docs/openapi.json` é um snapshot versionado e não deve ser editado manualmente.

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

## Atualização e validação do OpenAPI

Com a aplicação em execução e as credenciais exportadas:

```bash
# Atualizar docs/openapi.json a partir do código
./docs/openapi.sh export

# Falhar caso o snapshot esteja diferente do endpoint atual
./docs/openapi.sh check

# Validar redocly.yaml e executar as regras de governança
./docs/openapi.sh lint

# Gerar src/main/resources/static/redoc.html
./docs/openapi.sh build

# Exportar, validar e construir em sequência
./docs/openapi.sh all
```

Para consultar outra instância, defina `OPENAPI_URL` antes de `export`, `check` ou `all`.

As regras do Redocly exigem descrições, resumos, `operationId`, parâmetros documentados, tags,
segurança válida e pelo menos uma resposta `4xx` em cada operação.

## Testes

```bash
./mvnw test
```

A suíte inclui um teste de contrato que:

- compara estruturalmente o OpenAPI gerado com `docs/openapi.json`;
- exige as 16 operações, 4 tags e o esquema `basicAuth`;
- verifica respostas relevantes, como `409` e `415`;
- confirma que OpenAPI, Swagger UI e Redoc permanecem autenticados.

Após alterar um endpoint ou schema, atualize o snapshot e o Redoc e execute os testes e o lint.
