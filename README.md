# Sistema Runner - Trabalho Prático

Este projeto implementa o **Sistema Runner**, uma interface unificada e simplificada para execução do Assinador digital, desenvolvida como trabalho prático da disciplina de Implementação e Integração (2026).

## Especificações de Referência
* A especificação formal e requisitos do projeto estão definidos no arquivo [especificacao_runner.md](especificacao_runner.md) sob o commit fixo [1cae2dfab95b072baa5c7a38063801cc6e2bce81](https://github.com/kyriosdata/runner/commit/1cae2dfab95b072baa5c7a38063801cc6e2bce81).

## Arquitetura
O sistema é estruturado em dois módulos principais:
1. **assinatura (CLI Go)**: Interface multiplataforma de linha de comandos desenvolvida em Go, responsável por interagir com o usuário e gerenciar o ciclo de vida do servidor do assinador.
2. **assinador-java (JAR Java)**: Aplicação executável Java que valida rigorosamente os parâmetros de entrada e executa/simula a validação e criação de assinaturas digitais.

A CLI Go se integra com o JAR Java por meio de dois modos de invocação:
* **Modo Local (Cold Start)**: Invocação direta executando `java -jar assinador.jar <comando>`.
* **Modo Servidor (Warm Start - Padrão)**: Inicia o JAR como um servidor HTTP local persistente e realiza a integração via chamadas REST (`POST /api/sign` e `POST /api/validate`).

## Como Construir os Projetos

### Assinador Java
Pré-requisitos: JDK 21+ e Maven instalados.
```bash
cd assinador-java
mvn clean package
```
O executável resultante estará localizado em `assinador-java/target/assinador-java-1.0.0-SNAPSHOT.jar` (que pode ser copiado ou referenciado como `assinador.jar`).

### CLI Go
Pré-requisitos: Go 1.26+ instalado.
```bash
cd assinatura
go build -o assinatura.exe .
```

## Como Executar os Testes

### Testes Java (JUnit)
```bash
cd assinador-java
mvn test
```

### Testes Go
```bash
cd assinatura
go test ./...
```

## Uso Rápido da CLI
```bash
# Versão
./assinatura version

# Criação de assinatura (Modo Servidor por padrão)
./assinatura sign --input arquivo.pdf --output assinatura.sig

# Validação de assinatura
./assinatura validate --input arquivo.pdf --signature assinatura.sig
```
Consulte as opções completas executando `./assinatura --help`.
