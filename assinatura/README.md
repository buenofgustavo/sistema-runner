# Assinatura CLI

Ferramenta CLI desenvolvida em Go para realizar assinatura digital simulada e validação de artefatos.

## Status atual (US-01.1 a US-01.4)

- Projeto inicializado com `go.mod` usando o módulo `github.com/kyriosdata/assinatura`
- CLI construída com Cobra
- Comandos disponíveis: `version`, `sign`, `validate`
- Parsing de parâmetros com validações de obrigatoriedade
- Invocação local de `java -jar assinador.jar`
- Saída legível para sucesso, validação válida/inválida e erros
- Testes para parsing e fluxo CLI -> invocador Java

## Pré-requisitos

- Go instalado
- Java (JRE/JDK) no `PATH` ou `JAVA_HOME` configurado
- Ferramenta Cobra CLI (gerador):

```bash
go install github.com/spf13/cobra-cli@latest
```

## Estrutura de pacotes

```text
.
├── cmd/
│   ├── java.go        # Invocação de java -jar e descoberta do Java
│   ├── root.go        # Comando raiz e wiring dos subcomandos
│   ├── sign.go        # Comando de assinatura
│   ├── validate.go    # Comando de validação
│   ├── version.go     # Comando de versão
│   ├── java_test.go   # Testes da camada de invocação Java
│   └── root_test.go   # Testes de parsing e mapeamento dos comandos
├── main.go            # Ponto de entrada do binário
├── go.mod
└── README.md
```

## Uso da CLI

### Versão

```bash
go run . version
```

### Assinar arquivo (Modo Servidor - Padrão)

Por padrão, a CLI se comunica com o `assinador.jar` via HTTP. Se o servidor não estiver ativo, a CLI o iniciará automaticamente em segundo plano na porta indicada!

```bash
go run . sign --input arquivo.pdf --output arquivo.sig
```

### Validar assinatura (Modo Servidor - Padrão)

```bash
go run . validate --input arquivo.pdf --signature arquivo.sig
```

### Modo Servidor HTTP (Warm Start)

Para melhor desempenho, o servidor pode ser gerenciado de forma explícita com os comandos:

```bash
# Iniciar o servidor em background (porta padrão 8080, inatividade de 30 minutos)
go run . server start

# Iniciar em porta específica com timeout de inatividade personalizado (minutos)
go run . server start --port 9090 --shutdown-after 15

# Verificar o status de funcionamento do servidor
go run . server status --port 9090

# Parar o servidor de forma limpa (graceful shutdown)
go run . server stop --port 9090
```

### Modo Local (Cold Start)

Caso prefira executar a operação diretamente em uma nova instância da JVM local sem subir servidor, utilize o modo local:

```bash
go run . sign --mode local --input arquivo.pdf --output arquivo.sig
go run . validate --mode local --input arquivo.pdf --signature arquivo.sig
```

### Ajuda

```bash
go run . --help
go run . sign --help
go run . validate --help
go run . server --help
```

## Build e execução multiplataforma

Os comandos abaixo produzem binários para as 3 plataformas alvo.

```bash
# Windows
GOOS=windows GOARCH=amd64 go build -o dist/assinatura-windows-amd64.exe .

# Linux
GOOS=linux GOARCH=amd64 go build -o dist/assinatura-linux-amd64 .

# macOS
GOOS=darwin GOARCH=amd64 go build -o dist/assinatura-darwin-amd64 .
```

## Testes

```bash
go test ./...
```

## Releases e Downloads

O CLI está disponível para download e é empacotado individualmente para os sistemas `windows`, `linux` e `darwin` na página de [Releases](../../releases) deste repositório.

## Como Gerar Novos Artefatos / Releases

A geração de novos executáveis é 100% automatizada pelo pipeline de CI/CD via GitHub Actions. Para gerar e publicar uma nova versão:
1. Edite o arquivo `cmd/version.go` e altere a variável `var version = "x.y.z"`.
2. Faça o *commit* e envie (*push*) para a branch `main`.
3. O pipeline compilará as 3 plataformas automaticamente, anexará os `checksums` SHA256, assinará o pacote usando o *Cosign* e fará o deploy completo no [GitHub Releases](../../releases).

## Verificando Autenticidade com Cosign (Supply Chain Security)

Todas as nossas releases são assinadas digitalmente via **Sigstore / Cosign** usando a modalidade *keyless* (sem chaves longas) atrelada via certificado OIDC do GitHub Actions.
Para verificar os executáveis que você baixou na página de Releases, utilize as ferramentas de linha de comando exigidas.

**1. Verifique a Hash SHA256 (Opcional):**
```bash
sha256sum -c assinatura-vX.Y.Z-<os>-<arch>.sha256
```

**2. Verifique o Certificado Sigstore (Cosign):**
```bash
cosign verify-blob \
  --certificate assinatura-vX.Y.Z-<os>-<arch>.pem \
  --signature assinatura-vX.Y.Z-<os>-<arch>.sig \
  --certificate-identity "https://github.com/kyriosdata/runner/.github/workflows/ci.yml@refs/heads/main" \
  --certificate-oidc-issuer "https://token.actions.githubusercontent.com" \
  assinatura-vX.Y.Z-<os>-<arch>.exe
```

Se a saída for **"Verified OK"**, significa que o pacote veio infalivelmente do nosso pipeline do GitHub e não sofreu alterações no caminho.
