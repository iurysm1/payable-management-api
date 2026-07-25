---
name: payable-hexagonal-ddd
description: Aplicar o padrão hexagonal com DDD deste projeto de gestão de contas a pagar ao criar ou evoluir qualquer feature, caso de uso, endpoint REST, regra de negócio, integração, persistência, migração ou testes. Usar para manter as fronteiras entre domain, core e adapters, definir ports e manter mocks de teste claros antes de concluir.
---

# Arquitetura Hexagonal com DDD

Implementar features respeitando os limites abaixo. Trabalhar a partir do vocabulário do domínio e dos casos de uso; não começar pelo controller ou pelo repositório.

## Estrutura do projeto

| Camada | Local | Responsabilidade |
| --- | --- | --- |
| Domínio | `domain` | Entidades, enums, invariantes, comportamento e exceções de domínio. |
| Aplicação | `core/port/input`, `core/port/output`, `core/service` | Casos de uso, contratos de entrada e saída, orquestração e erros de aplicação. |
| Adaptador de entrada | `adapter/input` | REST, desserialização, validação de borda e mapeamento HTTP. |
| Adaptador de saída | `adapter/output` | JPA, repositórios Spring Data, conversores e integrações externas. |

Seguir o fluxo de dependências: `adapter -> core -> domain`. O `core` conhece o domínio e seus ports; adaptadores implementam os ports. Não permitir que controller ou serviço de aplicação dependa diretamente de `JpaRepository`.

## Regras de design

- Modelar regras e transições de negócio na entidade ou no agregado do domínio, com métodos expressivos como `create` e `updateDetails`; não colocá-las em controllers, repositories ou converters.
- Expor cada capacidade da aplicação por um input port e implementá-la em um service. O service coordena entidades e output ports, sem conter detalhes HTTP ou JPA.
- Criar ou ampliar output ports para toda dependência externa. Implementá-los em adapters concretos, como os adapters JPA existentes.
- Manter controllers pequenos: receber a requisição, aplicar `@Valid`, assegurar que o identificador da rota prevaleça em atualizações, chamar somente o input port e devolver a resposta HTTP.
- Criar exceções de domínio para violações de invariantes e exceções de aplicação para ausência, conflito ou política de caso de uso. Mapear novas exceções HTTP em `ApiExceptionHandler` com `ProblemDetail` quando expostas pela API.
- Manter DTOs dos casos de uso em `core.port.input.dto`, usando records e validação de entrada quando aplicável.
- Preservar a convenção atual em que entidades do domínio têm anotações JPA. Não separar modelos de persistência nem refatorar entidades existentes para um domínio puro sem solicitação explícita.

## Testes e mocks

- Testar services com output ports mockados, domínio com testes de invariantes e controllers alterados com `WebMvcTest`.
- Ao revisar uma classe de teste, centralizar em `@BeforeEach` somente stubs `when(...)` repetidos com a mesma dependência, método e argumentos; criar uma fixture padrão nomeada para o retorno compartilhado.
- Manter no teste os stubs cujo retorno, exceção ou argumentos definem o cenário. Não usar matchers amplos, como `anyLong()`, apenas para eliminar repetição quando eles puderem mascarar IDs inválidos ou fluxos de ausência.
- Criar fixtures no `@BeforeEach` para que cada teste receba um objeto novo e possa alterá-lo sem interferir nos demais.
- Não adicionar setup comum quando ele atender apenas um cenário ou tornar o comportamento do teste menos explícito.

## Fluxo obrigatório para uma feature

1. Identificar a linguagem do domínio, a regra de negócio e o agregado responsável. Definir invariantes e transições antes de desenhar a API.
2. Criar ou alterar o input port, DTO e service necessários. Declarar output ports para persistência, integração ou consulta de que o caso de uso depende.
3. Implementar os adapters de entrada e saída. Para REST, seguir os padrões de paginação, status HTTP e `ProblemDetail` já existentes.
4. Para mudança de schema, adicionar uma migration Flyway incremental em `src/main/resources/db/migration`; manter `ddl-auto: validate` e não depender de geração automática do schema.
5. Criar ou ajustar os testes na camada afetada e consolidar apenas mocks efetivamente repetidos na mesma classe.
6. Manter a alteração mínima e compatível com os contratos existentes. Declarar qualquer desvio arquitetural antes de concluir o trabalho.

## Checklist de conclusão

Antes de concluir uma feature, confirmar:

- [ ] As regras e invariantes pertencem ao domínio.
- [ ] O caso de uso é exposto por input port e coordenado por service.
- [ ] Persistência e integrações são acessadas apenas por output ports e adapters.
- [ ] O controller não contém regra de negócio nem acessa repositório.
- [ ] O schema foi alterado por migration Flyway, quando necessário.
- [ ] Exceções expostas pela API recebem o status HTTP apropriado.
- [ ] Há testes de domínio para invariantes, de service com ports mockados e de controller com `WebMvcTest` para comportamento HTTP modificado.
- [ ] Mocks repetidos foram revisados por classe; apenas chamadas equivalentes foram movidas para `@BeforeEach`, sem esconder cenários de erro.
- [ ] `./mvnw test` foi executado com sucesso, ou a falha foi reportada com a causa.

Para alterações exclusivamente documentais ou mecânicas, aplicar somente os itens relevantes da checklist.
