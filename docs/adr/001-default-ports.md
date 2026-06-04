# ADR 001: Seleção de Portas Padrão

## Status
Aprovado

## Contexto
O Sistema Runner precisa expor o Assinador e potencialmente o Simulador como servidores HTTP em portas locais do sistema. Para simplificar a experiência do usuário, é conveniente ter portas padrão definidas na CLI e no servidor.

## Decisão
1. A porta padrão para a instância do **Assinador** (`assinador.jar`) será **8080**.
2. A CLI Go (`assinatura`) assume o valor `8080` caso nenhuma porta seja especificada via flag `--port`.
3. Caso a porta 8080 esteja indisponível e seja ocupada por outro processo não relacionado ao assinador, o sistema deverá falhar de forma explícita e informar o usuário sobre a colisão, em vez de tentar adivinhar ou falhar silenciosamente.

## Consequências
* O usuário não precisa informar `--port` na maioria das execuções em ambiente de desenvolvimento local limpo.
* Garante isolamento caso o usuário deseje subir múltiplos runners em portas customizadas informando a flag.
