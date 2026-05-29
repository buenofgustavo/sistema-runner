package cmd

import (
	"fmt"
	"strings"

	"github.com/spf13/cobra"
)

func newValidateCmd(invoker Invoker) *cobra.Command {
	var jarPath string
	var inputPath string
	var signaturePath string
	var mode string
	var port string

	cmd := &cobra.Command{
		Use:   "validate",
		Short: "Valida assinatura digital de um artefato",
		Long:  `Executa o assinador.jar para validar assinatura digital de um arquivo.`,
		RunE: func(cmd *cobra.Command, args []string) error {
			var activeInvoker Invoker = invoker
			if mode == "server" {
				activeInvoker = NewHttpInvoker(port, jarPath)
			}

			javaArgs := []string{"validate", "--input", inputPath, "--signature", signaturePath}
			output, err := activeInvoker.Run(jarPath, javaArgs)

			if err != nil {
				if strings.Contains(err.Error(), "exit status") {
					fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura INVALIDA\n")
					if strings.TrimSpace(output) != "" {
						fmt.Fprintf(cmd.OutOrStdout(), "Detalhes:\n%s\n", strings.TrimSpace(output))
					}
					return nil
				}
				return fmt.Errorf("erro ao validar assinatura: %w", err)
			}

			fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura VALIDA\n")
			fmt.Fprintf(cmd.OutOrStdout(), "Arquivo: %s\n", inputPath)
			fmt.Fprintf(cmd.OutOrStdout(), "Assinatura: %s\n", signaturePath)
			if strings.TrimSpace(output) != "" {
				fmt.Fprintf(cmd.OutOrStdout(), "Detalhes:\n%s\n", strings.TrimSpace(output))
			}

			return nil
		},
	}

	cmd.Flags().StringVar(&jarPath, "jar", "assinador.jar", "Caminho para o assinador.jar")
	cmd.Flags().StringVar(&inputPath, "input", "", "Arquivo de entrada para validacao")
	cmd.Flags().StringVar(&signaturePath, "signature", "", "Arquivo de assinatura")
	cmd.Flags().StringVar(&mode, "mode", "server", "Modo de execucao: local ou server")
	cmd.Flags().StringVar(&port, "port", "8080", "Porta do servidor a ser utilizada no modo server")
	_ = cmd.MarkFlagRequired("input")
	_ = cmd.MarkFlagRequired("signature")

	return cmd
}
