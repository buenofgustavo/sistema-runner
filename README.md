# Sistema Runner

[![CI & Release](https://github.com/buenofgustavo/sistema-runner/actions/workflows/ci.yml/badge.svg)](https://github.com/buenofgustavo/sistema-runner/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java: 21+](https://img.shields.io/badge/Java-21+-orange.svg)](https://adoptium.net/)
[![Go: 1.26+](https://img.shields.io/badge/Go-1.26+-blue.svg)](https://go.dev/)

O **Sistema Runner** é uma solução unificada e robusta projetada para simplificar a criação e validação de assinaturas digitais, ocultando a complexidade de configurações de ambiente (como Java Virtual Machine e portas de rede). Ele foi desenvolvido como trabalho prático da disciplina de Implementação e Integração (2026).

A especificação de referência utilizada é mantida sob um commit fixo no arquivo [especificacao_runner.md](especificacao_runner.md).

---

## 🏗️ Arquitetura Integrada

O projeto adota uma arquitetura integrada dividida em dois componentes principais:

1. **CLI Go (`assinatura`)**: Um executável leve escrito em Go que serve como ponto de contato com o usuário. Ele valida argumentos de entrada, detecta/provisiona a JVM e gerencia o ciclo de vida do servidor Java em segundo plano.
2. **Assinador Java (`assinador.jar`)**: O motor de execução em Java 21/Spring Boot que centraliza as validações e realiza a simulação de assinatura e verificação digital.

### Modos de Invocação
* **Modo Servidor (Warm Start - Padrão)**: O CLI Go inicia o `assinador.jar` como um servidor REST em background (porta padrão `8080`). Chamadas subsequentes são feitas via HTTP para `/api/sign` e `/api/validate`, eliminando o overhead de inicialização da JVM.
* **Modo Local (Cold Start)**: O CLI Go invoca diretamente o `assinador.jar` via linha de comando (`java -jar assinador.jar ...`) para execuções esporádicas.

---

## 🛠️ Requisitos de Sistema e Build

Se você quiser compilar os binários do projeto a partir do código fonte, precisará de:

* **Go 1.26+** (para o CLI)
* **JDK 21+** e **Maven 3.8+** (para o Assinador Java)

### Compilando o Assinador Java
```bash
cd assinador-java
mvn clean package
```
O JAR gerado estará em `assinador-java/target/assinador-java-1.0.0-SNAPSHOT.jar`. Renomeie-o ou copie-o como `assinador.jar` no local de execução da sua CLI.

### Compilando a CLI Go
```bash
cd assinatura
go build -o assinatura.exe main.go
```

---

## 🚀 Como Executar o Projeto

A CLI foi projetada para rodar de forma transparente. Se o Java 21+ não estiver configurado no PATH ou no `JAVA_HOME`, a CLI irá **baixar e provisionar automaticamente uma versão portátil do JRE 21 (Adoptium/Temurin)** na pasta `~/.assinatura/jre`.

### Comandos Básicos (Modo Servidor / Warm Start)
Por padrão, a CLI sobe o servidor HTTP em background automaticamente na primeira execução:

1. **Exibir a versão do utilitário**:
   ```bash
   ./assinatura version
   ```

2. **Criar uma Assinatura**:
   ```bash
   ./assinatura sign --input dado.txt --output dado.sig
   ```

3. **Validar uma Assinatura**:
   ```bash
   ./assinatura validate --input dado.txt --signature dado.sig
   ```

### Comandos de Controle do Servidor
Você pode gerenciar manualmente o servidor em background:

* **Verificar se o servidor está rodando (Health Check)**:
  ```bash
  ./assinatura server status
  ```
* **Encerrar o servidor ativo**:
  ```bash
  ./assinatura server stop
  ```

### Comando em Modo Local (Cold Start)
Para executar a assinatura ou validação de forma direta em uma nova instância da JVM, utilize a flag `--mode local`:
```bash
./assinatura sign --mode local --jar assinador.jar --input dado.txt --output dado.sig
./assinatura validate --mode local --jar assinador.jar --input dado.txt --signature dado.sig
```

### Flags Globais de Verbose e Quiet
* **Quiet (`--quiet` ou `-q`)**: Suprime mensagens informativas, exibindo apenas o status da operação:
  ```bash
  ./assinatura sign -q --input dado.txt --output dado.sig
  ```
* **Verbose (`--verbose` ou `-v`)**: Mostra logs detalhados e a saída crua do JAR Java para fins de depuração.

---

## 🧪 Como Executar os Testes

### Executar os testes automatizados da CLI Go
```bash
cd assinatura
go test -v ./...
```

### Executar os testes automatizados do Assinador Java (JUnit)
```bash
cd assinador-java
mvn test
```

---

## 🌐 Testando a API REST (Modo Avançado FHIR)

Quando o `assinador.jar` está rodando em modo servidor (porta `8080`), ele expõe endpoints REST que podem ser testados com clientes HTTP (como `cURL` ou PowerShell `Invoke-RestMethod`).

### 1. Criar Assinatura Avançada (POST `/api/sign`)
Envie um payload contendo a estrutura de transação FHIR:

```bash
curl -X POST http://localhost:8080/api/sign \
  -H "Content-Type: application/json" \
  -d '{
    "bundleJson": "{}",
    "provenanceJson": "{}",
    "material": "{\"type\":\"PEM\",\"key\":\"CHAVE_PRIVADA_PEM\"}",
    "certChain": ["dGVzdGU="],
    "referenceTimestamp": 1751328005,
    "strategy": "iat",
    "signaturePolicy": "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|1.0.0",
    "operationalConfig": "{}"
  }'
```

O servidor responderá com o JWS simulado codificado em Base64:
```json
{
  "signature": "eyJQQVlMT0FEIiA6ICJleUpRV...",
  "valid": true,
  "message": "Assinatura digital simulada criada com sucesso (JWS)"
}
```

### 2. Validar Assinatura Avançada (POST `/api/validate`)
Envie o token retornado anteriormente no campo `jwsBase64`:

```bash
curl -X POST http://localhost:8080/api/validate \
  -H "Content-Type: application/json" \
  -d '{
    "jwsBase64": "eyJQQVlMT0FEIiA6ICJleUpRV...",
    "referenceTimestamp": 1751328005,
    "signaturePolicy": "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|1.0.0",
    "operationalConfig": "{}"
  }'
```

Resposta de sucesso:
```json
{
  "signature": "eyJQQVlMT0FEIiA6ICJleUpRV...",
  "valid": true,
  "message": "Assinatura é válida (simulada)"
}
```

---

## ⚖️ Licença

Este projeto está licenciado sob a Licença MIT - consulte o arquivo [LICENSE](LICENSE) para obter detalhes.
