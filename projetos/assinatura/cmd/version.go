package cmd

import (
	"fmt"
	"runtime"

	"github.com/spf13/cobra"
)

var version = "0.0.6"

func newVersionCmd() *cobra.Command {
	return &cobra.Command{
		Use:   "version",
		Short: "Exibe a versao atual do CLI",
		Long:  `Exibe a versao atual do CLI assinatura e outras informacoes de sistema.`,
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Fprintf(cmd.OutOrStdout(), "Assinatura CLI v%s %s/%s\n", version, runtime.GOOS, runtime.GOARCH)
		},
	}
}
