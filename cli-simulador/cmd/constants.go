package cmd

import "time"

const (
	simuladorJarURL       = "https://github.com/kyriosdata/runner/releases/download/hubsaude-validador-api-v0.1.10/hubsaude-validador-api-0.1.10-exec.jar"
	// URL do arquivo SHA-256 publicado junto com o JAR.
	simuladorJarSHA256URL = "https://github.com/kyriosdata/runner/releases/download/hubsaude-validador-api-v0.1.10/hubsaude-validador-api-0.1.10-exec.jar.sha256"
	// Nome local do JAR após download.
	simuladorJarName      = "hubsaude-validador-api.jar"
	// Arquivo que armazena o PID do processo em execução.
	pidFileName           = "simulador.pid"
	// Porta padrão do simulador.
	defaultPort           = "8080"
	// Endpoint de health check.
	healthPath            = "/health"
	// Tempo máximo aguardando o simulador ficar pronto.
	healthCheckTimeout    = 30 * time.Second
)
