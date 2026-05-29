package cmd

import (
	"fmt"
	"strings"

	"github.com/spf13/cobra"
)

func newSignCmd(invoker Invoker) *cobra.Command {
	var jarPath string
	var inputPath string
	var outputPath string
	var mode string
	var port string

	cmd := &cobra.Command{
		Use:   "sign",
		Short: "Cria assinatura digital de um artefato",
		Long:  `Executa o assinador.jar para criar assinatura digital de um arquivo.`,
		RunE: func(cmd *cobra.Command, args []string) error {
			var activeInvoker Invoker = invoker
			if mode == "server" {
				activeInvoker = NewHttpInvoker(port, jarPath)
			}

			javaArgs := []string{"sign", "--input", inputPath, "--output", outputPath}
			output, err := activeInvoker.Run(jarPath, javaArgs)
			if err != nil {
				if strings.TrimSpace(output) != "" {
					return fmt.Errorf("erro ao criar assinatura: %v\nsaida do assinador:\n%s", err, strings.TrimSpace(output))
				}
				return fmt.Errorf("erro ao criar assinatura: %w", err)
			}

			fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura criada com sucesso\n")
			fmt.Fprintf(cmd.OutOrStdout(), "Entrada: %s\n", inputPath)
			fmt.Fprintf(cmd.OutOrStdout(), "Saida: %s\n", outputPath)
			if strings.TrimSpace(output) != "" {
				fmt.Fprintf(cmd.OutOrStdout(), "Detalhes:\n%s\n", strings.TrimSpace(output))
			}
			return nil
		},
	}

	cmd.Flags().StringVar(&jarPath, "jar", "assinador.jar", "Caminho para o assinador.jar")
	cmd.Flags().StringVar(&inputPath, "input", "", "Arquivo de entrada para assinatura")
	cmd.Flags().StringVar(&outputPath, "output", "", "Arquivo de saida da assinatura")
	cmd.Flags().StringVar(&mode, "mode", "server", "Modo de execucao: local ou server")
	cmd.Flags().StringVar(&port, "port", "8080", "Porta do servidor a ser utilizada no modo server")
	_ = cmd.MarkFlagRequired("input")
	_ = cmd.MarkFlagRequired("output")

	return cmd
}
