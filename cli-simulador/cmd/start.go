package cmd

import (
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"time"

	"github.com/spf13/cobra"
)

func newStartCmd() *cobra.Command {
	var port string

	cmd := &cobra.Command{
		Use:   "start",
		Short: "Inicia o simulador em background",
		RunE: func(cmd *cobra.Command, args []string) error {
			homeDir, err := os.UserHomeDir()
			if err != nil {
				return fmt.Errorf("não foi possível obter diretório do usuário: %w", err)
			}
			hubsaudeDir := filepath.Join(homeDir, ".hubsaude")
			if err := os.MkdirAll(hubsaudeDir, 0755); err != nil {
				return fmt.Errorf("falha ao criar diretório %s: %w", hubsaudeDir, err)
			}

			jarPath := filepath.Join(hubsaudeDir, simuladorJarName)
			pidPath := filepath.Join(hubsaudeDir, pidFileName)
			logPath := filepath.Join(hubsaudeDir, "simulador.log")

			// 1. Check if already running via health check
			if isServerRunning(port) {
				fmt.Fprintf(cmd.OutOrStdout(), "✅ Simulador HubSaúde já está em execução e respondendo na porta %s.\n", port)
				return nil
			}

			// 2. Ensure Java is installed
			javaPath, err := findJava()
			if err != nil {
				slog.Debug("Java 21+ não encontrado, iniciando provisionamento")
				if errProv := EnsureJDK(); errProv != nil {
					return fmt.Errorf("java não encontrado e falha ao provisionar: %w (erro original: %v)", errProv, err)
				}
				javaPath, err = findJava()
				if err != nil {
					return fmt.Errorf("java não encontrado após o provisionamento: %w", err)
				}
			}

			// 3. Ensure simulator JAR is downloaded
			if _, errJar := os.Stat(jarPath); errJar != nil {
				slog.Debug("JAR do simulador não encontrado, iniciando download")
				if errDown := downloadFileWithSHA256(simuladorJarURL, simuladorJarSHA256URL, jarPath); errDown != nil {
					return errDown
				}
			} else {
				slog.Debug("JAR do simulador encontrado", "path", jarPath)
			}

			// 4. Start background process
			slog.Debug("Executando", "java", javaPath, "args", fmt.Sprintf("-jar %s --server.port=%s", jarPath, port))

			javaArgs := []string{"-jar", jarPath, "--server.port=" + port}
			proc := exec.Command(javaPath, javaArgs...)

			// Redirect output to log file
			logFile, err := os.OpenFile(logPath, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
			if err != nil {
				return fmt.Errorf("falha ao abrir arquivo de log: %w", err)
			}
			defer logFile.Close()

			proc.Stdout = logFile
			proc.Stderr = logFile

			// Platform-specific process detaching
			detachProcess(proc)

			if err := proc.Start(); err != nil {
				return fmt.Errorf("falha ao iniciar o processo Java: %w", err)
			}

			pid := proc.Process.Pid
			fmt.Fprintf(cmd.OutOrStdout(), "🚀 Simulador HubSaúde iniciado (PID %d). Aguardando prontidão na porta %s...\n", pid, port)

			// Write PID file
			if err := os.WriteFile(pidPath, []byte(strconv.Itoa(pid)), 0644); err != nil {
				slog.Warn("não foi possível salvar o arquivo de PID", "path", pidPath, "error", err)
			}

			// 5. Wait for readiness
			success := false
			deadline := time.Now().Add(healthCheckTimeout)
			for time.Now().Before(deadline) {
				if isServerRunning(port) {
					success = true
					break
				}
				time.Sleep(500 * time.Millisecond)
			}

			if success {
				fmt.Fprintf(cmd.OutOrStdout(), "✅ Simulador HubSaúde pronto e respondendo na porta %s.\n", port)
			} else {
				return fmt.Errorf("❌ Limite de tempo excedido: o simulador não respondeu na porta %s após %v", port, healthCheckTimeout)
			}

			return nil
		},
	}

	cmd.Flags().StringVar(&port, "port", defaultPort, "Porta do simulador")

	return cmd
}

func isServerRunning(port string) bool {
	client := &http.Client{Timeout: 1 * time.Second}
	// 1. Try healthPath (which is /health)
	url := fmt.Sprintf("http://127.0.0.1:%s%s", port, healthPath)
	resp, err := client.Get(url)
	if err == nil {
		defer resp.Body.Close()
		if resp.StatusCode == http.StatusOK {
			return true
		}
	}
	// 2. Try actuator health as fallback
	urlActuator := fmt.Sprintf("http://127.0.0.1:%s/actuator/health", port)
	resp2, err2 := client.Get(urlActuator)
	if err2 == nil {
		defer resp2.Body.Close()
		if resp2.StatusCode == http.StatusOK {
			return true
		}
	}
	return false
}
