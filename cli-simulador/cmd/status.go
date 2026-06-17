package cmd

import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/spf13/cobra"
)

func newStatusCmd() *cobra.Command {
	var port string

	cmd := &cobra.Command{
		Use:   "status",
		Short: "Verifica o estado do simulador",
		RunE: func(cmd *cobra.Command, args []string) error {
			homeDir, err := os.UserHomeDir()
			if err != nil {
				return fmt.Errorf("não foi possível obter diretório do usuário: %w", err)
			}
			hubsaudeDir := filepath.Join(homeDir, ".hubsaude")
			pidPath := filepath.Join(hubsaudeDir, pidFileName)

			// Helper function to print PARADO status
			printStopped := func() {
				fmt.Fprintln(cmd.OutOrStdout(), "🔴 ESTADO: PARADO")
				fmt.Fprintln(cmd.OutOrStdout(), "   Nenhuma instância registrada em ~/.hubsaude/simulador.pid")
			}

			// 1. Check if server is running on the port
			runningOnPort := isServerRunning(port)

			// 2. Read PID
			pidData, err := os.ReadFile(pidPath)
			var pid int
			if err == nil {
				if p, errP := strconv.Atoi(strings.TrimSpace(string(pidData))); errP == nil {
					pid = p
				}
			}

			if runningOnPort {
				fmt.Fprintln(cmd.OutOrStdout(), "🟢 ESTADO: ATIVO (PRONTO)")
				fmt.Fprintf(cmd.OutOrStdout(), "   PID    : %d\n", pid)
				fmt.Fprintf(cmd.OutOrStdout(), "   Porta  : %s\n", port)
				fmt.Fprintf(cmd.OutOrStdout(), "   Health : OK (http://localhost:%s%s)\n", port, healthPath)
				return nil
			}

			// 3. If not responding, check if the process is running
			if pid > 0 && isProcessRunning(pid) {
				fmt.Fprintln(cmd.OutOrStdout(), "🟡 ESTADO: PROCESSO EM EXECUÇÃO, MAS NÃO RESPONDE")
				fmt.Fprintf(cmd.OutOrStdout(), "   PID    : %d\n", pid)
				fmt.Fprintf(cmd.OutOrStdout(), "   Porta  : %s\n", port)
				fmt.Fprintln(cmd.OutOrStdout(), "   Health : FALHOU")
				fmt.Fprintln(cmd.OutOrStdout(), "   Dica   : O processo do simulador está ativo, mas a API não está respondendo. Pode estar inicializando ou inoperante. Use 'simulador stop' para resetar.")
			} else {
				_ = os.Remove(pidPath) // Clean up stale PID file if process not running
				printStopped()
			}

			return nil
		},
	}

	cmd.Flags().StringVar(&port, "port", defaultPort, "Porta do simulador")

	return cmd
}
