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
		Example: `  # Validar assinatura no modo servidor (padrão)
  assinatura validate --input documento.pdf --signature documento.sig

  # Validar assinatura usando uma porta específica de servidor
  assinatura validate --input documento.pdf --signature documento.sig --port 9090

  # Validar no modo local (sem subir servidor)
  assinatura validate --mode local --input documento.pdf --signature documento.sig --jar tools/assinador.jar

  # Validar de forma silenciosa (quiet)
  assinatura validate --input documento.pdf --signature documento.sig -q`,
		RunE: func(cmd *cobra.Command, args []string) error {
			var activeInvoker Invoker = invoker
			if mode == "server" {
				activeInvoker = NewHttpInvoker(port, jarPath)
			}

			javaArgs := []string{"validate", "--input", inputPath, "--signature", signaturePath}
			output, err := activeInvoker.Run(jarPath, javaArgs)

			if err != nil {
				if strings.Contains(err.Error(), "exit status") || (Verbose && strings.TrimSpace(output) != "") {
					if !Quiet {
						fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura INVALIDA\n")
					} else {
						fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura INVALIDA\n")
					}
					if Verbose && strings.TrimSpace(output) != "" {
						fmt.Fprintf(cmd.OutOrStdout(), "Detalhes:\n%s\n", strings.TrimSpace(output))
					}
					return nil
				}
				return fmt.Errorf("erro ao validar assinatura: %w", err)
			}

			if !Quiet {
				fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura VALIDA\n")
				fmt.Fprintf(cmd.OutOrStdout(), "Arquivo: %s\n", inputPath)
				fmt.Fprintf(cmd.OutOrStdout(), "Assinatura: %s\n", signaturePath)
			} else {
				fmt.Fprintf(cmd.OutOrStdout(), "Status: assinatura VALIDA\n")
			}
			
			if Verbose && strings.TrimSpace(output) != "" {
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
