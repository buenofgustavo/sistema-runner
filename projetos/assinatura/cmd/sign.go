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

	cmd := &cobra.Command{
		Use:   "sign",
		Short: "Cria assinatura digital de um artefato",
		Long:  `Executa o assinador.jar para criar assinatura digital de um arquivo.`,
		RunE: func(cmd *cobra.Command, args []string) error {
			javaArgs := []string{"sign", "--input", inputPath, "--output", outputPath}
			output, err := invoker.Run(jarPath, javaArgs)
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
	_ = cmd.MarkFlagRequired("input")
	_ = cmd.MarkFlagRequired("output")

	return cmd
}
