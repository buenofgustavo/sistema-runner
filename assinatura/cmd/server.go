package cmd

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"time"

	"github.com/spf13/cobra"
)

func newServerCmd() *cobra.Command {
	serverCmd := &cobra.Command{
		Use:   "server",
		Short: "Gerencia o ciclo de vida do servidor do Assinador",
		Long:  `Permite iniciar, parar e verificar o status da instância do assinador.jar em modo servidor HTTP.`,
	}

	serverCmd.AddCommand(newServerStartCmd())
	serverCmd.AddCommand(newServerStopCmd())
	serverCmd.AddCommand(newServerStatusCmd())

	return serverCmd
}

func newServerStartCmd() *cobra.Command {
	var jarPath string
	var port string
	var shutdownAfter string

	cmd := &cobra.Command{
		Use:   "start",
		Short: "Inicia o servidor do Assinador",
		RunE: func(cmd *cobra.Command, args []string) error {
			url := fmt.Sprintf("http://localhost:%s/api/health", port)
			
			// 1. Verificar se a porta está em uso
			ln, err := net.Listen("tcp", ":"+port)
			if err != nil {
				// Porta está ocupada. Vamos verificar se é o nosso Assinador.
				if isServerRunning(url) {
					if !Quiet {
						fmt.Fprintf(cmd.OutOrStdout(), "Status: Servidor já está em execução na porta %s\n", port)
					}
					return nil
				}
				// É outro processo ocupando a porta!
				return fmt.Errorf("falha ao iniciar servidor: a porta %s ja esta sendo utilizada por outro processo no sistema", port)
			}
			// Porta livre, liberar e prosseguir
			_ = ln.Close()

			// 2. Localizar java
			javaPath, err := findJava()
			if err != nil {
				// Java não encontrado, tentar provisionar automaticamente
				provisionErr := EnsureJDK()
				if provisionErr != nil {
					return fmt.Errorf("java nao encontrado e falha ao provisionar: %w (erro original: %v)", provisionErr, err)
				}
				javaPath, err = findJava()
				if err != nil {
					return fmt.Errorf("java nao encontrado apos o provisionamento: %w", err)
				}
			}

			// 3. Validar existência do jar
			if _, err := os.Stat(jarPath); err != nil {
				return fmt.Errorf("arquivo jar nao encontrado: %s. Verifique o caminho ou use --jar", jarPath)
			}

			// 4. Iniciar processo em background
			absoluteJarPath, err := filepath.Abs(jarPath)
			if err != nil {
				absoluteJarPath = jarPath
			}

			javaArgs := []string{"-jar", absoluteJarPath, "--server", "--port", port, "--shutdown-after", shutdownAfter}
			process := exec.Command(javaPath, javaArgs...)
			
			// Desacoplar a saída para que o CLI não trave
			process.Stdout = nil
			process.Stderr = nil

			err = process.Start()
			if err != nil {
				return fmt.Errorf("falha ao iniciar o processo Java: %w", err)
			}

			if !Quiet {
				fmt.Fprintf(cmd.OutOrStdout(), "Iniciando o Assinador na porta %s (timeout de inatividade: %s min)...\n", port, shutdownAfter)
			}

			// 5. Aguardar até 5 segundos para confirmar a inicialização
			success := false
			for i := 0; i < 10; i++ {
				time.Sleep(500 * time.Millisecond)
				if isServerRunning(url) {
					success = true
					break
				}
			}

			if success {
				if !Quiet {
					fmt.Fprintf(cmd.OutOrStdout(), "Status: Servidor iniciado com sucesso e ouvindo na porta %s\n", port)
				}
			} else {
				return fmt.Errorf("servidor iniciado, mas nao respondeu ao teste de saude na porta %s", port)
			}

			return nil
		},
	}

	cmd.Flags().StringVar(&jarPath, "jar", "assinador.jar", "Caminho para o assinador.jar")
	cmd.Flags().StringVar(&port, "port", "8080", "Porta para escutar as requisições")
	cmd.Flags().StringVar(&shutdownAfter, "shutdown-after", "30", "Minutos sem atividade para desligamento automático")

	return cmd
}

func newServerStopCmd() *cobra.Command {
	var port string

	cmd := &cobra.Command{
		Use:   "stop",
		Short: "Interrompe o servidor do Assinador",
		RunE: func(cmd *cobra.Command, args []string) error {
			url := fmt.Sprintf("http://localhost:%s/api/shutdown", port)

			req, err := http.NewRequest("POST", url, bytes.NewBuffer([]byte{}))
			if err != nil {
				return err
			}
			req.Header.Set("Content-Type", "application/json")

			client := &http.Client{Timeout: 5 * time.Second}
			resp, err := client.Do(req)
			if err != nil {
				// Se der erro de conexão, provavelmente já está parado
				fmt.Fprintf(cmd.OutOrStdout(), "Status: Não foi possível conectar ao servidor na porta %s (talvez já esteja desligado)\n", port)
				return nil
			}
			defer resp.Body.Close()

			if resp.StatusCode == http.StatusOK {
				fmt.Fprintf(cmd.OutOrStdout(), "Status: Comando de parada enviado com sucesso. O servidor na porta %s está sendo encerrado.\n", port)
			} else {
				fmt.Fprintf(cmd.OutOrStdout(), "Status: Servidor respondeu com código de erro ao tentar parar: %d\n", resp.StatusCode)
			}

			return nil
		},
	}

	cmd.Flags().StringVar(&port, "port", "8080", "Porta da instância do servidor a interromper")

	return cmd
}

func newServerStatusCmd() *cobra.Command {
	var port string

	cmd := &cobra.Command{
		Use:   "status",
		Short: "Verifica o status do servidor do Assinador",
		RunE: func(cmd *cobra.Command, args []string) error {
			url := fmt.Sprintf("http://localhost:%s/api/health", port)

			client := &http.Client{Timeout: 2 * time.Second}
			resp, err := client.Get(url)
			if err != nil {
				fmt.Fprintf(cmd.OutOrStdout(), "Status: O servidor NÃO está em execução na porta %s\n", port)
				return nil
			}
			defer resp.Body.Close()

			if resp.StatusCode == http.StatusOK {
				var result map[string]interface{}
				if err := json.NewDecoder(resp.Body).Decode(&result); err == nil {
					fmt.Fprintf(cmd.OutOrStdout(), "Status: Servidor em execução e SAUDÁVEL na porta %s\n", port)
					if msg, ok := result["message"]; ok {
						fmt.Fprintf(cmd.OutOrStdout(), "Mensagem: %v\n", msg)
					}
				} else {
					fmt.Fprintf(cmd.OutOrStdout(), "Status: Servidor ativo na porta %s, mas respondeu com formato inválido\n", port)
				}
			} else {
				fmt.Fprintf(cmd.OutOrStdout(), "Status: Servidor ativo na porta %s, mas retornou status HTTP %d\n", port, resp.StatusCode)
			}

			return nil
		},
	}

	cmd.Flags().StringVar(&port, "port", "8080", "Porta do servidor a verificar")

	return cmd
}

func isServerRunning(url string) bool {
	client := &http.Client{Timeout: 1 * time.Second}
	resp, err := client.Get(url)
	if err != nil {
		return false
	}
	defer resp.Body.Close()
	return resp.StatusCode == http.StatusOK
}
