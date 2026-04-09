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

	cmd := &cobra.Command{
		Use:   "validate",
		Short: "Valida assinatura digital de um artefato",
		Long:  `Executa o assinador.jar para validar assinatura digital de um arquivo.`,
		RunE: func(cmd *cobra.Command, args []string) error {
			javaArgs := []string{"validate", "--input", inputPath, "--signature", signaturePath}
			output, err := invoker.Run(jarPath, javaArgs)

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
	_ = cmd.MarkFlagRequired("input")
	_ = cmd.MarkFlagRequired("signature")

	return cmd
}
