# Sistema Runner

[![CI & Release](https://github.com/buenofgustavo/sistema-runner/actions/workflows/ci.yml/badge.svg)](https://github.com/buenofgustavo/sistema-runner/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java: 21+](https://img.shields.io/badge/Java-21+-orange.svg)](https://adoptium.net/)
[![Go: 1.26+](https://img.shields.io/badge/Go-1.26+-blue.svg)](https://go.dev/)

Este repositório contém a implementação integrada do **Sistema Runner**, desenvolvido como trabalho prático da disciplina de Implementação e Integração (2026).

O projeto é composto por dois utilitários CLI em Go e um servidor de validação em Java:

1. **Assinatura CLI (`assinatura`)**: Ferramenta para assinar e validar artefatos digitalmente via JVM/HTTP.
2. **Simulador CLI (`simulador`)**: Ferramenta para gerenciar o ciclo de vida (iniciar, parar e verificar status) do simulador de validação HubSaúde.
3. **Assinador Java (`assinador.jar`)**: O motor de regras e validação FHIR.

---

## 📖 Onde encontrar cada instrução?

Para facilitar a navegação, as documentações específicas de cada componente foram organizadas em seus respectivos diretórios:

### 1. Assinatura CLI e Assinador Java
As instruções detalhadas de compilação, execução, testes locais e testes avançados (REST/FHIR) da ferramenta de assinatura estão no arquivo:
👉 **[assinatura/README.md](assinatura/README.md)**

### 2. Simulador CLI HubSaúde
As instruções de download, compilação, execução em segundo plano, monitoramento de saúde e integridade da CLI do Simulador estão no arquivo:
👉 **[cli-simulador/README.md](cli-simulador/README.md)**

---

## ⚙️ CI/CD e Releases

Os executáveis de ambos os componentes (`assinatura` e `cli-simulador`) para Windows, Linux e macOS são compilados, testados e assinados digitalmente de forma automatizada pelo GitHub Actions em cada nova tag gerada.

- As releases podem ser baixadas em: [GitHub Releases](../../releases)
- As instruções de validação de assinatura criptográfica das releases com o **Cosign** podem ser encontradas nas respectivas documentações de cada CLI.

---

## ⚖️ Licença

Este projeto está licenciado sob a Licença MIT - consulte o arquivo [LICENSE](LICENSE) para obter detalhes.
