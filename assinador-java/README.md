# Assinador Java

Este diretório contém o motor de execução e regras do validador de assinaturas FHIR do sistema. É um projeto desenvolvido em **Java 21** com **Spring Boot 4.x** e biblioteca **HAPI FHIR**.

É utilizado pela CLI `assinatura` para realizar os fluxos de assinatura e validação digital.

---

## Pré-requisitos

- **JDK 21** ou superior instalado
- **Maven 3.8** ou superior instalado

---

## Compilação e Empacotamento

Para compilar o código fonte e gerar o arquivo executável `.jar`:

```bash
mvn clean package
```

O arquivo compilado estará disponível em:
`target/assinador-java-1.0.0-SNAPSHOT.jar`

---

## Execução

### 1. Manualmente (Local/Stand-alone)
Você pode executar o servidor diretamente via linha de comando:

```bash
java -jar target/assinador-java-1.0.0-SNAPSHOT.jar
```
*(O servidor subirá por padrão na porta `8080`)*

### 2. Com a CLI de Assinatura
Para usar este JAR recém-compilado junto com a CLI Go `assinatura`, copie o arquivo para a pasta do executável renomeando-o para `assinador.jar`:

```bash
cp target/assinador-java-1.0.0-SNAPSHOT.jar ../assinatura/assinador.jar
```

---

## Execução de Testes Unitários (JUnit)

Para rodar todos os testes automatizados da aplicação Java:

```bash
mvn test
```
