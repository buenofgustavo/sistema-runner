# Simulador CLI

Ferramenta CLI desenvolvida em Go para gerenciar o ciclo de vida do servidor simulador do HubSaúde (iniciar, parar e verificar estado).

## Status atual

- CLI construída com Cobra
- Comandos disponíveis: `start`, `status`, `stop`, `version`
- Invocação e execução em segundo plano de Java/Tomcat com redirecionamento de logs
- Descoberta inteligente de Java 21+ com fallback de provisionamento automático
- Download automático do executável do simulador (`hubsaude-validador-api.jar`) com validação de integridade SHA-256
- Suporte a logs detalhados via flag global `--verbose` formatados em `key=value` no `stderr`
- Testes automatizados para a CLI

## Pré-requisitos

- Go instalado (para desenvolvimento/compilação)
- Java 21+ instalado no PATH ou configurado na variável `JAVA_HOME`. *(Caso não esteja presente, o CLI fará o provisionamento automático do JRE 21 em ~/.hubsaude/jdk).*

## Estrutura de pacotes

```text
.
├── cmd/
│   ├── simulador/
│   │   └── main.go       # Ponto de entrada do executável
│   ├── checksum.go       # Download do JAR e validação de integridade SHA-256
│   ├── constants.go      # Constantes e URLs de referência do simulador
│   ├── java.go           # Descoberta do Java 21+ no sistema
│   ├── jdk.go            # Provisionamento automático do JRE 21
│   ├── process_unix.go   # Verificação de processo ativo para Linux/macOS
│   ├── process_windows.go# Verificação de processo ativo para Windows
│   ├── root.go           # Inicialização e parametrização do Cobra e do slog
│   ├── start.go          # Implementação do comando 'start'
│   ├── status.go         # Implementação do comando 'status'
│   ├── stop.go           # Implementação do comando 'stop'
│   └── version.go        # Implementação do comando 'version'
├── go.mod
├── go.sum
├── especificacao.md      # Especificações originais
└── README.md             # Instruções de execução e documentação do projeto
```

---

## Como Instalar (Downloads)

Para utilizar o utilitário, você não precisa compilar os arquivos fonte localmente. Basta acessar a página de [**Releases**](../../releases) do repositório e baixar o binário correspondente à sua plataforma de execução:

| Sistema | Arquivo para baixar |
|---|---|
| Windows | `cli-simulador-*-windows-amd64.exe` |
| Linux | `cli-simulador-*-linux-amd64` |
| macOS | `cli-simulador-*-darwin-amd64` |

Após o download, renomeie o executável para `simulador` (ou `simulador.exe` no Windows).

---

## Uso da CLI

Abra o terminal na pasta onde salvou o executável (renomeado para `simulador` ou `simulador.exe`).

### Versão
Exibe a versão e o commit do build do utilitário:
```bash
# No Windows
.\simulador.exe version

# No Linux / macOS
./simulador version
```

### Iniciar o simulador (`start`)
Inicia o simulador em segundo plano. O utilitário irá provisionar os requisitos automaticamente e aguardará até que o servidor esteja completamente pronto e respondendo antes de retornar.
```bash
# No Windows
.\simulador.exe start

# No Linux / macOS
./simulador start
```

**Com porta personalizada:**
```bash
# No Windows
.\simulador.exe start --port 9090

# No Linux / macOS
./simulador start --port 9090
```

### Verificar o estado do simulador (`status`)
Mostra se o simulador está ativo e respondendo aos testes de saúde.
```bash
# No Windows
.\simulador.exe status

# No Linux / macOS
./simulador status
```

**Com porta personalizada:**
```bash
# No Windows
.\simulador.exe status --port 9090

# No Linux / macOS
./simulador status --port 9090
```

**Exemplo de saída (Ativo):**
```text
🟢 ESTADO: ATIVO (PRONTO)
   PID    : 12345
   Porta  : 8080
   Health : OK (http://localhost:8080/health)
```

### Parar o simulador (`stop`)
Encerra de forma limpa o processo do simulador ativo em background.
```bash
# No Windows
.\simulador.exe stop

# No Linux / macOS
./simulador stop
```

---

## Opções Globais

### Verbose (`--verbose`)
Exibe logs estruturados de diagnóstico em nível `DEBUG` direto no `stderr` no formato `key=value`. Útil para depurar problemas de download, caminhos de execução ou falhas de rede.
```bash
# No Windows
.\simulador.exe start --verbose

# No Linux / macOS
./simulador start --verbose
```

---

## Build e Compilação Multiplataforma

Para gerar binários otimizados para as três plataformas suportadas:

```bash
# Windows
GOOS=windows GOARCH=amd64 go build -o dist/cli-simulador-windows-amd64.exe ./cmd/simulador/main.go

# Linux
GOOS=linux GOARCH=amd64 go build -o dist/cli-simulador-linux-amd64 ./cmd/simulador/main.go

# macOS (Darwin)
GOOS=darwin GOARCH=amd64 go build -o dist/cli-simulador-darwin-amd64 ./cmd/simulador/main.go
```

## Executar Testes Unitários

```bash
go test ./... -v
```

---

## Integridade e Segurança (Releases)

Todas as novas versões geradas pelo pipeline do GitHub Actions contam com:
1. **SHA256SUMS**: Checksums SHA-256 anexados de forma transparente.
2. **Cosign Signatures**: Assinaturas criptográficas via Sigstore / Cosign (*keyless*) para atestar a autenticidade e proveniência do binário.

Para verificar a integridade localmente após o download:
```bash
# Verificar checksum
sha256sum -c cli-simulador-v1.2.0-windows-amd64.exe.sha256

# Verificar assinatura OIDC com Cosign
cosign verify-blob \
  --certificate cli-simulador-v1.2.0-windows-amd64.pem \
  --signature cli-simulador-v1.2.0-windows-amd64.sig \
  --certificate-identity-regexp "https://github.com/.*" \
  --certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
  cli-simulador-v1.2.0-windows-amd64.exe
```
