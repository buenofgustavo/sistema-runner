# Contexto e Escopo - Trabalho Prático

## 1. Visão geral

Este documento define o contexto e escopo do trabalho prático da disciplina de Implementação e Integração do Bacharelado em Engenharia de Software (2026). O trabalho visa proporcionar aos estudantes a oportunidade de prática de construção de software.

## 2. Objetivos

- Uma aplicação multiplataforma via linha de comandos (CLI)
- Integração entre aplicações
- Validação de parâmetros e tratamento de erros
- Simulação de funcionalidades complexas

## 3. Descrição do sistema

O sistema é composto por **duas aplicações** que trabalham de forma integrada:

### 3.1. Aplicação assinatura

**Descrição:** interface via linha de comandos (console) para interação com usuários humanos.

**Características:**
- Multiplataforma (Windows, Linux e macOS)
- Interface de linha de comandos (CLI - Command Line Interface)
- Integra-se com a aplicação assinador.jar
- Fornece uma interface amigável para usuários humanos acessarem funcionalidades de assinatura digital

**Responsabilidades:**
- Receber comandos do usuário
- Validar consistência sintática dos parâmetros de entrada do usuário
- Invocar a aplicação assinador.jar com os parâmetros
- Apresentar resultados ao usuário de forma legível

### 3.2. Aplicação assinador.jar

**Descrição:** aplicação Java que valida parâmetros de entrada e simula a criação e validação de assinaturas digitais.

**Características:**
- Implementada em Java (arquivo .jar)
- Não realiza assinatura digital real (apenas simulação)
- Valida parâmetros de entrada
- Retorna respostas pré-construídas
- Suporta dois modos de execução:
  - **Modo local (CLI)**: a aplicação é invocada diretamente via linha de comandos. Cada execução realiza o ciclo completo de inicialização da JVM e carga da aplicação (*cold start*), sendo adequado para execuções esporádicas ou scripts de automação.
  - **Modo servidor (HTTP)**: a aplicação é iniciada uma única vez e permanece em execução, aguardando requisições. Este modo elimina o overhead de inicialização nas chamadas subsequentes (*warm start*), oferecendo menor latência e maior throughput para cenários com múltiplas requisições.

**Responsabilidades:**
- Validar parâmetros recebidos para operações de criação e validação de assinatura
- Reagir corretamente na presença de falhas (parâmetros inválidos)
- Em caso de sucesso na validação:
  - Para **criação de assinatura**: retornar uma assinatura previamente construída (simulada)
  - Para **validação de assinatura**: retornar indicação de sucesso ou falha no formato esperado
- Garantir que todos os parâmetros estejam corretos antes de processar

## 4. Modelo C4 - Diagramas de Arquitetura

### 4.1. Diagrama de Contexto (Nível 1)

```
                                    ┌─────────────────┐
                                    │                 │
                                    │    Usuário      │
                                    │                 │
                                    └────────┬────────┘
                                             │
                                             │ Interage via
                                             │ linha de comandos
                                             │
                                             ▼
                    ┌────────────────────────────────────────┐
                    │                                        │
                    │   Sistema de Assinatura Digital       │
                    │          (Simulado)                    │
                    │                                        │
                    │  - Criar assinaturas digitais         │
                    │  - Validar assinaturas digitais       │
                    │                                        │
                    └────────────────────────────────────────┘
                                             │
                                             │ Baseado em
                                             │ especificações
                                             ▼
                    ┌────────────────────────────────────────┐
                    │                                        │
                    │  Especificações FHIR Saúde GO         │
                    │  (Parâmetros de Assinatura)           │
                    │                                        │
                    └────────────────────────────────────────┘
```

**Descrição do Contexto:**
- **Usuário**: Pessoa que utiliza o sistema através de comandos de terminal
- **Sistema de Assinatura Digital**: O sistema completo que simula operações de assinatura digital
- **Especificações FHIR**: Define os parâmetros precisos para criação e validação de assinaturas

### 4.2. Diagrama de Contêiner (Nível 2)

```
                                    ┌─────────────────┐
                                    │                 │
                                    │    Usuário      │
                                    │                 │
                                    └────────┬────────┘
                                             │
                                             │ Comandos CLI
                                             │ (criar, validar)
                                             │
                                             ▼
                    ┌────────────────────────────────────────┐
                    │                                        │
                    │      Assinador (Aplicação CLI)        │
                    │                                        │
                    │  - Aplicação multiplataforma           │
                    │  - Interface de linha de comandos      │
                    │  - Valida entrada do usuário          │
                    │  - Formata saída para o usuário       │
                    │                                        │
                    │  Tecnologia: A definir                 │
                    │  Plataformas: Windows, Linux, macOS    │
                    │                                        │
                    └──────────────┬─────────────────────────┘
                                   │
                                   │ Invoca com
                                   │ parâmetros
                                   │ validados
                                   │
                                   ▼
                    ┌────────────────────────────────────────┐
                    │                                        │
                    │   Assinatura.jar (Aplicação Java)     │
                    │                                        │
                    │  - Valida parâmetros de entrada       │
                    │  - Trata erros e exceções             │
                    │  - Simula criação de assinatura       │
                    │  - Simula validação de assinatura     │
                    │  - Retorna respostas pré-construídas  │
                    │                                        │
                    │  Tecnologia: Java (JAR executável)     │
                    │                                        │
                    └────────────────────────────────────────┘
```

**Descrição dos Contêineres:**

1. **Assinador (Aplicação CLI)**
   - Tipo: Aplicação de linha de comandos
   - Função: Interface para o usuário humano
   - Comunicação: Invoca assinatura.jar via linha de comandos

2. **Assinatura.jar (Aplicação Java)**
   - Tipo: Aplicação Java executável
   - Função: Motor de simulação de assinaturas digitais
   - Comunicação: Recebe parâmetros e retorna resultados

## 5. Funcionalidades

### 5.1. Criar Assinatura Digital (Simulada)

**Entrada:**
- Parâmetros conforme especificação FHIR para criação de assinatura
- Referência: [caso-de-uso-criar-assinatura.html](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-criar-assinatura.html)

**Processamento:**
1. Validar todos os parâmetros recebidos
2. Verificar formato e completude dos dados
3. Se válido: retornar assinatura pré-construída
4. Se inválido: retornar mensagem de erro apropriada

**Saída:**
- Sucesso: Assinatura digital simulada (pré-construída)
- Falha: Mensagem de erro indicando o problema

### 5.2. Validar Assinatura Digital (Simulada)

**Entrada:**
- Parâmetros conforme especificação FHIR para validação de assinatura
- Referência: [caso-de-uso-validar-assinatura.html](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-validar-assinatura.html)

**Processamento:**
1. Validar todos os parâmetros recebidos
2. Verificar formato da assinatura e dados associados
3. Se válido: retornar resultado simulado (sucesso/falha)
4. Se inválido: retornar mensagem de erro apropriada

**Saída:**
- Sucesso: Indicação se a assinatura é válida ou inválida (simulado)
- Falha: Mensagem de erro indicando o problema

## 6. Requisitos Técnicos

### 6.1. Aplicação Assinador

**Requisitos Funcionais:**
- RF01: Deve funcionar em Windows, Linux e macOS
- RF02: Deve fornecer interface via linha de comandos
- RF03: Deve validar entrada do usuário antes de invocar assinatura.jar
- RF04: Deve apresentar resultados de forma legível ao usuário
- RF05: Deve tratar erros e apresentar mensagens apropriadas

**Requisitos Não-Funcionais:**
- RNF01: Deve ser fácil de instalar e executar
- RNF02: Deve ter documentação clara de uso
- RNF03: Mensagens de erro devem ser claras e acionáveis

### 6.2. Aplicação Assinatura.jar

**Requisitos Funcionais:**
- RF01: Deve validar rigorosamente todos os parâmetros de entrada
- RF02: Deve implementar operação de criação de assinatura (simulada)
- RF03: Deve implementar operação de validação de assinatura (simulada)
- RF04: Deve retornar erros claros quando parâmetros são inválidos
- RF05: Deve seguir as especificações FHIR para parâmetros

**Requisitos Não-Funcionais:**
- RNF01: Deve ser executável em qualquer sistema com JVM
- RNF02: Deve ter tratamento robusto de erros
- RNF03: Deve retornar resultados em formato estruturado

## 7. Integração entre Aplicações

### 7.1. Fluxo de Criação de Assinatura

```
Usuário → Assinador → assinatura.jar → Assinador → Usuário

1. Usuário: Executa comando para criar assinatura
2. Assinador: Valida entrada do usuário
3. Assinador: Invoca assinatura.jar com parâmetros
4. Assinatura.jar: Valida parâmetros
5. Assinatura.jar: Retorna assinatura simulada
6. Assinador: Formata resultado
7. Assinador: Apresenta ao usuário
```

### 7.2. Fluxo de Validação de Assinatura

```
Usuário → Assinador → assinatura.jar → Assinador → Usuário

1. Usuário: Executa comando para validar assinatura
2. Assinador: Valida entrada do usuário
3. Assinador: Invoca assinatura.jar com parâmetros
4. Assinatura.jar: Valida parâmetros
5. Assinatura.jar: Retorna resultado simulado
6. Assinador: Formata resultado
7. Assinador: Apresenta ao usuário
```

### 7.3. Tratamento de Erros

Em qualquer ponto do fluxo, erros devem ser:
- Capturados apropriadamente
- Propagados de forma estruturada
- Apresentados ao usuário de forma clara
- Incluir informação suficiente para correção

## 8. Parâmetros de Entrada

Os parâmetros para as operações de criação e validação de assinatura digital estão definidos de forma precisa nas especificações FHIR:

### 8.1. Parâmetros para Criar Assinatura
- **Referência**: https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-criar-assinatura.html
- **Descrição**: Define todos os parâmetros necessários para solicitar a criação de uma assinatura digital

### 8.2. Parâmetros para Validar Assinatura
- **Referência**: https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-validar-assinatura.html
- **Descrição**: Define todos os parâmetros necessários para solicitar a validação de uma assinatura digital

**Observação**: A implementação deve seguir rigorosamente estas especificações para garantir compatibilidade e corretude.

## 9. Escopo do Trabalho Prático

### 9.1. O que ESTÁ no Escopo

✅ Desenvolvimento da aplicação Assinador (CLI multiplataforma)
✅ Desenvolvimento da aplicação assinatura.jar
✅ Integração entre as duas aplicações
✅ Validação rigorosa de parâmetros
✅ Simulação de criação de assinatura
✅ Simulação de validação de assinatura
✅ Tratamento de erros e exceções
✅ Testes das funcionalidades
✅ Documentação de uso

### 9.2. O que NÃO ESTÁ no Escopo

❌ Implementação real de assinatura digital criptográfica
❌ Integração com autoridades certificadoras
❌ Armazenamento persistente de assinaturas
❌ Interface gráfica (GUI - Graphical User Interface)
❌ API web ou serviços REST
❌ Autenticação de usuários
❌ Geração real de certificados digitais

## 10. Entregáveis

1. **Código-fonte da aplicação Assinador**
   - Implementação completa
   - Compatível com Windows, Linux e macOS
   - Código bem documentado

2. **Código-fonte da aplicação assinatura.jar**
   - Implementação em Java
   - Validação completa de parâmetros
   - Simulação das operações

3. **Testes**
   - Testes unitários
   - Testes de integração
   - Casos de teste para cenários de erro

4. **Documentação**
   - Manual de usuário para Assinador
   - Documentação técnica da integração
   - Exemplos de uso
   - Guia de instalação

5. **Especificação (este documento)**
   - Contexto e escopo definidos
   - Diagramas C4
   - Requisitos documentados

## 11. Considerações de Implementação

### 11.1. Simulação

Como o sistema **simula** operações de assinatura digital:
- **Para criação**: Prepare assinaturas de exemplo pré-construídas que podem ser retornadas quando os parâmetros são válidos
- **Para validação**: Implemente lógica simples que sempre retorna um resultado pré-determinado (válido/inválido) baseado em critérios simples
- **Foco na validação**: A maior parte do esforço deve estar em validar corretamente os parâmetros de entrada

### 11.2. Tecnologias Sugeridas

**Para Assinador:**
- Python (com bibliotecas como Click ou argparse)
- Go (com Cobra)
- Rust (com Clap)
- Node.js (com Commander)

**Para assinatura.jar:**
- Java 11+ (LTS - Long Term Support)
- Maven ou Gradle para construção (build)
- JUnit para testes

### 11.3. Padrões de Qualidade

- Código limpo e bem organizado
- Tratamento adequado de exceções
- Testes com boa cobertura
- Documentação clara
- Mensagens de erro úteis

## 12. Cronograma e Fases

O desenvolvimento será realizado de forma **paulatina**, ao longo da disciplina:

**Fase 1**: Especificação e planejamento (este documento)
**Fase 2**: Implementação do assinatura.jar
**Fase 3**: Implementação do Assinador
**Fase 4**: Integração e testes
**Fase 5**: Documentação e refinamentos

## 13. Referências

1. **Especificações FHIR - Segurança**
   - [Caso de Uso: Criar Assinatura](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-criar-assinatura.html)
   - [Caso de Uso: Validar Assinatura](https://fhir.saude.go.gov.br/r4/seguranca/caso-de-uso-validar-assinatura.html)

2. **Modelo C4 para Visualização de Arquitetura**
   - [C4 Model](https://c4model.com/)
   - Nível 1: Diagrama de Contexto
   - Nível 2: Diagrama de Contêiner

3. **Boas Práticas de CLI**
   - Mensagens claras e consistentes
   - Tratamento adequado de erros
   - Documentação de help integrada

## 14. Glossário

- **Assinador**: Aplicação de linha de comandos que serve como interface para o usuário
- **assinatura.jar**: Aplicação Java que simula operações de assinatura digital
- **Assinatura Digital**: No contexto deste trabalho, uma simulação de assinatura criptográfica
- **FHIR**: Fast Healthcare Interoperability Resources - Recursos de Interoperabilidade Rápida em Saúde, padrão para troca de informações de saúde
- **CLI**: Command Line Interface - Interface de Linha de Comandos
- **GUI**: Graphical User Interface - Interface Gráfica do Usuário
- **API**: Application Programming Interface - Interface de Programação de Aplicações
- **REST**: Representational State Transfer - Transferência de Estado Representacional
- **JAR**: Java Archive - Formato de arquivo executável Java
- **LTS**: Long Term Support - Suporte de Longo Prazo
- **Simulação**: Comportamento que imita a operação real sem executar a lógica criptográfica complexa

---

**Versão**: 1.0  
**Data**: 2026  
**Disciplina**: Implementação e Integração  
**Curso**: Bacharelado em Engenharia de Software
