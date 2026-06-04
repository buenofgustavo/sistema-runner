# ADR 002: Estratégia de Descoberta de Instância Ativa

## Status
Aprovado

## Contexto
O CLI Go precisa ser capaz de detectar se um servidor do Assinador já está em execução localmente em uma porta específica para:
1. Reutilizar a instância e evitar o overhead de inicialização (Warm Start).
2. Evitar colisões e mensagens redundantes de inicialização.

## Decisão
A descoberta de instância ativa será realizada por um **Health Check real** em vez de apenas verificar se a porta TCP está ocupada por socket binding.
* A CLI enviará uma requisição `GET` ao endpoint `/api/health` da porta configurada.
* Se a resposta retornar `200 OK` e o JSON indicar o status `"UP"` com a mensagem esperada, a CLI considerará que o Assinador original está rodando naquela porta e reutilizará a conexão HTTP.
* Se a requisição falhar por conexão recusada ou timeout, assume-se que o servidor não está ativo (mesmo que a porta TCP esteja em escuta por outro processo não relacionado). Nesse caso, a CLI tenta iniciar um novo servidor e trata o erro de colisão se o processo Java falhar ao fazer o bind.

## Consequências
* Previne falsos positivos caso a porta 8080 esteja ocupada por um servidor web Apache, Tomcat ou Docker.
* Melhora a resiliência e a previsibilidade do ciclo de vida da integração CLI-servidor.
