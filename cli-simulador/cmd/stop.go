package cmd

import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/spf13/cobra"
)

func newStopCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "stop",
		Short: "Encerra o simulador que está rodando em background",
		RunE: func(cmd *cobra.Command, args []string) error {
			homeDir, err := os.UserHomeDir()
			if err != nil {
				return fmt.Errorf("não foi possível obter diretório do usuário: %w", err)
			}
			hubsaudeDir := filepath.Join(homeDir, ".hubsaude")
			pidPath := filepath.Join(hubsaudeDir, pidFileName)

			// Read PID file
			pidData, err := os.ReadFile(pidPath)
			if err != nil {
				fmt.Fprintln(cmd.OutOrStdout(), "ℹ️ O simulador não está em execução (nenhuma referência de PID localizada).")
				return nil
			}

			pidStr := strings.TrimSpace(string(pidData))
			pid, err := strconv.Atoi(pidStr)
			if err != nil {
				fmt.Fprintln(cmd.OutOrStdout(), "⚠️ Arquivo de PID corrompido ou inválido. Limpando arquivo...")
				_ = os.Remove(pidPath)
				return nil
			}

			// Check process running
			if !isProcessRunning(pid) {
				fmt.Fprintf(cmd.OutOrStdout(), "ℹ️ O processo com PID %d já se encontra finalizado.\n", pid)
				_ = os.Remove(pidPath)
				return nil
			}

			// Find process
			proc, err := os.FindProcess(pid)
			if err != nil {
				return fmt.Errorf("❌ Erro ao obter referência do processo PID %d: %w", pid, err)
			}

			// Kill process
			if err := proc.Kill(); err != nil {
				return fmt.Errorf("❌ Falha ao finalizar o processo PID %d: %w", pid, err)
			}

			fmt.Fprintf(cmd.OutOrStdout(), "🛑 Simulador HubSaúde (PID %d) finalizado com sucesso.\n", pid)
			_ = os.Remove(pidPath)
			return nil
		},
	}
}
