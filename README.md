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

Para executar o utilitário, você pode compilar os fontes ou simplesmente **baixar os executáveis da última Release**:

1. Vá em [Releases](https://github.com/buenofgustavo/sistema-runner/releases) e baixe:
   - O executável correspondente à sua plataforma (ex.: `assinatura-v0.0.8-windows-amd64.exe` para Windows).
   - O arquivo `assinador.jar`.
2. Para facilitar a digitação dos comandos, **renomeie o executável do CLI** para `assinatura` (ou `assinatura.exe` no Windows).
3. Mantenha o `assinador.jar` e o executável no mesmo diretório.

A CLI foi projetada para rodar de forma transparente. Se o Java 21+ não estiver configurado no PATH ou no `JAVA_HOME`, a CLI irá **baixar e provisionar automaticamente uma versão portátil do JRE 21 (Adoptium/Temurin)** na pasta `~/.assinatura/jre`.

### Preparando um arquivo para teste
Antes de rodar os comandos, crie um arquivo de texto de entrada chamado `dado.txt` usando o comando correspondente ao seu terminal:
* **No PowerShell (Windows)**:
  ```powershell
  Set-Content -Path dado.txt -Value "Texto de teste para assinar e validar."
  ```
* **No Prompt do Windows (cmd)**:
  ```cmd
  echo Texto de teste para assinar e validar. > dado.txt
  ```
* **No Linux / macOS**:
  ```bash
  echo "Texto de teste para assinar e validar." > dado.txt
  ```

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
* **Ligar o servidor**:
  ```bash
  ./assinatura server start
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

## 🌐 Testando a API REST (Modo Avançado FHIR)

Quando o `assinador.jar` está rodando em modo servidor (porta `8080`), ele expõe endpoints REST que validam rigidamente as estruturas de dados no formato FHIR. Você pode testar esse fluxo avançado usando clientes HTTP como cURL ou scripts do PowerShell.

### Teste Automatizado no PowerShell (Windows)
Se você estiver utilizando o PowerShell, pode rodar o bloco de script abaixo para executar o fluxo avançado completo de forma automatizada (ele gerará a assinatura, capturará o JWS retornado no campo `signature`, validará contra a política de assinatura e salvará o resultado no arquivo `retorno_validacao.json`):

```powershell
# 1. Preparar o payload para criação da assinatura avançada
$signPayload = @{
    bundleJson = "{}"
    provenanceJson = "{}"
    material = '{"type":"PEM","key":"CHAVE_PRIVADA_MOCK"}'
    certChain = @("dGVzdGU=")
    referenceTimestamp = 1751328005
    strategy = "iat"
    signaturePolicy = "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|1.0.0"
    operationalConfig = "{}"
} | ConvertTo-Json -Depth 5

Write-Host "1. Solicitando Assinatura Avançada..." -ForegroundColor Cyan
$signResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/sign" -Method Post -Body $signPayload -ContentType "application/json"
$jws = $signResponse.signature

Write-Host "Assinatura JWS gerada com sucesso!`n" -ForegroundColor Green

# 2. Preparar o payload para validação usando o JWS gerado
$validatePayload = @{
    jwsBase64 = $jws
    referenceTimestamp = 1751328005
    signaturePolicy = "https://fhir.saude.go.gov.br/r4/seguranca/ImplementationGuide/br.go.ses.seguranca|1.0.0"
    operationalConfig = "{}"
} | ConvertTo-Json -Depth 5

Write-Host "2. Solicitando Validação Avançada..." -ForegroundColor Cyan
$validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/validate" -Method Post -Body $validatePayload -ContentType "application/json"

# 3. Converter o objeto da resposta para JSON e salvar no arquivo
$validateResponse | ConvertTo-Json | Set-Content -Path "retorno_validacao.json"

Write-Host "3. Resultado da validação salvo com sucesso em 'retorno_validacao.json'!" -ForegroundColor Green
Write-Host "`nConteúdo do arquivo salvo:" -ForegroundColor Yellow
Get-Content -Path "retorno_validacao.json"
```

### Teste Manual via cURL
Caso queira testar de forma isolada e manual:

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

## ⚖️ Licença

Este projeto está licenciado sob a Licença MIT - consulte o arquivo [LICENSE](LICENSE) para obter detalhes.
