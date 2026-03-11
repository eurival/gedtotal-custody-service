# gedtotal-custody-service

## Responsabilidade

Microserviço responsável por:

- consumir `custodia.anchor.request`
- buscar payload e binário do documento ou artefato assinado no `gedtotalapi`
- calcular hash final do documento
- calcular Merkle root
- executar ancoragem blockchain quando configurada
- publicar `custodia.anchor.result` ou `custodia.anchor.failure`

## Papel na arquitetura

Este serviço:

- não acessa o banco do `gedtotalapi`
- não é fonte da verdade do estado da esteira
- não decide storage do documento
- processa apenas integridade, Merkle e ancoragem

## Fluxo

1. consome comando Kafka em `custodia.anchor.request`
2. chama `GET /api/internal/custodia/documentos/{arquivoId}/payload`
3. chama `GET /api/internal/custodia/documentos/{arquivoId}/content`
4. calcula hash e estrutura de Merkle
5. ancora em blockchain quando habilitado
6. publica resultado em `custodia.anchor.result`
7. em caso de erro, publica `custodia.anchor.failure`

## Configuração mínima

Principais propriedades em `src/main/resources/application.yaml`:

- `spring.kafka.bootstrap-servers`
- `app.internal-api.gedtotalapi-base-url`
- `app.internal-api.internal-token`
- `app.kafka.topics.custody-request`
- `app.kafka.topics.custody-result`
- `app.kafka.topics.custody-failure`
- `app.blockchain.*`

### Variáveis úteis para teste local

```bash
export KAFKA_BOOTSTRAP_SERVERS=15.229.173.87:19092
export GEDTOTALAPI_INTERNAL_TOKEN='SEU_TOKEN_INTERNO_AQUI'
```

Observação:

- `GEDTOTALAPI_INTERNAL_TOKEN` deve ser o mesmo segredo configurado no `gedtotalapi`
- essa autenticação é restrita aos endpoints internos de custódia

## Portas

- HTTP: `8092`

## Tópicos Kafka

Consome:

- `custodia.anchor.request`

Publica:

- `custodia.anchor.result`
- `custodia.anchor.failure`

## Execução local

```bash
./mvnw spring-boot:run
```

## Teste local mínimo

Pré-requisitos:

- Kafka acessível
- `gedtotalapi` rodando
- `gedtotal-signature-service` já tendo produzido o artefato assinado ou hash final disponível

Configuração recomendada para primeiro teste:

- `app.blockchain.enabled=false`

Com isso, o serviço executa o fluxo técnico de hash/Merkle sem depender de RPC ou contrato real.

## Blockchain

Para habilitar ancoragem real:

- `app.blockchain.enabled=true`
- configurar:
  - `network`
  - `rpc-url`
  - `chain-id`
  - `private-key`
  - `anchor-address`
  - `method-name`
  - `gas-price-wei`
  - `gas-limit`

## Observações operacionais

- o serviço deve consumir o artefato final, não o documento original, quando o `gedtotalapi` já tiver `artifactArquivoId`
- blockchain aqui é evidência complementar, não substitui assinatura nem carimbo do tempo
- o resultado só é oficial quando o `gedtotalapi` consome `custodia.anchor.result`
