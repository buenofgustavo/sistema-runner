# ADR 003: Biblioteca de Parsing de CLI e Modos de Invocação

## Status
Aprovado

## Contexto
O CLI Go atua como uma interface centralizada de usuário, fornecendo subcomandos como `sign`, `validate` e `server` com flags associadas. Uma biblioteca padrão de parsing Go (`flag`) é muito rudimentar para subcomandos aninhados.

## Decisão
1. Utilizar a biblioteca **`github.com/spf13/cobra`** para gerenciar o roteamento, árvore de comandos e parsing de flags da CLI Go.
2. Definir o **modo servidor (Warm Start)** como a estratégia padrão de integração das operações de assinatura e validação.
3. Permitir que o usuário opte explicitamente pelo **modo local (Cold Start)** informando a flag `--mode local`.

## Consequências
* A CLI obtém autogeração de telas de ajuda detalhadas (`--help`) de forma transparente e idiomática.
* Promove uma experiência rápida de Warm Start por padrão, mantendo a flexibilidade de Cold Start para scripts simples.
